package org.geneweaver.io.connector;

import java.util.HashMap;
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
		return new Gene(id, start, end);
	}
	
	/**
	 * Specify the parameters on EQTL which we read from the original file
	 * and then save on EQTLOverlap later using a bean map.
	 */
	protected <T extends Located> Map<String, Object> getMeta(T line) {
		EQTL eqtl = (EQTL)line;
		Map<String, Object> meta = new HashMap<>();
		meta.put("chr",  eqtl.getChrGRCm39());
		meta.put("bp",   eqtl.getBpGRCm39());
		meta.put("lod",  eqtl.getLod());
		meta.put("tissueFileName",eqtl.getTissueFileName());
		meta.put("tissueGroup",eqtl.getTissueGroup());
		meta.put("tissueName",eqtl.getTissueName());
		meta.put("uberon",eqtl.getUberon());
		meta.put("studyId",eqtl.getStudyId());
		return meta;
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
	public <T extends AbstractEntity> T create(Located loc, Variant variant) {
		
		if (loc instanceof Gene) {
			EQTLOverlap ret = new EQTLOverlap();
			ret.setGene(loc);
			ret.setVariant(variant);
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}
}
