package org.geneweaver.io.connector;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.RegulatoryFeature;
import org.geneweaver.domain.RegulatoryFeatureOverlap;
import org.geneweaver.domain.Variant;

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
		this("regfeats");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public RegulatoryFeatureOverlapConnector(String databaseFileName) {
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".gff.gz", ".gff");
		setNewestInDirectoryByName(true);
	}
	
	
	@Override
	protected Located createIntersectionObject(String id, int start, int end) {
		return new RegulatoryFeature(id, start, end);
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
			ret.setRegFeature(loc);
			ret.setVariant(variant);
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}

}
