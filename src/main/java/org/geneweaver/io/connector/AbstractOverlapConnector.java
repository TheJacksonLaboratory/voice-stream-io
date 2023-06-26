package org.geneweaver.io.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Located;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOverlapConnector<N extends Entity, E extends Entity> implements Connector<N, E>, AutoCloseable {

	
	protected static Logger logger = LoggerFactory.getLogger(AbstractOverlapConnector.class);

	protected String tableName;
	protected String fileName;

	protected OverlapService oservice = new OverlapService();
	protected ChromosomeService cservice = ChromosomeService.getInstance();
	protected String basePath;

	protected Collection<Path> source = new TreeSet<>();
	
	// Just done by chromosome
	protected Map<String,Connection>		   connCache   =  Collections.synchronizedMap(new HashMap<>(23));

	// These will get large e.g. ~20k depending on BASE_SIZE
	protected Map<String,PreparedStatement>  insertCache =  Collections.synchronizedMap(new HashMap<>(1009));
	protected Map<String,PreparedStatement>  selectCache =  Collections.synchronizedMap(new HashMap<>(1009));

	protected List<String> fileFilters = new LinkedList<>();

	public void add(Path hFile) throws FileNotFoundException {
		if (!Files.exists(hFile)) throw new FileNotFoundException(hFile.toString());
		this.source.add(hFile);
	}

	/**
	 * Adds all the files to be cached recursively.
	 * @param dir
	 * @throws IOException 
	 */
	public Collection<Path> addAll(Path dir) throws IOException {
		return addAll(dir, -1);
	}
	
	/**
	 * Adds all the bed.gz files to be cached recursively.
	 * Stopping if the limit is reached (reduces total files for testing).
	 * @param dir
	 * @param limit
	 * @throws IOException 
	 */
	Collection<Path> addAll(Path dir, int limit) throws IOException {
		Files.walk(dir).forEach(path->{
			if (!Files.isRegularFile(path)) {
				logger.debug(path+" is not a regular file and will not be used!");
				return;
			}
			
			boolean isOkay = fileFilters.isEmpty();
			for (String filter : this.fileFilters) {
				if (path.getFileName().toString().toLowerCase().endsWith(filter)) {
					isOkay = true;
					break;
				}
			}
			if (!isOkay) return;
			
			if (limit>0 && source.size()>limit) return; // Do not add things after limit reached.
			
			// The paths can have duplicates, especially for mouse. 
			// We must take the newer one.
			source.add(path);
		});
		return source;
	}
	
	/**
	 * Call this method to create a cache of the files which we have added.
	 * This cache is then used when the connector is streamed to look up locations.
	 * 
	 * @throws SQLException
	 * @throws ReaderException
	 * @throws IOException
	 */
	public void create() throws SQLException, ReaderException, IOException {

		if (source==null || source.isEmpty()) throw new IllegalArgumentException();
		int index = -1;
		for (Path path : source) {

			++index;
			System.out.println(path+" "+index+" of "+source.size());

			StreamReader<Located> reader = ReaderFactory.getReader(new ReaderRequest(path.getFileName().toString(), path));
			reader.stream()
				  .filter(ChromosomeService::isValidChromosome)
				  .filter(l->isValidClass(l))
				  .forEach(reg -> store(reg));
		} 
	}

	/**
	 * Override to filter class
	 * @param l
	 * @return true if class type is valid.
	 */
	protected boolean isValidClass(Object l) {
		return true;
	}

	protected <T extends Located> void store(T line) {
		
		int lower = Math.min(line.getStart(), line.getEnd());
		int upper = Math.max(line.getStart(), line.getEnd());
		
		String lshardName = oservice.getShardName(line.getChr(), lower);
		if (lshardName==null) {
			logger.warn("Could not find shard for "+line.getChr());
			return; // No shard
		}
		storeBase(lshardName, line);
		
		String ubshardName = oservice.getShardName(line.getChr(), upper);
		if (ubshardName==null) {
			logger.warn("Could not find shard for "+line.getChr());
			return; // No shard
		}
		if (!ubshardName.equals(lshardName)) storeBase(ubshardName, line);
	}


	private <T extends Located> void storeBase(String shardName, T line) {
		
		if (shardName==null) return;
		try {
			PreparedStatement stmt = getInsertStatement(line.getChr(), shardName);
			if (stmt==null) return; // Not all peaks have reasonable chromosomes.
			
			// Put the key in, lower case.
			if (line.id()==null) return; // We cannot map unnamed peaks.
			stmt.setString(1, line.id());	
			
			int lower = Math.min(line.getStart(), line.getEnd());
			stmt.setInt(2,lower);
			
			int upper = Math.max(line.getStart(), line.getEnd());
			stmt.setInt(3,upper);
			stmt.execute();
			
		} catch (Exception ne) {
			ne.printStackTrace();
			throw new RuntimeException(ne);
		}
	}
	
	private PreparedStatement getInsertStatement(String chr, String shardName) throws Exception {
		Connection conn = getConnection(chr, false);
		if (conn==null) return null;
		PreparedStatement stmt = insertCache.get(shardName);
		if (stmt==null) {
			try (Statement create = conn.createStatement() ) {  

				String sql =  "CREATE TABLE IF NOT EXISTS " + tableName+shardName + 
						" (id int NOT NULL AUTO_INCREMENT, " + 
						// Important UNIQUE means there is an index and
						// that the later lookup will be fast.
						" entityId VARCHAR(128) NOT NULL, " +  
						" lower INTEGER," +
						" upper INTEGER);"; 

				create.executeUpdate(sql);
				logger.info("Create table if not exists "+shardName+":"+tableName);
			} 

			stmt = conn.prepareStatement("INSERT INTO "+tableName+shardName+" (entityId, lower, upper) VALUES (?,?,?);");
			insertCache.put(shardName, stmt);
		} 
		return stmt;
	}
	
	protected synchronized PreparedStatement getSelectStatement(String chr, String shardName) throws Exception {
		
		String name = Thread.currentThread().getName();
		String cacheKey = name+"/"+shardName;
		PreparedStatement stmt = selectCache.get(cacheKey);
		if (stmt!=null) return stmt;
		
		Connection conn = getConnection(chr, true);
		if (conn==null) return null;
		if (stmt==null) {
			String sql = "SELECT entityId, lower, upper FROM "+tableName+shardName+" WHERE (?>=lower AND ?<=upper) OR (?>=lower AND ?<=upper);";
			stmt = conn.prepareStatement(sql);
			selectCache.put(cacheKey, stmt);
		} 
		return stmt;
	}

	protected Connection getConnection(String chr, boolean readOnly) throws Exception {
		
		Connection ret = connCache.get(chr);
		if (ret == null) {
			ret = newConnection(chr, readOnly);
			if (ret != null) connCache.put(chr, ret);
		}
		return ret;
	}

	protected Connection newConnection(String chr, boolean readOnly) throws SQLException, IOException {
		
		chr = cservice.getChromosome(chr);
		if (chr==null) return null;
		String path = this.basePath+"_"+chr;
		String uri = "jdbc:h2:"+path+";mode=MySQL";
		if (readOnly) uri = uri+";ACCESS_MODE_DATA=r";
		return DriverManager.getConnection(uri,"sa","");
	}

	/**
	 * @return the fileFilters
	 */
	protected List<String> getFileFilters() {
		return fileFilters;
	}

	/**
	 * @param fileFilters the fileFilters to set
	 */
	protected void setFileFilters(List<String> fileFilters) {
		this.fileFilters = fileFilters;
	}
	/**
	 * @param fileFilters the fileFilters to set
	 */
	protected void setFileFilters(String... fileFilters) {
		this.fileFilters = Arrays.asList(fileFilters);
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
	
	/**
	 * Size may be used only after importing all located objects (e.g. peaks) to cache.
	 * @return the size.
	 * @throws Exception 
	 */
	public long size() throws Exception {
		
		// We get the size of the tables in the dir
		Path dir = Paths.get(this.basePath).getParent();
		List<Path> files = Files.list(dir)
				                .filter(Files::isRegularFile)
				                .filter(p->p.getFileName().toString().toLowerCase().endsWith(".mv.db"))
				                .collect(Collectors.toList());
		
		long size = 0;
		for (Path path : files) {
			try (Connection conn = createConnection(path);
			     Statement tabs = conn.createStatement()) {
				
				DatabaseMetaData md = conn.getMetaData();
				ResultSet rs = md.getTables(null, null, "%", null);
				List<String> names = new ArrayList<>();
				while (rs.next()) {
					String tname = rs.getString(3);
					if (tname.startsWith(this.tableName)) names.add(tname);
				}
				
				for (String tname : names) {
					try(Statement stmt = conn.createStatement()) {  
		
						String sql = "SELECT COUNT(1) FROM "+tname+";";
						try(ResultSet res = stmt.executeQuery(sql)) {
							res.next();
							size += res.getLong(1);
						}
					}
				}
			}
		}
		return size;
	}
	
	private Connection createConnection(Path path) throws SQLException {
		
		String spath = path.toString().substring(0, path.toString().toLowerCase().lastIndexOf(".mv.db"));
		String uri = "jdbc:h2:"+spath+";mode=MySQL;ACCESS_MODE_DATA=r";
		return DriverManager.getConnection(uri,"sa","");
	}


	public void close() throws SQLException {
		
		for (String shard : insertCache.keySet()) {
			Statement stmt = insertCache.get(shard);
			stmt.close();
		}
		insertCache.clear();
		
		for (Statement stmt : selectCache.values()) {
			stmt.close();
		}
		selectCache.clear();
		
		for (Connection conn : connCache.values()) {
			conn.close();
		}
		connCache.clear();
	}

}
