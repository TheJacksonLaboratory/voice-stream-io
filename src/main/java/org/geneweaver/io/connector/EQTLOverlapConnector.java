package org.geneweaver.io.connector;

import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.EQTL;
import org.geneweaver.domain.EQTLOverlap;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Gene;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Variant;

/**
 * Used for the overlaps between eQTL and Gene.
 * eQTL is actually a relationship. 
 * 
 * @author gerrim
 *
 */
public class EQTLOverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	public EQTLOverlapConnector() {
		this("eqtloverlaps");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public EQTLOverlapConnector(String databaseFileName) {
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".csv.gz", ".csv");
	}
		
	@Override
	protected Located createIntersectionObject(String id, int start, int end) {
		// We process the eQTLs for the location but use
		// the geneId for the id.
		Gene gene = new Gene(id, start, end);
		// TODO
		// gene.setMeta(...)
		return gene;
	}

	/**
	 * Implement to provide custom filtering to the input stream.
	 * @param loc
	 * @return
	 */
	@Override
	protected boolean filter(Located loc) {
		// We process the eQTLs for the location but use
		// the geneId for the id.
		if (loc instanceof EQTL) {
			return true;
		}
		return false;
	}

	@Override
	public <T extends AbstractEntity> T create(Located loc, Variant variant, int intersectRange,
					float intersectFaction) {
		
		if (loc instanceof Gene) {
			EQTLOverlap ret = new EQTLOverlap();
			ret.setGene(loc);
			ret.setVariant(variant);
			ret.setIntersectRange(intersectRange);
			ret.setIntersectFraction(intersectFaction);
			
			// If the meta 
			Map<String,Object> meta = loc.getMeta();
			if (meta == null) {
				throw new IllegalArgumentException("The metadata values for EQTLOverlap must be set!");
			}
			
			// All the meta values must be fields in the EQTLOverlap bean.
			BeanMap map = new BeanMap(ret);
			map.putAll(map);
			
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}
}
