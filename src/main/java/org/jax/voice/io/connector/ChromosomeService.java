package org.jax.voice.io.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jax.voice.domain.Located;

/**
 * Standardizes chromosome e.g. removes chr etc.
 * to make all chromosome string values the same from
 * the different file formats.
 * @author gerrim
 *
 */
public class ChromosomeService {

	public static final String na = "NA"; // Chromosome NA or not specified.
	private static final String chromo = "(chr)?([0-9]{0,2}|x|y|m|(mt)|na)";
	private static final Pattern strictChromPattern = Pattern.compile("^("+chromo+")$");
	private static final Pattern chromPattern = Pattern.compile("^("+chromo+"|"+chromo+"_.*)$");
	private static final Map<String,String> chrCache = new HashMap<>();

	private static ChromosomeService staticInstance;
	public static ChromosomeService getInstance() {
		if (staticInstance==null) staticInstance = new ChromosomeService();
		return staticInstance;
	}
	
	private ChromosomeService() {
		
	}

	/**
	 * Returns null if the chromosome is not recognised.
	 * @param chr
	 * @return The chromosome number or letter if recognised. (e.g. 22, M, X etc.)
	 */
	public String getChromosome(String chr) {
		
		if (chr == null) return null;
		chr = chr.toLowerCase();
		if (chrCache.containsKey(chr)) {
			return chrCache.get(chr);
		}
		if (chr.isBlank()) return null;
		
		if (Boolean.getBoolean("strict")) {
			Matcher matcher = strictChromPattern.matcher(chr);
			if (matcher.matches()) {
				String lchr = matcher.group(3);
				if (lchr!=null && !lchr.isBlank()) {
					if ("mt".equals(lchr)) lchr = "m";
					lchr = lchr.toUpperCase();
					chrCache.put(chr, lchr);
					return lchr;
				}
			}
			chrCache.put(chr, null);
			return null;
		}
		
		Matcher matcher = chromPattern.matcher(chr);
		if (matcher.matches()) {
			String lchr = matcher.group(3);
			if (lchr==null || lchr.isBlank()) {
				lchr = matcher.group(6);
			}
			if (lchr==null || lchr.isBlank()) {
				chrCache.put(chr, null);
				return null;
			}
			int upos = lchr.indexOf('_');
			if (upos>0) lchr = lchr.substring(0, upos);
			if ("mt".equals(lchr)) lchr = "m";
			lchr = lchr.toUpperCase();
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
	public static boolean isValidChromosome(Located entity) {
		String chr = entity.getChr();
		return chr!=null;
	}

	public static void clearCache() {
		chrCache.clear();
	}

}
