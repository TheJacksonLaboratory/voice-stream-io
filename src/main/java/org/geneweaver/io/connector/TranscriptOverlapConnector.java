package org.geneweaver.io.connector;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Species;
import org.geneweaver.domain.Transcript;
import org.geneweaver.domain.TranscriptOverlap;
import org.geneweaver.domain.Variant;

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
			ret.setVariant(variant);
			return (T) ret;
		}
		throw new IllegalArgumentException("Cannot intersect with "+loc);
	}
}
