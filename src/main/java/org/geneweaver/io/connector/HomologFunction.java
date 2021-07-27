/*-
 * 
 * Copyright 2018, 2020  The Jackson Laboratory Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Matthew Gerring
 */
package org.geneweaver.io.connector;

import java.io.File;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Gene;
import org.geneweaver.domain.GeneticEntity;
import org.geneweaver.domain.HomologGene;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a function for mapping taxon+gene name to ensemble geneId.
 * 
 * Because the mapping is a map which is large to fit in memory, a h2
 * database is used to hold the map. This database will not be recreated 
 * if it exists (you must manually delete it) but it is mapping file specific
 * (different maps give different databases).
 * 
 * <code><pre>
 * 
 * HomologFunction func = new HomologFunction();
 * func.add(9606, new File("hg38_2.gtf"));
 * func.add(10090, new File("mm10_2.gtf"));
 * func.create(); // Creates indexed database.
 * 
 * </pre></code>
 * 
 * NOTE: This function does not need to create a database. Unlike EQTLFunction which is
 * too large to fit in memory, this one is only in the 100k-1m range, it would fit in memory.
 * Because the EQTLFunction database is working so well, we have used the same pattern here.
 * It has the minor advantage that the map is cached and does not have to be recomputed if 
 * we want to rewrite the bulk export files.
 * 
 * @author gerrim
 *
 * @param <N>
 * @param <E>
 */
public class HomologFunction<N extends HomologGene, E extends HomologGene> implements Function<N, E>, AutoCloseable {

	private static Logger logger = LoggerFactory.getLogger(HomologFunction.class);

	private static final String driver 		= System.getProperty("gweaver.gtex.mappingdb.driver", "org.h2.Driver");   
	private static final String tableName 	= System.getProperty("gweaver.gtex.mappingdb.tableName","IDMAPPING");

	private String dabasePath;

	private String databaseFileName;
	
	private Map<Integer, File> source = new HashMap<>();
	
	/**
	 * Set to avoid caching the database and always make a new one.
	 */
	private boolean newDatabase = false;

	public HomologFunction() {
		this("homologene.h2");
	}

	public HomologFunction(String databaseFileName) {
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
	 * Set the location of the database. Sets the folder name.
	 * The actual database name is always the mapping file name with ".h2" appended.
	 * @param dir
	 */
	public void setLocation(Path dir) {
		String path = dir.toAbsolutePath().toString();
		this.dabasePath  = path+"/"+databaseFileName;
	}

	private Connection connection;
	private PreparedStatement lookup;
	
	/**
	 * You must call create() to set up the database.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E apply(N t) {
		
		// We are setting the rsId. If it was found
		// already, our work here is done.
		if (t.getGeneId()!=null) {
			return (E)t;
		}
		
		if (connection==null) {
			try {
				connection = createConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		
		// Map the rsId
		String geneNameKey = t.getGeneNameKey().toLowerCase();
		try {
			if (lookup==null) lookup = connection.prepareStatement("SELECT geneId FROM "+tableName+" WHERE geneNameKey = ?;");
			
			lookup.setString(1, geneNameKey);
			try (ResultSet res = lookup.executeQuery()) {
				res.next();
				String geneId = res.getString(1);
				t.setGeneId(geneId);
			}
			
		} catch (SQLException ne) {
			logger.warn("Cannot map "+geneNameKey, ne);
			return (E)t;
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
		createMappingDatabase();
		parseSource();
	}
	
	private void parseSource() throws SQLException, ReaderException {

		try (Connection conn = createConnection();
			 PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+tableName+" (geneNameKey, geneId) VALUES (?,?) ON DUPLICATE KEY UPDATE id=id;") ) {  

			for (Integer taxon : source.keySet()) {

				File file = source.get(taxon);
				StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(String.valueOf(taxon), file));
				reader.stream()
					  .filter(g->g instanceof Gene)
					  .forEach(ge -> storeGene(ge, stmt, taxon));
			} 
		}
	}

	private void storeGene(Entity ge, PreparedStatement stmt, int taxon) {
		try {
			Gene gene = (Gene)ge;
			
			// Put the key in, lower case.
			if (gene.getGeneName()==null) return; // We canot mapp unnamed genes.
			String lcName = gene.getGeneName().toLowerCase();
			stmt.setString(1, taxon+":"+lcName);						
			stmt.setString(2, gene.getGeneId());
			stmt.execute();
			
			if (lcName.contains(".")) {
				String notDot = lcName.substring(0, lcName.lastIndexOf('.'));					
				stmt.setString(1, taxon+":"+notDot);						
				stmt.setString(2, gene.getGeneId());
				stmt.execute();
			}
		} catch (Exception ne) {
			ne.printStackTrace();
			throw new RuntimeException(ne);
		}
	}

	private void createMappingDatabase() throws IOException, SQLException {

		try (Connection conn = createConnection();
			 Statement stmt = conn.createStatement() ) {  

			String sql =  "CREATE TABLE " + tableName + 
						" (id int NOT NULL AUTO_INCREMENT, " + 
						// Important UNIQUE means there is an index and
						// that the later lookup will be fast.
						" geneNameKey VARCHAR(64) NOT NULL UNIQUE, " +  
						" geneId VARCHAR(64));"; 

			stmt.executeUpdate(sql);
			logger.info("Created table IDMAPPING");
		}
	}

	private Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:"+dabasePath+";mode=MySQL","sa","");
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
}
