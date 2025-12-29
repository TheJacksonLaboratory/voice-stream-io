package org.jax.voice.io.connector;

import java.util.HashMap;
import java.util.Map;

import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.EQTL;
import org.jax.voice.domain.EQTLOverlap;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Gene;
import org.jax.voice.domain.Located;
import org.jax.voice.domain.Species;
import org.jax.voice.domain.Variant;

/**
 * Used for the overlaps between eQTL and Gene.
 * eQTL is actually a relationship. 
 * 
 * @author gerrim
 *
 */
public class EQTLOverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	public EQTLOverlapConnector() {
		this("Homo sapiens", "eqtloverlaps");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public EQTLOverlapConnector(String species, String databaseFileName) {
		super(species);
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".csv.gz", ".csv");
	}
		
	@Override
	protected Located createIntersectionObject(Object id, int start, int end) {
		// We process the eQTLs for the location but use
		// the geneId for the id.
		if (id==null) return null;
		String geneId = (String)id;
		return new Gene(geneId, start, end);
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
		meta.put("species",eqtl.getSpecies());
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

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractEntity> T create(Located loc, Variant variant) {
		
		if (loc instanceof Gene) {
			EQTLOverlap ret = new EQTLOverlap();
			ret.setSpecies(Species.code(species));
			ret.setGene(loc);
			ret.setVariant(variant);
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}
}
