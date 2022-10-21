package org.geneweaver.io.connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Peak;
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
public class OverlapConnector<N extends Entity, E extends Entity> extends AbstractDatabaseConnector implements Connector<N, E>, AutoCloseable  {

	
	private static Logger logger = LoggerFactory.getLogger(OverlapConnector.class);

	public OverlapConnector() {
		this("regions.h2");
	}

	public OverlapConnector(String databaseFileName) {
		super(System.getProperty("gweaver.mappingdb.tableName","REGIONS"), databaseFileName);
	}
	
	/**
	 * Adds all the bed.gz files to be cached recursively.
	 * @param dir
	 * @throws IOException 
	 */
	public void addAll(Path dir) throws IOException {
		addAll(dir, -1);
	}
	
	/**
	 * Adds all the bed.gz files to be cached recursively.
	 * Stopping if the limit is reached (reduces total files for testing).
	 * @param dir
	 * @param limit
	 * @throws IOException 
	 */
	void addAll(Path dir, int limit) throws IOException {
		Files.walk(dir).forEach(path->{
			if (!Files.isRegularFile(path)) {
				logger.debug(path+" is not a regular file and will not be used!");
				return;
			}
			if (!path.getFileName().toString().toLowerCase().endsWith(".bed.gz")) return;
			try {
				if (limit>0 && source.size()>limit) return; // Do not add things after limit reached.
				add(source.size(), path);
			} catch (ClassNotFoundException | FileNotFoundException e) {
				logger.error(path.toString(), e);
			}
		});
	}

	private Connection connection;
	private PreparedStatement lookup;
	private OverlapService oservice;

	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> stream(N ent, Session session) {
		
		// Other streams may run through this connector, but
		// if they sent other objects, we return them.
		if (!(ent instanceof Variant)) return (Stream<E>) Stream.of(ent);
		Variant variant = (Variant)ent;
		
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
			if (lookup==null) lookup = connection.prepareStatement("SELECT peakId, lower, upper FROM "+tableName+" WHERE (?>=lower AND ?<=upper) OR (?>=lower AND ?<=upper);");
			
			int vlower = Math.min(variant.getStart(), variant.getEnd());
			lookup.setInt(1, vlower);
			lookup.setInt(2, vlower);
			int vupper = Math.max(variant.getStart(), variant.getEnd());
			lookup.setInt(3, vupper);
			lookup.setInt(4, vupper);

			try (ResultSet res = lookup.executeQuery()) {
				while(res.next()) {
					String peakId = res.getString(1);
					int rlow = res.getInt(2);
					int rup  = res.getInt(3);
					
					Overlap o = oservice.intersection(variant, new Peak(peakId, rlow, rup));
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
						" peakId VARCHAR(128) NOT NULL UNIQUE, " +  
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
				System.out.println(file.getName()+" "+code+" of "+source.size());
				
				StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest(String.valueOf(code), file));
				reader.stream()
					  .forEach(reg -> storeRegion(reg, stmt));
			} 
		}
	}

	private void storeRegion(Peak peak, PreparedStatement stmt) {
		try {
			
			// Put the key in, lower case.
			if (peak.getPeakId()==null) return; // We cannot map unnamed peaks.
			stmt.setString(1, peak.getPeakId().toString());	
			
			int lower = Math.min(peak.getStart(), peak.getEnd());
			stmt.setInt(2,lower);
			
			int upper = Math.max(peak.getStart(), peak.getEnd());
			stmt.setInt(3,upper);
			stmt.execute();
			
		} catch (Exception ne) {
			ne.printStackTrace();
			throw new RuntimeException(ne);
		}
	}

}
