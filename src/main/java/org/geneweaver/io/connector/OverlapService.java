package org.geneweaver.io.connector;

import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.CLI;

/**
 * This class contains the raw mathematics for an intersection,
 * however iterating all peaks (~100mill) for all variants (~1bill)
 * is not going to be scalable.
 * 
 * @author gerrim
 *
 */
public class OverlapService {
	
	private ChromosomeService cservice = ChromosomeService.getInstance();
	private static final int baseSize = Integer.parseInt(System.getenv().getOrDefault("BASE_SIZE", "100000"));
	static int minOverlap;

	static {
		// An overlap of 1 or more is allowed as an overlap.
		// Increasing this reduces the overlaps between variants and peaks which are large.
		String smin = CLI.get("MIN_OVERLAP", "min.overlap", "1");
		minOverlap = Integer.parseInt(smin);
	}
	/**
	 * Gets the intersection of the two objects. This
	 * is not designed to be run in an n*m loop, see above comment.
	 * However once you have two objects which might intersect, this
	 * can do the mathematics and return the Overlap which the correct
	 * fields set.
	 * a = p.s - v.s;
	 * a < 0 ? a = 0 : a=a;
	 * b = v.e - p.e;
	 * b < 0 ? b = 0 : b=b;
	 * bisectRange = v.e-v.s-a-b

	 * @param variant
	 * @param loc
	 * @return
	 * @throws OverlapException
	 */
	public <T extends AbstractEntity> T intersection(Variant variant, Located loc, 
											IntersectionCreator creator, Map<String,Object> meta) {
		
		int vs = Math.min(variant.getStart(), variant.getEnd());
		int ve = Math.max(variant.getStart(), variant.getEnd());
		
		int ps = Math.min(loc.getStart(), loc.getEnd());
		int pe = Math.max(loc.getStart(), loc.getEnd());
		
		// Does (a,b) bisect (lower,upper)?
		// (a <= upper) && (lower <= b);
		boolean intersects = vs <= pe && ps <= ve;
		if (!intersects) return null;
		
		T relationship = creator.create(loc, variant);
		BeanMap map = new BeanMap(relationship);
		map.putAll(meta);
		return relationship;
	}		
	
	/**
	 * Get the base of the location which is used for sharding.
	 * @param loc
	 * @return
	 */
	public int getShardBase(int loc) {
		return Math.round(loc/baseSize);
	}

	public String getShardName(String chr, int loc) {
		StringBuilder b = new StringBuilder();
		
		// Must have a valid chromosome for a shard.
		final String chrGood = cservice.getChromosome(chr);
		if (chrGood==null) return null;
		b.append("_");
		b.append(chrGood);
		b.append("_");
		b.append(getShardBase(loc));
		return b.toString();
	}

}
