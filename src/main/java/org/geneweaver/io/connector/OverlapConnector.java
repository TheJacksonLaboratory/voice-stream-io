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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
 * than 200mill and closer to 100k rows to be fast. In order to do this, we record the peak
 * in two tables if they straddle a shard boundary, once for its lower location and once 
 * for its upper (unless they are the same).
 * Then when seeing if there is a connection to a Variant we take the base of its lower value
 * and look up the peaks in that table (shard). 
 * 
 * In addition we use separate files for each chromosome with a separate connection. This
 * makes the connection somewhat faster because there can be 200mill base pairs in a chromosome
 * therefore if the base pair shards are 10000, there can be 20000 tables.
 * 
 * There are roughly 29 billion overlaps in the human variant to peak space on Ensembl.
 * 
 * @author gerrim
 *
 */
public class OverlapConnector<N extends Entity, E extends Entity> implements Connector<N, E>, AutoCloseable  {

	
	private static Logger logger = LoggerFactory.getLogger(OverlapConnector.class);

	private String tableName;
	private String fileName;

	private OverlapService oservice = new OverlapService();
	private ChromosomeService cservice = ChromosomeService.getInstance();
	private String basePath;

	private Collection<Path> source = new TreeSet<>();
	
	// Just done by chromosome
	private Map<String,Connection>		   connCache   =  Collections.synchronizedMap(new HashMap<>(23));

	// These will get large e.g. ~20k depending on BASE_SIZE
	private Map<String,PreparedStatement>  insertCache =  Collections.synchronizedMap(new HashMap<>(1009));
	private Map<String,PreparedStatement>  selectCache =  Collections.synchronizedMap(new HashMap<>(1009));

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
			if (!path.getFileName().toString().toLowerCase().endsWith(".bed.gz") && 
				!path.getFileName().toString().toLowerCase().endsWith(".bed")	) return;
			
			if (limit>0 && source.size()>limit) return; // Do not add things after limit reached.
			
