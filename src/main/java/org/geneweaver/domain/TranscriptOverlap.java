package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@Generated("POJO")
@RelationshipEntity(type = "TRANSCRIPT_OVERLAP")
public class TranscriptOverlap extends AbstractEntity {

	/** The variant. */
	@StartNode
	private Located transcript;
	
	/** The peak. */
	@EndNode
	private Located variant;

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append(":START_ID(Transcript-Id)");
		buf.append(getDelimiter());
		buf.append(":END_ID(Rs-Id)");
		buf.append(getDelimiter());
		buf.append(":TYPE");
		return buf.toString();
	}
	
	/**
	 * To csv.
	 *
	 * @return the string
	 */
	@Override
	public String toCsv() {
		StringBuilder buf = new StringBuilder();
		buf.append(transcript!=null?transcript.id():"NA");
		buf.append(getDelimiter());
		buf.append(variant!=null?variant.id():"NA");
		buf.append(getDelimiter());
		buf.append("TRANSCRIPT_OVERLAP");
		return buf.toString();
	}

	/**
	 * @return the variant
	 */
	public Located getTranscript() {
		return transcript;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setTranscript(Located transcript) {
		this.transcript = transcript;
	}

	/**
	 * @return the peak
	 */
	public Located getVariant() {
		return variant;
	}

	/**
	 * @param peak the peak to set
	 */
	public void setVariant(Located variant) {
		this.variant = variant;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(transcript, variant);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof TranscriptOverlap))
			return false;
		TranscriptOverlap other = (TranscriptOverlap) obj;
		return Objects.equals(transcript, other.transcript) && Objects.equals(variant, other.variant);
	}

	@Override
	public String toString() {
		if (transcript==null || variant==null) return super.toString();
		return "(Variant{rsId:"+variant.id()+"})-[TRANSCRIPT_OVERLAP]-(Transcript{transcriptId:"+transcript.id()+")";
	}


}
