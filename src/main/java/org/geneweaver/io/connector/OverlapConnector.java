package org.geneweaver.io.connector;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.Variant;
import org.neo4j.ogm.session.Session;

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
public class OverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	public OverlapConnector() {
		this("peaks");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public OverlapConnector(String databaseFileName) {
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".bed.gz", ".bed");
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
	
	// Every so often we print that overlaps are found in verbose mode.
	private volatile int count = 0;
	private int frequency = 10000;

	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> stream(N ent, Session session, PrintStream log) {
		
		// Other streams may run through this connector, but
		// if they send other objects, we return them.
		if (!(ent instanceof Variant)) return (Stream<E>) Stream.of(ent);
		Variant variant = (Variant)ent;
		
		String shardName = oservice.getShardName(variant.getChr(), variant.getStart());

		Collection<Entity> ret = new LinkedList<>();
		ret.add(variant);
		
		if (log!=null && count%frequency==0) {
			log.println("Using shard: "+shardName);
		}

		if (shardName!=null) {
	 		try {
				PreparedStatement lookup = getSelectStatement(variant.getChr(), shardName, log);
				if (lookup==null) { // Not all peaks have reasonable chromosomes.
					return (Stream<E>) ret.stream();
				}
				
				int vlower = Math.min(variant.getStart(), variant.getEnd());
				lookup.setInt(1, vlower);
				lookup.setInt(2, vlower);
				int vupper = Math.max(variant.getStart(), variant.getEnd());
				lookup.setInt(3, vupper);
				lookup.setInt(4, vupper);
	
				Set<String> usedIds = new HashSet<>();
				try (ResultSet res = lookup.executeQuery()) {
					while(res.next()) {
						String peakId = res.getString(1);
						if (usedIds.contains(peakId)) {
							logger.info("Encountered duplicate peakID: "+peakId);
							if (log!=null) log.println("Encountered duplicate peakID: "+peakId);
							continue;
						}
						int rlow = res.getInt(2);
						int rup  = res.getInt(3);
						
						Overlap o = oservice.intersection(variant, new Peak(peakId, rlow, rup));
						if (o!=null) {
							o.setChr(variant.getChr());
							ret.add(o);
							usedIds.add(peakId);
							
							if (log!=null && count%frequency==0) {
								log.println("Example of overlap found: "+o.toCsv());
							}
						}
					}
				}
				
			} catch (Exception ne) {
				logger.warn("Cannot map "+variant, ne);
			}
		}
		count++;
		
		return (Stream<E>) ret.stream();
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
			peak.setPeakId(UUID.randomUUID().toString());
			peak.setStart((int)(Math.random()*roughBPperChr));
			peak.setEnd((int)(Math.random()*roughBPperChr));
			peak.setChr(chr);
			store(peak, null, null);
			if (i%1000000 == 0) System.out.println("Added randoms, size "+i);
		} 
		return nrows;
	}

	/**
	 * @return the frequency
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}


}
