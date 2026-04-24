package org.jax.voice.io.connector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Located;
import org.jax.voice.domain.Peak;
import org.jax.voice.domain.PeakOverlap;
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
public class PeakOverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	private boolean allowNulls    = Boolean.getBoolean("org.jax.voice.io.connector.ALLOW_NULL_IN_PEAKID");
	private boolean allowNoTissue = Boolean.parseBoolean(System.getProperty("org.jax.voice.io.connector.ALLOW_NOTISSUE_IN_PEAKID", "true"));
	
	private String peakFeatureFilter = null;
	
	private Path tissueList;
	private int tissueLevel=3;
	
	public PeakOverlapConnector() {
		this("Homo sapiens", "peaks");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public PeakOverlapConnector(String species, String databaseFileName) {
		super(species, Long.class);
		setTableName(System.getProperty("gweaver.mappingdb.tableName","PEAK_REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".bed.gz", ".bed");
		setNewestInDirectoryByName(true);
	}
	
	/**
	 * Adds all the bed.gz files to be cached recursively.
	 * Stopping if the limit is reached (reduces total files for testing).
	 * @param dir
	 * @param limit
	 * @throws IOException 
	 */
	@Override
	Collection<Path> addAll(Path dir, int limit) throws IOException {
		super.addAll(dir, limit);
		this.source = removeOlderNames(source);
		return source;
	}

	// e.g.
	// mus_musculus.GRCm39.forebrain_embryonic_10_5_days.H3K36me3.ccat_histone.peaks.20201003.bed.gz
	// mus_musculus.GRCm39.forebrain_embryonic_10_5_days.H3K36me3.ccat_histone.peaks.20201021.bed.gz
	private static final Pattern datedName = Pattern.compile("^(.*)\\.peaks\\.(\\d+)\\.bed\\.gz$");
	/**
	 * The paths are sorted. Remove the older ones in the sorted stack.
	 * @param source2
	 */
	private Collection<Path> removeOlderNames(Collection<Path> paths) {
		
		List<Path> rev = new ArrayList<>(paths);
		
		// Review of the sorted order works because the file name ends with the numeric date.
		// Reverse puts the older ones later.
		Collections.reverse(rev);
		
		// Hold the stub names we have checked.
		Collection<String> checked = new HashSet<>();
		for (Iterator<Path> it = rev.iterator(); it.hasNext();) {
			Path path = it.next();
			String fileName = path.getFileName().toString();
			Matcher matcher = datedName.matcher(fileName);
			if (matcher.matches()) {
				String stub = matcher.group(1);
				if (checked.contains(stub)) {
					it.remove(); // Older duplicate removed.
					continue;
				}
				checked.add(stub);
			}
		}
		return rev;
	}
	
	@Override
	protected Located createIntersectionObject(Object id, int start, int end) {
		Long lid = id instanceof Long ? (Long)id : Long.valueOf(id.toString());
		return new Peak(lid, start, end);
	}

	@Override
	protected boolean testId(Object peakId) {
		if (!allowNulls && (peakId==null || "null".equals(peakId))) { // One of the properties making up the id is unset.
			logger.info("Peak missing information: "+peakId);
			return false;
		}
//		if (!allowNoTissue && peakId.endsWith("-t")) { // No tissue identified
//			logger.info("Peak missing tissue information: "+peakId);
//			return false;
//		}
		return true;
	}
	
	/**
	 * Implement to provide custom filtering to the input stream.
	 * @param loc
	 * @return
	 */
	@Override
	protected boolean filter(Located loc) {
		if (loc instanceof Peak) {
			Peak p = (Peak)loc;
			return filter(p, peakFeatureFilter); 
		}
		return true;
	}
	
	/**
	 * Call to filter a peak by feature type, useful used in a stream.
	 * @param p
	 * @param peakFeatureFilter
	 * @return
	 */
	public static boolean filter(Peak p, String peakFeatureFilter) {
		if (peakFeatureFilter==null) return true;
		if (p.getFeatureType()==null && peakFeatureFilter!=null) {
			return false; // If they have no feature and we should filter the features, we do not want this one.
		}
		if (p.getFeatureType()!=null && peakFeatureFilter!=null) {
			if (p.getFeatureType().equalsIgnoreCase(peakFeatureFilter)) return true;
			return p.getFeatureType().matches(peakFeatureFilter); // Might be false
		}
		return true;
	}

	private long roughBPperChr = 200000000;
	
	/**
	 * Method used to add random rows to the database.
	 * 
	 * @param nrows
	 * @throws SQLException 
	 */
	int testAddRandomRows(String chr, int nrows) throws SQLException {
		
		for (int i = 0; i < nrows; i++) {

			Peak peak = new Peak();
			peak.setPeakId(Math.round(Math.random()*Integer.MAX_VALUE));
			peak.setStart((int)(Math.random()*roughBPperChr));
			peak.setEnd((int)(Math.random()*roughBPperChr));
			peak.setChr(chr);
			store(peak, null, null);
			if (i%1000000 == 0) System.out.println("Added randoms, size "+i);
		} 
		return nrows;
	}
	
	/**
	 * @return the allowNulls
	 */
	public boolean isAllowNulls() {
		return allowNulls;
	}

	/**
	 * @param allowNulls the allowNulls to set
	 */
	public void setAllowNulls(boolean allowNulls) {
		this.allowNulls = allowNulls;
	}

	/**
	 * @return the allowNoTissue
	 */
	public boolean isAllowNoTissue() {
		return allowNoTissue;
	}

	/**
	 * @param allowNoTissue the allowNoTissue to set
	 */
	public void setAllowNoTissue(boolean allowNoTissue) {
		this.allowNoTissue = allowNoTissue;
	}

	/**
	 * @return the peakFeatureFilter
	 */
	public String getPeakFeatureFilter() {
		return peakFeatureFilter;
	}

	/**
	 * @param peakFeatureFilter the peakFeatureFilter to set
	 */
	public void setPeakFeatureFilter(String peakFeatureFilter) {
		this.peakFeatureFilter = peakFeatureFilter;
	}

	@Override
	public <T extends AbstractEntity> T create(Located loc, 
											Variant variant) {
		
		if (loc instanceof Peak) {
			PeakOverlap ret = new PeakOverlap();
			ret.setSpecies(Species.code(species));
			ret.setPeak(loc);
			ret.setChr(variant.getChr());
			ret.setVariant(variant);
			return (T) ret;
		} 
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}

	// Split on commas that are not inside double-quotes.
	// Good enough for "one record per line" CSV (no embedded newlines).
	private static final Pattern CSV_SPLIT_COMMA_OUTSIDE_QUOTES =
			Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

	private static String csvUnquote(String s) {
		if (s == null) return null;
		s = s.trim();
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
			// CSV escaping for quotes is doubled quotes: "" -> "
			s = s.replace("\"\"", "\"");
		}
		return s;
	}

	private static String[] splitCsvLineLoose(String line) {
		// Preserve trailing empties, since some rows might omit optional columns.
		String[] parts = CSV_SPLIT_COMMA_OUTSIDE_QUOTES.split(line, -1);
		for (int i = 0; i < parts.length; i++) {
			parts[i] = csvUnquote(parts[i]);
		}
		return parts;
	}
	
	protected Predicate<Path> cachedPathFilter=null;
	
	protected Predicate<Path> getPathFilter() {
		
		if (cachedPathFilter!=null) return cachedPathFilter;
		if (tissueList==null) return super.getPathFilter();
		if (!Files.exists(tissueList)) return super.getPathFilter();
		
		try {
			List<String> tissues = Files.lines(tissueList)
									.map(String::trim)	
									.filter(t -> !t.isBlank())
									.filter(t -> !t.startsWith("#"))
									// Some lines use quotes e.g. astrocyte,"Astrocytes (brain, unspecified region)",1
									.map(PeakOverlapConnector::splitCsvLineLoose)
									.filter(sa -> Integer.parseInt(sa[2]) <= tissueLevel)
									.map(sa -> "/"+sa[0]+"/")
									.toList();
			cachedPathFilter = p -> {
				String fileName = p.toString();
				for (String tissue : tissues) {
					if (fileName.contains(tissue)) return true;
				}
				return false;
			};
			return cachedPathFilter;
		} catch (IOException e) {
			logger.error("Error reading tissue list "+tissueList, e);
			return super.getPathFilter();
		}
	}

	/**
	 * 
	 * @return the tissueList
	 */
	public Path getTissueList() {
		return tissueList;
	}

	/**
	 * Set the tissue list before calling addAll because
	 * the list is used to filter the peaks as they are added. 
	 * If the list is not set, then all peaks are added.
	 * @param tissueList the tissueList to set
	 */
	public void setTissueList(Path tissueList) {
		this.tissueList = tissueList;
	}

	/**
	 * @return the tissueLevel
	 */
	public int getTissueLevel() {
		return tissueLevel;
	}

	/**
	 * @param tissueLevel the tissueLevel to set
	 */
	public void setTissueLevel(int tissueLevel) {
		this.tissueLevel = tissueLevel;
		this.cachedPathFilter=null;
	}

}
