package org.geneweaver.io.connector;

import java.util.Map;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Gene;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.ReaderRequest;
import org.neo4j.ogm.session.Session;

/**
 * Class to create step file connections.
 * This class parses the special tss file and the variant file which come from CCSI.
 * Then we are able to look up locations in each step file as we do when we parse peaks
 * by location. This is not a fast process.
 * 
 * @author gerrim
 *
 */
public class StepConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E>  {

	/**
	 * Some of the input files are heterogeneous and
	 * we only want one entity such as "Gene" from the file.
	 */
	private final Class<N> clazz;

	public StepConnector(Class<N> clazz) {
		this(clazz, clazz.getSimpleName()); // Or variants, we have to process both.
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public StepConnector(Class<N> clazz, String databaseFileName) {
		this.clazz = clazz;
		this.tableName = System.getProperty("gweaver.mappingdb.tableName","REGIONS");
		this.fileName = databaseFileName;
	}
	
	/**
	 * Override for readers which read file formats whose objects
	 * do not fit a normal read and need mapping to use with the connector.
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Located coerce(Object e) {
		if (clazz==Variant.class && e instanceof Map) {
			return Entity.coerce((Map<String,Object>)e, new Variant());
		}
		if (clazz==Gene.class && e instanceof Gene) {
			fixId((Gene)e);
		}
		return (Located)e;
	}
	
	@Override
	protected void configure(ReaderRequest request) {
		if (clazz == Variant.class) {
			request.setDelimiter("\t");
			request.setIncludeAll(false);
		}
	}

	/**
	 * Override to filter class
	 * @param l
	 * @return true if class type is valid.
	 */
	protected boolean isValidClass(Object l) {
		return l.getClass()==clazz;
	}

	/**
	 * Method which gets the connections between Gene and Variant
	 * using this dataset which are known as CONTACT
	 */
	@Override
	public Stream<E> stream(N entity, Session session) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Gene fixId(Gene g) {
		String geneId = g.getGeneId();
		if (geneId.contains(".")) {
			geneId = geneId.substring(0, geneId.indexOf('.'));
			g.setGeneId(geneId);
		}
		return g;
	}
}
