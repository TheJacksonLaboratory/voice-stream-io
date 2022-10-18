package org.geneweaver.io.connector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.geneweaver.io.reader.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatabaseConnector {

	private static Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnector.class);


	protected static final String driver 		= System.getProperty("gweaver.mappingdb.driver", "org.h2.Driver");   

	protected String dabasePath;
	protected final String tableName; 
	protected String databaseFileName;
	
	/**
	 * The source of data which we will cache prior to doing the mapping.
	 * IF the create() method is called before setting sources, it will fail.
	 */
	protected Map<Integer, File> source = new HashMap<>();

	/**
	 * Set to avoid caching the database and always make a new one.
	 */
	private boolean newDatabase = false;

	public AbstractDatabaseConnector(String tableName, String databaseFileName) {
		this.tableName = tableName;
		this.databaseFileName = databaseFileName;
	}

	/**
	 * Add the genes from this file to the database we are building.
	 * @param taxon
	 * @param gtf
	 * @throws ClassNotFoundException
	 */
	public void add(int taxon, Path gtf) throws ClassNotFoundException, FileNotFoundException {
		add(taxon, gtf.toAbsolutePath().toFile());
	}

	/**
	 * Add the genes from this file to the database we are building.
	 * @param taxon
	 * @param gtf
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException 
	 */
	public void add(int taxon, File gtf) throws ClassNotFoundException, FileNotFoundException {	
		if (!gtf.exists()) throw new FileNotFoundException(gtf+" is not there!");
		if (dabasePath==null) {
			setLocation(gtf.getParentFile().toPath());
			Class.forName(driver); // Load driver class.
		}
		source.put(taxon, gtf);
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
	public void create() throws Exception {
		// A Map<> as big as the mapping file does not 
		// fit in memory so we use an embedded table.
		if (source.isEmpty()) throw new IllegalArgumentException("The add() method must be called to add some data before creating the database!");
		EXISTS: if (exists()) {
			if (isNewDatabase()) {
				Path db = Paths.get(dabasePath+".mv.db");
				Files.delete(db);
				break EXISTS;
			}
			logger.warn("The database "+dabasePath+" already exists and will not be recreated.");
			return;
		}
		createDatabase();
		parseSource();
	}

	/**
	 * Implement this method to create the database schema.
	 *
	 * @throws IOException
	 * @throws SQLException
	 */
	protected abstract void createDatabase() throws IOException, SQLException;


	/**
	 * Implement this method to create the database data.
	 *
	 * @throws IOException
	 * @throws SQLException
	 */
	protected abstract void parseSource() throws SQLException, ReaderException;

	
	protected Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:"+dabasePath+";mode=MySQL","sa","");
	}

	public boolean exists() {
		Path db = Paths.get(dabasePath+".mv.db");
		return Files.exists(db);
	}

	/**
	 * @return the newDatabase
	 */
	public boolean isNewDatabase() {
		return newDatabase;
	}

	/**
	 * @param newDatabase the newDatabase to set
	 */
	public void setNewDatabase(boolean newDatabase) {
		this.newDatabase = newDatabase;
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


	/**
	 * Set the location of the database. Sets the folder name.
	 * The actual database name is always the mapping file name with ".h2" appended.
	 * @param dir
	 */
	public void setLocation(Path dir) {
		String path = dir.toAbsolutePath().toString();
		this.dabasePath  = path+"/"+databaseFileName;
	}

}
