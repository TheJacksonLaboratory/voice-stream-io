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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractOverlapConnector<N extends Entity, E extends Entity> implements Connector<N, E>, AutoCloseable, IntersectionCreator {

	
	protected static Logger logger = LoggerFactory.getLogger(AbstractOverlapConnector.class);
	private static ObjectMapper mapper = new ObjectMapper();

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
	
		
	// Every so often we print that overlaps are found in verbose mode.
	protected volatile int count = 0;
	protected int frequency = 10000;

	/**
	 * Mouse peaks and reg features have two matching files with repeats in them, 
	 * take only the newer file by name if this boolean is set.
	 */
	protected boolean newestInDirectoryByName = false;

	/**
	 * Type ref for map of <String,Object>
	 */
	protected TypeReference<HashMap<String,Object>> mapReference = new TypeReference<HashMap<String,Object>>() {};

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
			
			if (isNewestInDirectoryByName()) {
				
				try {
					List<Path> peers = new ArrayList<>(Files.list(path.getParent()).toList());
					Collections.sort(peers);
					Path last = peers.get(peers.size()-1);
					if (!Files.isSameFile(path, last)) {
						return; // Do not add older files.
					}
				} catch (IOException ne) {
					logger.error("Cannot check for older ignored files in dir {}", path.getParent());
				}
			}
			
			// The paths can have duplicates, especially for mouse. 
			// We must take the newer one.
			source.add(path);
		});
		return source;
	}
	
	public long create() throws SQLException, ReaderException, IOException {
		return create(null, System.out);
	}

	/**
	 * Call this method to create a cache of the files which we have added.
	 * This cache is then used when the connector is streamed to look up locations.
	 * 
	 * @throws SQLException
	 * @throws ReaderException
	 * @throws IOException
	 */
	public final <T extends Located> long create(String prefix, PrintStream out) throws SQLException, ReaderException, IOException {

		if (source==null || source.isEmpty()) throw new IllegalArgumentException();
		int index = -1;
		
		long added = 0;
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
			
			stream = stream.filter(l->filter(l));
			
			stream =  stream.filter(ChromosomeService::isValidChromosome)
						    .filter(l->isValidClass(l));
			
			if (limit!=null && limit>0) {
				stream = stream.limit(limit.longValue());
			}
			
			long stored = stream.map(loc -> store(loc, prefix, out))
						        .filter(s->s!=null)
							    .count();
			added += stored;
			
		} 
		return added;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> stream(N ent, Session session, PrintStream log) {
		
		// Other streams may run through this connector, but
		// if they send other objects, we return them.
		if (!(ent instanceof Variant)) return (Stream<E>) Stream.of(ent);
		Variant variant = (Variant)ent;
		
		String shardName = oservice.getShardName(variant.getChr(), variant.getStart());

		Collection<Entity> ret = new LinkedList<>();
		ret.add(variant);
		
		if (log!=null && count%frequency==0) {
			log.println("Using shard: "+shardName);
		}
		
		if (shardName!=null) {
	 		try {
				PreparedStatement lookup = getSelectStatement(variant.getChr(), shardName, log);
				if (lookup==null) { // Not all peaks have reasonable chromosomes.
					return (Stream<E>) ret.stream();
				}
				
				int a = Math.min(variant.getStart(), variant.getEnd());
				lookup.setInt(1, a);
				int b = Math.max(variant.getStart(), variant.getEnd());
				lookup.setInt(2, b);
	
				Set<String> usedIds = new HashSet<>();
				
				try (ResultSet res = lookup.executeQuery()) {

					while(res.next()) {
						String id = res.getString(1);
						if (id==null) continue;
						if (usedIds.contains(id)) {
							logger.info("Encountered duplicate id ("+getClass().getSimpleName()+"): "+id);
							continue;
						}
						
						if (!testId(id)) continue;
						
						int rlow 		= res.getInt(2);
						int rup  		= res.getInt(3);
						String smeta  	= res.getString(4);
						
						if (log!=null && count%frequency==0) {
							log.println("Example of id ("+getClass().getSimpleName()+") found: "+id);
						}

						Map<String,Object> meta = mapper.readValue(smeta, mapReference);
						AbstractEntity o = oservice.intersection(variant, createIntersectionObject(id, rlow, rup), this, meta);
						if (o!=null) {
							o.setChr(variant.getChr());
							ret.add(o);
							usedIds.add(id);
							
							if (log!=null && count%frequency==0) {
								log.println("Example of overlap found: "+o.toCsv());
							}
						}

					}
					
				}
				
	 		} catch (Exception ne) {
				logger.warn("Cannot map "+variant, ne);
			}
	 		
		}
		count++;
		
		return (Stream<E>) ret.stream();
	
	}
	
	/**
	 * Create an intersection object which we will compare with intersection,
	 * fill in parameters and return as the overlap object.
	 * @param id - unique id
	 * @param start - bp start
	 * @param end - bp end
	 * @return intersection object which we will compare with intersection
	 */
	protected abstract Located createIntersectionObject(String id, int start, int end);
		
	protected boolean testId(String id) {
		return true;
	}

	/**
	 * Implement to provide custom filtering to the input stream.
	 * @param loc
	 * @return
	 */
	protected boolean filter(Located loc) {
		return true;
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

	protected <T extends Located> T store(T line, String prefix, PrintStream out) {
		
		int lower = Math.min(line.getStart(), line.getEnd());
		int upper = Math.max(line.getStart(), line.getEnd());
		String chr = line.getChr();
		String lshardName = oservice.getShardName(chr, lower);
		if (lshardName==null) {
			String msg = "Could not find shard for "+chr;
			logger.warn(msg);
			out.println(msg);
			return null; // No shard
		}
		storeBase(lshardName, line, prefix, out);
		
		String ubshardName = oservice.getShardName(chr, upper);
		if (ubshardName==null) {
			String msg = "Could not find shard for "+chr;
			logger.warn(msg);
			out.println(msg);
			return null; // No shard
		}
		if (!ubshardName.equals(lshardName)) storeBase(ubshardName, line, prefix, out);
		return line;
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
			
			Map<String, Object> meta = getMeta(line);
			String smeta = mapper.writeValueAsString(meta);
			stmt.setString(4,smeta);
			stmt.execute();
			
		} catch (Exception ne) {
			ne.printStackTrace();
			throw new RuntimeException(ne);
		}
	}
	
	protected <T extends Located> Map<String, Object> getMeta(T line) {
		return Collections.emptyMap();
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
						" upper INTEGER," +
						" meta CHARACTER(1024));"; 

				create.executeUpdate(sql);
				logger.info("Create table if not exists "+shardName+":"+tableName);
			} 

			stmt = conn.prepareStatement("INSERT INTO "+tableName+shardName+" (entityId, lower, upper, meta) VALUES (?,?,?,?);");
			insertCache.put(shardName, stmt);
		} 
		return stmt;
	}
	
	protected synchronized PreparedStatement getSelectStatement(String chr, String shardName, PrintStream out) throws Exception {
		
		String name = Thread.currentThread().getName();
		String cacheKey = name+"/"+fileName+"/"+shardName;
		PreparedStatement stmt = selectCache.get(cacheKey);
		if (stmt!=null) return stmt;
		
		Connection conn = getConnection(chr, true, out);
		if (conn==null) return null;
		if (stmt==null) {
			// Does (a,b) bisect (lower,upper)?
			// (a <= upper) && (lower <= b);
			String sql = "SELECT entityId, lower, upper, meta FROM "+tableName+shardName+" WHERE (?<=upper AND lower<=?);";
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

	/**
	 * @return the frequency
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the newestInDirectoryByName
	 */
	public boolean isNewestInDirectoryByName() {
		return newestInDirectoryByName;
	}

	/**
	 * @param newestInDirectoryByName the newestInDirectoryByName to set
	 */
	public void setNewestInDirectoryByName(boolean newestInDirectoryByName) {
		this.newestInDirectoryByName = newestInDirectoryByName;
	}

}
