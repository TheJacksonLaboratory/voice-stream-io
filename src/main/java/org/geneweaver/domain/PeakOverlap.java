package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@Generated("POJO")
@RelationshipEntity(type = "PEAK_OVERLAP")
public class PeakOverlap extends AbstractEntity {

	/** The variant. */
	@StartNode
	private Located variant;
	
	/** The peak. */
	@EndNode
	private Located peak;

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append(":START_ID(Rs-Id-"+getSpecies()+")");
		buf.append(getDelimiter());
		buf.append(":END_ID(Peak-Id)");
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
		buf.append(variant!=null?variant.id():"NA");
		buf.append(getDelimiter());
		buf.append(peak!=null?peak.id():"NA");
		buf.append(getDelimiter());
		buf.append("PEAK_OVERLAP");
		return buf.toString();
	}

	/**
	 * @return the variant
	 */
	public Located getVariant() {
		return variant;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setVariant(Located variant) {
		this.variant = variant;
	}

	/**
	 * @return the peak
	 */
	public Located getPeak() {
		return peak;
	}

	/**
	 * @param peak the peak to set
	 */
	public void setPeak(Located peak) {
		this.peak = peak;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(peak, variant);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof PeakOverlap))
			return false;
		PeakOverlap other = (PeakOverlap) obj;
		return Objects.equals(peak, other.peak) && Objects.equals(variant, other.variant);
	}

	@Override
	public String toString() {
		if (variant==null || peak==null) return super.toString();
		return "(Variant{rsId:"+variant.id()+"})-[PEAK_OVERLAP]-(Peak{peakId:"+peak.id()+")";
	}


}
