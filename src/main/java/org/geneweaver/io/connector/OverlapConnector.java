package org.geneweaver.io.connector;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Region;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This function reads all the regions from their separate files
 * and caches them in a large table. This table can then be used to map
 * Variants to Regions using Intersection connections.
 * 
 * This connector should be used with Variants and return a stream of the
 * variant and all the Intersections of that variant with Regions form the bed files.
 * 
 * @author gerrim
 *
 */
public class OverlapConnector<N extends Variant, E extends Entity> extends AbstractDatabaseConnector implements Connector<N, E>, AutoCloseable  {

	
	private static Logger logger = LoggerFactory.getLogger(OverlapConnector.class);

	public OverlapConnector() {
		this("regions.h2");
	}

	public OverlapConnector(String databaseFileName) {
		super(System.getProperty("gweaver.mappingdb.tableName","REGIONS"), databaseFileName);
	}


	private Connection connection;
	private PreparedStatement lookup;
	private OverlapService oservice;

	@Override
	public Stream<E> stream(N variant, Session session) {
		
		if (connection==null) {
			try {
				connection = createConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		if (oservice==null) oservice = new OverlapService();

		Collection<Entity> ret = new LinkedList<>();
		ret.add(variant);
		try {
			if (lookup==null) lookup = connection.prepareStatement("SELECT peakId FROM "+tableName+" WHERE lower >= ? OR upper <= ?;");
			
			int lower = Math.min(variant.getStart(), variant.getEnd());
			lookup.setInt(1, lower);
			int upper = Math.max(variant.getStart(), variant.getEnd());
			lookup.setInt(2, upper);

			try (ResultSet res = lookup.executeQuery()) {
				while(res.next()) {
					String peakId = res.getString(1);
					int rlow = res.getInt(2);
					int rup  = res.getInt(3);
					
					Overlap o = oservice.intersection(variant, new Region(peakId, rlow, rup));
					if (o!=null) ret.add(o);
				}
			}
			
		} catch (SQLException ne) {
			logger.warn("Cannot map "+variant, ne);
		}
		
		return (Stream<E>) ret.stream();

	}

	public void close() throws SQLException {
		if (connection!=null) connection.close();
		if (lookup!=null) lookup.close();
	}

	@Override
	protected void createDatabase() throws IOException, SQLException {
		try (Connection conn = createConnection();
			 Statement stmt = conn.createStatement() ) {  

			String sql =  "CREATE TABLE " + tableName + 
						" (id int NOT NULL AUTO_INCREMENT, " + 
						// Important UNIQUE means there is an index and
						// that the later lookup will be fast.
						" peakId VARCHAR(64) NOT NULL UNIQUE, " +  
						" lower INTEGER," +
						" upper INTEGER);"; 

			stmt.executeUpdate(sql);
			logger.info("Created table "+tableName);
		}
	}

	@Override
	protected void parseSource() throws SQLException, ReaderException {
		try (Connection conn = createConnection();
			 PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+tableName+" (peakId, lower, upper) VALUES (?,?,?);") ) {  

			for (Integer code : source.keySet()) {

				File file = source.get(code);
				StreamReader<Region> reader = ReaderFactory.getReader(new ReaderRequest(String.valueOf(code), file));
				reader.stream()
					  .forEach(reg -> storeRegion(reg, stmt));
			} 
		}
	}

	private void storeRegion(Region region, PreparedStatement stmt) {
		try {
			
			// Put the key in, lower case.
			if (region.getPeakId()==null) return; // We cannot map unnamed peaks.
			stmt.setString(1, region.getPeakId().toString());	
			
			int lower = Math.min(region.getStart(), region.getEnd());
			stmt.setInt(2,lower);
			
			int upper = Math.max(region.getStart(), region.getEnd());
			stmt.setInt(3,upper);
			stmt.execute();
			
		} catch (Exception ne) {
			ne.printStackTrace();
			throw new RuntimeException(ne);
		}
	}

}
