package org.jax.gweaver.io.connector;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.jax.gweaver.domain.EQTL;
import org.jax.gweaver.io.reader.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This is a function for mapping variant id in GTEx to rsId which is
 * used to map to variant.
 * 
 * Because the mapping is a map which is large to fit in memory, a h2
 * database is used to hold the map. This database will not be recreated 
 * if it exists (you must manually delete it) but it is mapping file specific
 * (different maps give different databases).
 * 
 * Example of lookup:
 * @see https://storage.googleapis.com/gtex_analysis_v8/reference/GTEx_Analysis_2017-06-05_v8_WholeGenomeSeq_838Indiv_Analysis_Freeze.lookup_table.txt.gz
 * 
 * @author gerrim
 *
 * @param <N>
 * @param <E>
 */
public class EQTLFunction<N extends EQTL, E extends EQTL> implements Function<N, E>, AutoCloseable {

	private static Logger logger = LoggerFactory.getLogger(EQTLFunction.class);

	private static final String driver 		= System.getProperty("gweaver.gtex.mappingdb.driver", "org.h2.Driver");   
	private static final String tableName 	= System.getProperty("gweaver.gtex.mappingdb.tableName","IDMAPPING");

	private Serializable mapping;

	private String split = System.getProperty("gweaver.gtex.mappingdb.lookupDelimiter", "\\t+");
	private String dabasePath;

	public EQTLFunction(Path path) throws ClassNotFoundException {
		this(path.toFile());
	}

	public EQTLFunction(File mapping) throws ClassNotFoundException {
		this.mapping = mapping;
		setLocation(Paths.get("."));
		Class.forName(driver); // Load driver class.
	}

	public EQTLFunction(URL mapping) throws ClassNotFoundException {
		this.mapping = mapping;
		setLocation(Paths.get("."));
		Class.forName(driver); // Load driver class.
	}

	/**
	 * Set the location of the database. Sets the folder name.
	 * The actual database name is always the mapping file name with ".h2" appended.
	 * @param dir
	 */
	public void setLocation(Path dir) {
		String path = dir.toAbsolutePath().toString();
		this.dabasePath  = path+"/"+FilenameUtils.getName(mapping.toString())+".h2";
	}

	private Connection connection;
	private PreparedStatement lookup;
	
	/**
	 * You must call create() to set up the database.
	 * This will take a longish time if it does not exist yet.
	 * For example the standard GTEx lookup database is 2.2Gb with 
	 * 46569704 objects, @see EQTLFunctionTest
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E apply(N t) {
		
		// We are setting the rsId. If it was found
		// already, our work here is done.
		if (t.getRsId()!=null) {
			return (E)t;
		}
		
		if (connection==null) {
			try {
				connection = createConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		
		String variantId = t.getvId();
		try {
			if (lookup==null) lookup = connection.prepareStatement("SELECT rsId FROM "+tableName+" WHERE variantId = ?;");
			
			lookup.setString(1, variantId);
			try (ResultSet res = lookup.executeQuery()) {
				res.next();
				String rsId = res.getString(1);
				t.setRsId(rsId);
			}
			
		} catch (SQLException ne) {
			throw new RuntimeException("Cannot map "+variantId, ne);
		}
		return (E)t;
	}
	
	public void close() throws SQLException {
		if (connection!=null) connection.close();
		if (lookup!=null) lookup.close();
	}

	/**
	 * Create the mapping database from variantId to rsId.
	 * This call takes a long time!
	 * 
	 * It is always made to a folder "./variantMappingDatabase". If this
	 * already exists, then a new one will not be created.
	 * 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public void create() throws IOException, SQLException {
		if (exists()) {
			logger.info("The database "+dabasePath+" already exists and will not be recreated.");
			return;
		}
		createMappingDatabase();
		parseMappingFile();
	}
	
	private void parseMappingFile() throws SQLException, IOException {

		// A Map<> as big as the mapping file does not 
		// fit in memory so we use an embedded table.
		
		try (Connection conn = createConnection();
			 PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+tableName+" (variantId, rsId) VALUES (?,?);") ) {  

			Iterator<String> iterator = StreamUtil.createStream(mappingInputStream(), mappingName(), true);
			try {
				int varIndex = -1;
				int rsIndex  = -1;

				while(iterator.hasNext()) {
					String line = iterator.next();
					String[] frags = line.split(getSplit());

					// Parse the header line, only if we have not
					if (varIndex<0 || rsIndex<0) {
						for (int i = 0; i < frags.length; i++) {
							if ("variant_id".equals(frags[i].toLowerCase())) {
								varIndex = i;
							} else if (frags[i].toLowerCase().startsWith("rs_id_")) {
								rsIndex = i;
							}
						}
						continue;
					}

					stmt.setString(1, frags[varIndex]);
					stmt.setString(2, frags[rsIndex]);
					
					stmt.execute();
				}

			} finally {
				if (iterator instanceof Closeable) {
					try {
						((Closeable)iterator).close();
					} catch (IOException e) {
						throw e;
					}
				}
			}
		}
	}

	private void createMappingDatabase() throws IOException, SQLException {

		try (Connection conn = createConnection();
			 Statement stmt = conn.createStatement() ) {  

			String sql =  "CREATE TABLE " + tableName + 
						" (id int NOT NULL AUTO_INCREMENT, " + 
						// Important UNIQUE means there is an index and
						// that the later lookup will be fast.
						" variantId VARCHAR(512) NOT NULL UNIQUE, " +  
						" rsId VARCHAR(32));"; 

			stmt.executeUpdate(sql);
			logger.info("Created table IDMAPPING");
		}
	}

	private Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:"+dabasePath,"sa","");
	}

	/**
	 * Create an input stream for the mapping or none if no mapping file.
	 * @return
	 * @throws IOException
	 */
	@JsonIgnore
	InputStream mappingInputStream() throws IOException {
		if (mapping==null) return null;
		if (mapping instanceof File) return new FileInputStream((File)mapping);
		if (mapping instanceof URL) return ((URL)mapping).openStream();
		return null;
	}

	/**
	 * Get the name from the mapping or null if no mapping.
	 * @return
	 * @throws IOException
	 */
	@JsonIgnore
	String mappingName() throws IOException {
		if (mapping==null) return null;
		return FilenameUtils.getName(mapping.toString());
	}

	/**
	 * @return the delimiter
	 */
	public String getSplit() {
		return split;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setSplit(String split) {
		this.split = split;
	}

	
	public boolean exists() {
		Path db = Paths.get(dabasePath+".mv.db");
		return Files.exists(db);
	}

	public int size() throws SQLException {
		
		try (Connection conn = createConnection();
			 Statement stmt = conn.createStatement() ) {  

			String sql = "SELECT COUNT(1) FROM "+tableName+";";
			try(ResultSet res = stmt.executeQuery(sql)) {
				res.next();
				return res.getInt(1);
			}
		}
	}
}
