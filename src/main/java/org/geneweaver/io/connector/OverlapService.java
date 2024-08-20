package org.geneweaver.io.connector;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.PeakOverlap;
import org.geneweaver.domain.RegulatoryFeature;
import org.geneweaver.domain.RegulatoryFeatureOverlap;
import org.geneweaver.domain.Transcript;
import org.geneweaver.domain.TranscriptOverlap;
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
	@SuppressWarnings("unchecked")
	public <T extends AbstractEntity> T intersection(Variant variant, Located loc) {
		
		int vs = Math.min(variant.getStart(), variant.getEnd());
		int ve = Math.max(variant.getStart(), variant.getEnd());
		
		int ps = Math.min(loc.getStart(), loc.getEnd());
		int pe = Math.max(loc.getStart(), loc.getEnd());
		
		// We rule out peaks of size 1
		// This is in an effort to reduce the number of hits.
		if (pe-ps <= 0) return null;

		// This is the part that weeds out non overlap peaks fast.
		if (ps>ve) return null;
		if (pe<vs) return null;
		
		// TODO There is probably some much better math for
		// finding the overlap of two lines but this is fast.
		int a = ps-vs;
		int b = ve-pe;
		if (a<0) a = 0;
		if (b<0) b = 0;
		
		int intersectRange = ve-vs-a-b;
		
		if (intersectRange<(minOverlap-1)) return null;
		
		float intersectFaction = intersectRange>0&&(ve-vs)>0
				                ? (float)intersectRange/(float)(ve-vs)
				                : 0f;

		if (loc instanceof Peak) {
			PeakOverlap ret = new PeakOverlap();
			ret.setPeak(loc);
			ret.setVariant(variant);
			ret.setIntersectRange(intersectRange);
			ret.setIntersectFraction(intersectFaction);
			
			return (T) ret;
			
		} else if (loc instanceof Transcript) {
			TranscriptOverlap ret = new TranscriptOverlap();
			ret.setTranscript(loc);
			ret.setVariant(variant);
			ret.setIntersectRange(intersectRange);
			ret.setIntersectFraction(intersectFaction);
			return (T) ret;
			
		} else if (loc instanceof RegulatoryFeature) {
			RegulatoryFeatureOverlap ret = new RegulatoryFeatureOverlap();
			ret.setRegFeature(loc);
			ret.setVariant(variant);
			ret.setIntersectRange(intersectRange);
			ret.setIntersectFraction(intersectFaction);
			return (T) ret;
		} else {
			throw new IllegalArgumentException("Unrecognised to location: "+loc);
		}
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
