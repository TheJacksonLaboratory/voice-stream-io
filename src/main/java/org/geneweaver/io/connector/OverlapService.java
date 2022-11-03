package org.geneweaver.io.connector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.Variant;

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

		if (ps>ve) return null;
		if (pe<vs) return null;
		
		int a = ps-vs;
		int b = ve-pe;
		if (a<0) a = 0;
		if (b<0) b = 0;
		
		int intersectRange = ve-vs-a-b;
		double intersectFaction = intersectRange>0&&(ve-vs)>0
				                ? intersectRange/(ve-vs)
				                : 0;
		
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
		final String chrGood = getChromosome(chr);
		b.append("_");
		b.append(chrGood);
		b.append("_");
		b.append(getShardBase(loc));
		return b.toString();
	}
	
	private static final String chromo = "(chr[0-9]{0,2}X?Y?(MT)?)";
	private static final Pattern chromPattern = Pattern.compile("("+chromo+"|"+chromo+"_.*)");

	/**
	 * Returns null if the chromosome is not recognised.
	 * @param chr
	 * @return
	 */
	String getChromosome(String chr) {
		if (chr.length()<4) return null;
		Matcher matcher = chromPattern.matcher(chr);
		if (matcher.matches()) {
			String lchr = matcher.group(1);
			int upos = lchr.indexOf('_');
			if (upos>0) lchr = lchr.substring(0, upos);
			return lchr;
		}
		return null;
	}
}
