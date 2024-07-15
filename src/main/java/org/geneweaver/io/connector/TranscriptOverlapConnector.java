package org.geneweaver.io.connector;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Transcript;
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
public class TranscriptOverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	public TranscriptOverlapConnector() {
		this("transcripts");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public TranscriptOverlapConnector(String databaseFileName) {
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".gtf.gz", ".gtf");
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
						String transcriptId = res.getString(1);
						if (transcriptId==null) continue;
						if (usedIds.contains(transcriptId)) {
							logger.info("Encountered duplicate transcript geneId: "+transcriptId);
							continue;
						}
						int rlow = res.getInt(2);
						int rup  = res.getInt(3);
						
						if (log!=null && count%frequency==0) {
							log.println("Example of transcriptId found: "+transcriptId);
						}

						AbstractEntity o = oservice.intersection(variant, new Transcript(transcriptId, rlow, rup));
						if (o!=null) {
							o.setChr(variant.getChr());
							ret.add(o);
							usedIds.add(transcriptId);
							
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

	/**
	 * Implement to provide custom filtering to the input stream.
	 * @param loc
	 * @return
	 */
	@Override
	protected boolean filter(Located loc) {
		if (loc instanceof Transcript) {
			return true;
		}
		return false;
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
