package org.geneweaver.io.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
 * The databases holding the peaks are sharded because these tables need to be smaller
 * than 1mill and closer to 100k rows to be fast. In order to do this, we record the peak
 * in two tables, once for its lower location and once for its upper (unless they are the same).
 * Then when seeing if there is a connection to a Variant we take the base of its lower value
 * and look up the peaks in that database. 
 * 
 * @author gerrim
 *
 */
public class OverlapConnector<N extends Entity, E extends Entity> implements Connector<N, E>, AutoCloseable  {

	
	private static Logger logger = LoggerFactory.getLogger(OverlapConnector.class);
	private String tableName;
	private String fileName;

	private OverlapService oservice = new OverlapService();
	private String basePath;

	private List<Path> source = new LinkedList<>();
	
	// These will get large
	private Map<String,PreparedStatement>  insertCache = new HashMap<>(89);
	private Map<String,PreparedStatement>  selectCache = new HashMap<>(89);

	public OverlapConnector() {
		this("peaks");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public OverlapConnector(String databaseFileName) {
		this.tableName = System.getProperty("gweaver.mappingdb.tableName","REGIONS");
		this.fileName = databaseFileName;
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
			if (!path.getFileName().toString().toLowerCase().endsWith(".bed.gz") && 
				!path.getFileName().toString().toLowerCase().endsWith(".bed")	) return;
			
			if (limit>0 && source.size()>limit) return; // Do not add things after limit reached.
			
			source.add(path);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> stream(N ent, Session session) {
		
		// Other streams may run through this connector, but
		// if they sent other objects, we return them.
		if (!(ent instanceof Variant)) return (Stream<E>) Stream.of(ent);
		Variant variant = (Variant)ent;
		
		String shardName = oservice.getShardName(variant.getChr(), variant.getStart());

		Collection<Entity> ret = new LinkedList<>();
		ret.add(variant);
		try {
			PreparedStatement lookup = getSelectStatement(shardName);
			
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
			
		} catch (Exception ne) {
			logger.warn("Cannot map "+variant, ne);
		}
		
		return (Stream<E>) ret.stream();
	}

	public void close() throws SQLException {
		
		for (Statement stmt : insertCache.values()) {
			stmt.close();
		}
		insertCache.clear();
		
		for (Statement stmt : selectCache.values()) {
			stmt.close();
		}
		selectCache.clear();
		
		connection.close();
	}
	
	public void create() throws SQLException, ReaderException, IOException {

		if (source==null || source.isEmpty()) throw new IllegalArgumentException();
		for (Path path : source) {

			System.out.println(path+" of "+source.size());

			StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest(path.getFileName().toString(), path));
			reader.stream()
				  .forEach(reg -> storePeak(reg));
		} 
	}

	private void storePeak(Peak peak) {
		
		int lower = Math.min(peak.getStart(), peak.getEnd());
		int upper = Math.max(peak.getStart(), peak.getEnd());
		
		String lshardName = oservice.getShardName(peak.getChr(), lower);
		storePeakBase(lshardName, peak);
		
		String ubshardName = oservice.getShardName(peak.getChr(), upper);
		if (!ubshardName.equals(lshardName)) storePeakBase(ubshardName, peak);
	}


	private void storePeakBase(String shardName, Peak peak) {
		try {
			PreparedStatement stmt = getInsertStatement(shardName);
			// Put the key in, lower case.
			if (peak.getPeakId()==null) return; // We cannot map unnamed peaks.
			stmt.setString(1, peak.getPeakId());	
			
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
	
	
	private PreparedStatement getInsertStatement(String shardName) throws Exception {
		Connection conn = getConnection(shardName, false);
		PreparedStatement stmt = insertCache.get(shardName);
		if (stmt==null) {
			try (Statement create = conn.createStatement() ) {  
				
				String sql =  "CREATE TABLE IF NOT EXISTS " + tableName+shardName + 
							" (id int NOT NULL AUTO_INCREMENT, " + 
							// Important UNIQUE means there is an index and
							// that the later lookup will be fast.
							" peakId VARCHAR(128) NOT NULL UNIQUE, " +  
							" lower INTEGER," +
							" upper INTEGER);"; 
		
				create.executeUpdate(sql);
				logger.info("Create table if not exists "+shardName+":"+tableName);
			} 

			stmt = conn.prepareStatement("INSERT INTO "+tableName+shardName+" (peakId, lower, upper) VALUES (?,?,?);");
			insertCache.put(shardName, stmt);
		} 
		return stmt;
	}
	
	private PreparedStatement getSelectStatement(String shardName) throws Exception {
		
		Connection conn = getConnection(shardName, true);
		PreparedStatement stmt = selectCache.get(shardName);
		if (stmt==null) {
			String sql = "SELECT peakId, lower, upper FROM "+tableName+shardName+" WHERE (?>=lower AND ?<=upper) OR (?>=lower AND ?<=upper);";
			stmt = conn.prepareStatement(sql);
			selectCache.put(shardName, stmt);
		} 
		return stmt;
	}

	private Connection connection;

	private Connection getConnection(String shardName, boolean readOnly) throws Exception {
		if( connection == null) {
			connection = newConnection(shardName, readOnly);
		}
		return connection;
	}

	private Connection newConnection(String shardName, boolean readOnly) throws SQLException, IOException {
		
		//String path = this.basePath+shardName;
		String path = this.basePath;
		String uri = "jdbc:h2:"+path+";mode=MySQL";
		if (readOnly) uri = uri+";ACCESS_MODE_DATA=r";
		return DriverManager.getConnection(uri,"sa","");
	}

	/**
	 * Method used to add random rows to the database.
	 * 
	 * @param nrows
	 * @throws SQLException 
	 */
	int testAddRandomRows(String chr, int nrows) throws SQLException {
		
		for (int i = 0; i < nrows; i++) {

			Peak peak = new Peak();
			peak.setPeakId(UUID.randomUUID().toString());
			peak.setStart((int)Math.round(Math.random()*10000));
			peak.setEnd((int)Math.round(Math.random()*10000));
			peak.setChr(chr);
			storePeak(peak);
			if (i%1000000 == 0) System.out.println("Added randoms, size "+i);
		} 
		return nrows;
	}


	/**
	 * Set the location of the database. Sets the folder name.
	 * The actual database name is always the mapping file name with ".h2" appended.
	 * @param dir
	 */
	public void setLocation(Path dir) {
		String path = dir.toAbsolutePath().toString();
		this.basePath  = path+"/"+fileName;
	}

	public void add(Path hFile) throws FileNotFoundException {
		if (!Files.exists(hFile)) throw new FileNotFoundException(hFile.toString());
		this.source.add(hFile);
	}

	/**
	 * Size may be used only after importing all peaks to cache.
	 * @return the size.
	 * @throws SQLException
	 */
	public long getTotalImportedSize() throws SQLException {
		
		long size = 0;
		for (String shard : insertCache.keySet()) {
			try(Statement stmt = connection.createStatement()) {  

				String sql = "SELECT COUNT(1) FROM "+tableName+shard+";";
				try(ResultSet res = stmt.executeQuery(sql)) {
					res.next();
					size += res.getLong(1);
				}
			}
		}
		return size;
	}


}
