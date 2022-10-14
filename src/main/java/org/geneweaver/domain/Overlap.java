package org.geneweaver.domain;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonInclude;

@Generated("POJO")
@RelationshipEntity(type = "OVERLAP")
public class Overlap extends AbstractEntity {

	/** The variant. */
	@StartNode
	private Variant variant;
	private String variantId;

	
	/** The peak. */
	@EndNode
	private Region peak;
	private UUID peakId;

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
	private double intersectFraction;

	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append(":START_ID(Rs-Id)");
		buf.append(getDelimiter());
		buf.append("intersectRange:int");
		buf.append(getDelimiter());
		buf.append("intersectFraction:double");
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
		buf.append(getVariantId());
		buf.append(getDelimiter());
		buf.append(getIntersectRange());
		buf.append(getDelimiter());
		buf.append(getIntersectFraction());
		buf.append(getDelimiter());
		buf.append(getPeakId());
		buf.append(getDelimiter());
		buf.append("OVERLAP");
		return buf.toString();
	}

	/**
	 * @return the variant
	 */
	public Variant getVariant() {
		return variant;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setVariant(Variant variant) {
		this.variant = variant;
	}

	/**
	 * @return the peak
	 */
	public Region getPeak() {
		return peak;
	}

	/**
	 * @param peak the peak to set
	 */
	public void setPeak(Region peak) {
		this.peak = peak;
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
	public double getIntersectFraction() {
		return intersectFraction;
	}

	/**
	 * @param intersectFraction the intersectFraction to set
	 */
	public void setIntersectFraction(double intersectFraction) {
		this.intersectFraction = intersectFraction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(intersectFraction, intersectRange, peak, variant);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Overlap))
			return false;
		Overlap other = (Overlap) obj;
		return intersectFraction == other.intersectFraction && intersectRange == other.intersectRange
				&& Objects.equals(peak, other.peak) && Objects.equals(variant, other.variant);
	}

	/**
	 * @return the variantId
	 */
	public String getVariantId() {
		if (variantId!=null) return variantId;
		if (variant!=null) return variant.getRsId();
		return null;
	}

	/**
	 * @param variantId the variantId to set
	 */
	public void setVariantId(String variantId) {
		this.variantId = variantId;
	}

	/**
	 * @return the peakId
	 */
	public UUID getPeakId() {
		if (peakId!=null) return peakId;
		if (peak!=null) return peak.getPeakId();
		return null;
	}

	/**
	 * @param peakId the peakId to set
	 */
	public void setPeakId(UUID peakId) {
		this.peakId = peakId;
	}


}
