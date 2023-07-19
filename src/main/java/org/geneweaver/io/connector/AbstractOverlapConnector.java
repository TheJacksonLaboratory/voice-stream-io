package org.geneweaver.io.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Located;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOverlapConnector<N extends Entity, E extends Entity> implements Connector<N, E>, AutoCloseable {

	
	protected static Logger logger = LoggerFactory.getLogger(AbstractOverlapConnector.class);

	private String tableName;
	private String fileName;

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
	
	/**
	 * For testing we can limit the numbers of genes or variants processed
	 * into the database. This allows things to parse more quickly when create() is
	 * called.
	 */
	private Long limit;
	private Long skip;

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
		create(null, System.out);
	}

	/**
	 * Call this method to create a cache of the files which we have added.
	 * This cache is then used when the connector is streamed to look up locations.
	 * 
	 * @throws SQLException
	 * @throws ReaderException
	 * @throws IOException
	 */
	public void create(String prefix, PrintStream out) throws SQLException, ReaderException, IOException {

		if (source==null || source.isEmpty()) throw new IllegalArgumentException();
		int index = -1;
		for (Path path : source) {

			++index;
			if (out!=null) out.println("Input "+path+" "+index+" of "+source.size());

			ReaderRequest request = new ReaderRequest(path.getFileName().toString(), path);
			configure(request);
			
			StreamReader<?> reader = ReaderFactory.getReader(request);
			Stream<?> raw = reader.stream();
			
			// The skip is not that accurate because
			// we use it on the raw which might have other objects in.
			// However they are used in testing and will be slow if we
			// parse all the line in between.
			if (skip!=null && skip>0) {
				raw = raw.skip(skip.longValue());
			}
			
			Stream<Located> stream = raw.map(e->coerce(e));
			stream =  stream.filter(ChromosomeService::isValidChromosome)
						    .filter(l->isValidClass(l));
			
			if (limit!=null && limit>0) {
				stream = stream.limit(limit.longValue());
			}
			
			stream.forEach(loc -> store(loc, prefix, out));
		} 
	}

	/**
	 * Override to configure the request.
	 * @param request
	 */
	protected void configure(ReaderRequest request) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Override for readers which read file formats whose objects
	 * do not fit a normal read and need mapping to use with the connector.
	 * @param e
	 * @return
	 */
	protected Located coerce(Object e) {
		return (Located)e;
	}

	/**
	 * Override to filter class
	 * @param l
	 * @return true if class type is valid.
	 */
	protected boolean isValidClass(Object l) {
		return true;
	}

	protected <T extends Located> void store(T line, String prefix, PrintStream out) {
		
		int lower = Math.min(line.getStart(), line.getEnd());
		int upper = Math.max(line.getStart(), line.getEnd());
		
		String lshardName = oservice.getShardName(line.getChr(), lower);
		if (lshardName==null) {
			logger.warn("Could not find shard for "+line.getChr());
			return; // No shard
		}
		storeBase(lshardName, line, prefix, out);
		
		String ubshardName = oservice.getShardName(line.getChr(), upper);
		if (ubshardName==null) {
			logger.warn("Could not find shard for "+line.getChr());
			return; // No shard
		}
		if (!ubshardName.equals(lshardName)) storeBase(ubshardName, line, prefix, out);
	}


	private <T extends Located> void storeBase(String shardName, T line, String prefix, PrintStream out) {
		
		if (shardName==null) return;
		try {
			PreparedStatement stmt = getInsertStatement(line.getChr(), shardName, out);
			if (stmt==null) return; // Not all peaks have reasonable chromosomes.
			
			// Put the key in, lower case.
			String id = line.id();
			if (id==null) return; // We cannot map unnamed peaks.
			if (prefix!=null && !id.startsWith(prefix)) {
				throw new IllegalArgumentException("The id '"+id+"' should have started with '"+prefix+"'");
			}
			stmt.setString(1, id);	
			
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
	
	private PreparedStatement getInsertStatement(String chr, String shardName, PrintStream out) throws Exception {
		Connection conn = getConnection(chr, false, out);
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
		String cacheKey = name+"/"+fileName+"/"+shardName;
		PreparedStatement stmt = selectCache.get(cacheKey);
		if (stmt!=null) return stmt;
		
		Connection conn = getConnection(chr, true, null);
		if (conn==null) return null;
		if (stmt==null) {
			String sql = "SELECT entityId, lower, upper FROM "+tableName+shardName+" WHERE (?>=lower AND ?<=upper) OR (?>=lower AND ?<=upper);";
			stmt = conn.prepareStatement(sql);
			selectCache.put(cacheKey, stmt);
		} 
		return stmt;
	}

	protected Connection getConnection(String chr, boolean readOnly, PrintStream out) throws Exception {
		
		String connKey = fileName+"/"+chr;
		Connection ret = connCache.get(connKey);
		if (ret == null) {
			ret = newConnection(chr, readOnly, out);
			if (ret != null) connCache.put(connKey, ret);
		}
		return ret;
	}

	private Connection newConnection(String chr, boolean readOnly, PrintStream out) throws SQLException, IOException {
		
		chr = cservice.getChromosome(chr);
		if (chr==null) return null;
		String path = this.basePath+"_"+chr;
		if (out!=null) out.println("New database connection to file: "+path);
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

	/**
	 * @return the limit
	 */
	public Long getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Long limit) {
		this.limit = limit;
	}

	/**
	 * @return the skip
	 */
	public Long getSkip() {
		return skip;
	}

	/**
	 * @param skip the skip to set
	 */
	public void setSkip(Long skip) {
		this.skip = skip;
	}

	/**
	 * @return the fileName
	 */
	protected String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the tableName
	 */
	protected String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	protected void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
