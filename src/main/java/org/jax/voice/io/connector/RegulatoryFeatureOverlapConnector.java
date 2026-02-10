package org.jax.voice.io.connector;

import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Located;
import org.jax.voice.domain.RegulatoryFeature;
import org.jax.voice.domain.RegulatoryFeatureOverlap;
import org.jax.voice.domain.Species;
import org.jax.voice.domain.Variant;

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
public class RegulatoryFeatureOverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	public RegulatoryFeatureOverlapConnector() {
		this("Homo sapiens", "regfeats");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public RegulatoryFeatureOverlapConnector(String species, String databaseFileName) {
		super(species);
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".gff.gz", ".gff");
		setNewestInDirectoryByName(true);
	}
	
	
	@Override
	protected Located createIntersectionObject(Object id, int start, int end) {
		
		if (id==null) return null;
		String featureId = (String)id;
		return new RegulatoryFeature(featureId, start, end);
	}

	/**
	 * Implement to provide custom filtering to the input stream.
	 * @param loc
	 * @return
	 */
	@Override
	protected boolean filter(Located loc) {
		if (loc instanceof RegulatoryFeature) {
			return true;
		}
		return false;
	}

	@Override
	public <T extends AbstractEntity> T create(Located loc, Variant variant) {
		
		if (loc instanceof RegulatoryFeature) {
			RegulatoryFeatureOverlap ret = new RegulatoryFeatureOverlap();
			ret.setSpecies(Species.code(species));
			ret.setRegFeature(loc);
			ret.setChr(variant.getChr());
			ret.setVariant(variant);
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}

}