			// The paths can have duplicates, especially for mouse. 
			// We must take the newer one.
			source.add(path);
		});
		this.source = removeOlderNames(source);
		return source;
	}

	// e.g.
	// mus_musculus.GRCm39.forebrain_embryonic_10_5_days.H3K36me3.ccat_histone.peaks.20201003.bed.gz
	// mus_musculus.GRCm39.forebrain_embryonic_10_5_days.H3K36me3.ccat_histone.peaks.20201021.bed.gz
	private static final Pattern datedName = Pattern.compile("^(.*)\\.peaks\\.(\\d+)\\.bed\\.gz$");
	/**
	 * The paths are sorted. Remove the older ones in the sorted stack.
	 * @param source2
	 */
	private Collection<Path> removeOlderNames(Collection<Path> paths) {
		
		List<Path> rev = new ArrayList<>(paths);
		
		// Review of the sorted order works because the file name ends with the numeric date.
		// Reverse puts the older ones later.
		Collections.reverse(rev);
		
		// Hold the stub names we have checked.
		Collection<String> checked = new HashSet<>();
		for (Iterator<Path> it = rev.iterator(); it.hasNext();) {
			Path path = it.next();
			String fileName = path.getFileName().toString();
			Matcher matcher = datedName.matcher(fileName);
			if (matcher.matches()) {
				String stub = matcher.group(1);
				if (checked.contains(stub)) {
					it.remove(); // Older duplicate removed.
					continue;
				}
				checked.add(stub);
			}
		}
		return rev;
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
		
		if (shardName!=null) {
	 		try {
				PreparedStatement lookup = getSelectStatement(variant.getChr(), shardName);
				if (lookup==null) { // Not all peaks have reasonable chromosomes.
					return (Stream<E>) ret.stream();
				}
	
				int vlower = Math.min(variant.getStart(), variant.getEnd());
				lookup.setInt(1, vlower);
				lookup.setInt(2, vlower);
				int vupper = Math.max(variant.getStart(), variant.getEnd());
				lookup.setInt(3, vupper);
				lookup.setInt(4, vupper);
	
				Set<String> usedIds = new HashSet<>();
				try (ResultSet res = lookup.executeQuery()) {
					while(res.next()) {
						String peakId = res.getString(1);
						if (usedIds.contains(peakId)) {
							logger.info("Encountered duplicate peakID: "+peakId);
							continue;
						}
						int rlow = res.getInt(2);
						int rup  = res.getInt(3);
						
						Overlap o = oservice.intersection(variant, new Peak(peakId, rlow, rup));
						if (o!=null) {
							o.setChr(variant.getChr());
							ret.add(o);
							usedIds.add(peakId);
						}
					}
				}
				
			} catch (Exception ne) {
				logger.warn("Cannot map "+variant, ne);
			}
		}
		
		return (Stream<E>) ret.stream();
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
	
	public void create() throws SQLException, ReaderException, IOException {

		if (source==null || source.isEmpty()) throw new IllegalArgumentException();
		int index = -1;
		for (Path path : source) {

			++index;
			System.out.println(path+" "+index+" of "+source.size());

			StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest(path.getFileName().toString(), path));
			reader.stream()
				  .filter(ChromosomeService::isValidChromosome)
				  .forEach(reg -> storePeak(reg));
		} 
	}

	private void storePeak(Peak peak) {
		
		int lower = Math.min(peak.getStart(), peak.getEnd());
		int upper = Math.max(peak.getStart(), peak.getEnd());
		
		String lshardName = oservice.getShardName(peak.getChr(), lower);
		if (lshardName==null) {
			logger.warn("Could not find shard for "+peak.getChr());
			return; // No shard
		}
		storePeakBase(lshardName, peak);
		
		String ubshardName = oservice.getShardName(peak.getChr(), upper);
		if (ubshardName==null) {
			logger.warn("Could not find shard for "+peak.getChr());
			return; // No shard
		}
		if (!ubshardName.equals(lshardName)) storePeakBase(ubshardName, peak);
	}


	private void storePeakBase(String shardName, Peak peak) {
		
		if (shardName==null) return;
		try {
			PreparedStatement stmt = getInsertStatement(peak.getChr(), shardName);
			if (stmt==null) return; // Not all peaks have reasonable chromosomes.
			
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
						" peakId VARCHAR(128) NOT NULL, " +  
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
	
	private synchronized PreparedStatement getSelectStatement(String chr, String shardName) throws Exception {
		
		String name = Thread.currentThread().getName();
		String cacheKey = name+"/"+shardName;
		PreparedStatement stmt = selectCache.get(cacheKey);
		if (stmt!=null) return stmt;
		
		Connection conn = getConnection(chr, true);
		if (conn==null) return null;
		if (stmt==null) {
			String sql = "SELECT peakId, lower, upper FROM "+tableName+shardName+" WHERE (?>=lower AND ?<=upper) OR (?>=lower AND ?<=upper);";
			stmt = conn.prepareStatement(sql);
			selectCache.put(cacheKey, stmt);
		} 
		return stmt;
	}

	private Connection getConnection(String chr, boolean readOnly) throws Exception {
		
		Connection ret = connCache.get(chr);
		if (ret == null) {
			ret = newConnection(chr, readOnly);
			if (ret != null) connCache.put(chr, ret);
		}
		return ret;
	}

	private Connection newConnection(String chr, boolean readOnly) throws SQLException, IOException {
		
		chr = cservice.getChromosome(chr);
		if (chr==null) return null;
		String path = this.basePath+"_"+chr;
		String uri = "jdbc:h2:"+path+";mode=MySQL";
		if (readOnly) uri = uri+";ACCESS_MODE_DATA=r";
		return DriverManager.getConnection(uri,"sa","");
	}

	private long roughBPperChr = 200000000;
	
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
			peak.setStart((int)(Math.random()*roughBPperChr));
			peak.setEnd((int)(Math.random()*roughBPperChr));
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

}
