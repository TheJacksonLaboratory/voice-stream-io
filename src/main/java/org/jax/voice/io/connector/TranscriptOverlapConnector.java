package org.jax.voice.io.connector;

import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Located;
import org.jax.voice.domain.Species;
import org.jax.voice.domain.Transcript;
import org.jax.voice.domain.TranscriptOverlap;
import org.jax.voice.domain.Variant;

/**
 * Used for the overlaps between Variant and Transcript
 * 
 * @author gerrim
 *
 */
public class TranscriptOverlapConnector<N extends Entity, E extends Entity> extends AbstractOverlapConnector<N,E> {

	public TranscriptOverlapConnector() {
		this("Homo sapiens", "transcripts");
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public TranscriptOverlapConnector(String species, String databaseFileName) {
		super(species);
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setFileFilters(".gtf.gz", ".gtf");
	}
		
	@Override
	protected Located createIntersectionObject(Object id, int start, int end) {
		if (id==null) return null;
		String transId = (String)id;
		return new Transcript(transId, start, end);
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

	@Override
	public <T extends AbstractEntity> T create(Located loc, Variant variant) {
		
		if (loc instanceof Transcript) {
			TranscriptOverlap ret = new TranscriptOverlap();
			ret.setSpecies(Species.code(species));
			ret.setTranscript(loc);
			ret.setChr(variant.getChr());
			ret.setVariant(variant);
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}
}
