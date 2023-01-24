package org.geneweaver.io.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Peak;
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
	
	private static final int baseSize = Integer.parseInt(System.getenv().getOrDefault("BASE_SIZE", "100000"));
	private static int minOverlap;

	static {
		// We do not currently make an overlap if it is 1 base pair long.
		// This is to reduce the overlaps which do not fit if we allow all the 
		// possible ones.
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
	 * @param peak
	 * @return
	 * @throws OverlapException
	 */
	public Overlap intersection(Variant variant, Peak peak) {
		
		int vs = Math.min(variant.getStart(), variant.getEnd());
		int ve = Math.max(variant.getStart(), variant.getEnd());
		
		int ps = Math.min(peak.getStart(), peak.getEnd());
		int pe = Math.max(peak.getStart(), peak.getEnd());
		
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

		Overlap ret = new Overlap();
		ret.setPeak(peak);
		ret.setVariant(variant);
		ret.setIntersectRange(intersectRange);
		ret.setIntersectFraction(intersectFaction);
		
		return ret;
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
		final String chrGood = getChromosome(chr);
		if (chrGood==null) return null;
		b.append("_");
		b.append(chrGood);
		b.append("_");
		b.append(getShardBase(loc));
		return b.toString();
	}
	
	private static final String chromo = "(chr[0-9]{0,2}X?Y?(MT)?)";
	private static final Pattern strictChromPattern = Pattern.compile("^("+chromo+")$");
	private static final Pattern chromPattern = Pattern.compile("("+chromo+"|"+chromo+"_.*)");
	private static final Map<String,String> chrCache = new HashMap<>();

	/**
	 * Returns null if the chromosome is not recognised.
	 * @param chr
	 * @return
	 */
	static String getChromosome(String chr) {
		
		if (chr == null) return null;
		if (chrCache.containsKey(chr)) return chrCache.get(chr);
		if (chr.length()<4) return null;
		
		if (Boolean.getBoolean("strict")) {
			Matcher matcher = strictChromPattern.matcher(chr);
			if (matcher.matches()) {
				String lchr = matcher.group(1);
				if (lchr!=null) {
					chrCache.put(chr, lchr);
					return lchr;
				}
			}
			chrCache.put(chr, null);
			return null;
		}
		
		Matcher matcher = chromPattern.matcher(chr);
		if (matcher.matches()) {
			String lchr = matcher.group(1);
			int upos = lchr.indexOf('_');
			if (upos>0) lchr = lchr.substring(0, upos);
			chrCache.put(chr, lchr);
			return lchr;
		}
		chrCache.put(chr, null);
		return null;
	}
	
	/**
	 * If we cannot figure out the chromo, do not use the peak.
	 * @param peak
	 * @return
	 */
	public static boolean isValidChromosome(Peak peak) {
		String chr = getChromosome(peak.getChr());
		return chr!=null;
	}

	public static void clearCache() {
		chrCache.clear();
	}
}
