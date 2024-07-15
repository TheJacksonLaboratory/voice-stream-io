package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonInclude;

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
	 * A scalar which is the amount of overlap between the variant and the park.
	 * a = p.s - v.s;
	 * a < 0 ? a = 0 : a=a;
	 * b = v.e - p.e;
	 * b < 0 ? b = 0 : b=b;
	 * intersectRange = v.e-v.s-a-b
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private int intersectRange;

	/**
	 * The faction of the peak which overlaps the original variant location.
	 * intersectFraction = intersectRange / (v.e-v.s)
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private float intersectFraction;

	
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
		buf.append("intersectRange:int");
		buf.append(getDelimiter());
		buf.append("intersectFraction:float");
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
		buf.append(getIntersectRange());
		buf.append(getDelimiter());
		buf.append(getIntersectFraction());
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
	public void setTranscript(Located variant) {
		this.transcript = variant;
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
	public void setVariant(Located peak) {
		this.variant = peak;
	}

	/**
	 * @return the intersectRange
	 */
	public int getIntersectRange() {
		return intersectRange;
	}

	/**
	 * @param intersectRange the intersectRange to set
	 */
	public void setIntersectRange(int intersectRange) {
		this.intersectRange = intersectRange;
	}

	/**
	 * @return the intersectFraction
	 */
	public float getIntersectFraction() {
		return intersectFraction;
	}

	/**
	 * @param intersectFraction the intersectFraction to set
	 */
	public void setIntersectFraction(float intersectFraction) {
		this.intersectFraction = intersectFraction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(intersectFraction, intersectRange, variant, transcript);
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
		return intersectFraction == other.intersectFraction && intersectRange == other.intersectRange
				&& Objects.equals(variant, other.variant) && Objects.equals(transcript, other.transcript);
	}

	@Override
	public String toString() {
		if (transcript==null || variant==null) return super.toString();
		return "(Variant{rsId:"+transcript.id()+"})-[PEAK_OVERLAP]-(Peak{peakId:"+variant.id()+")";
	}


}
