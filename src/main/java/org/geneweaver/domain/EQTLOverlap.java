package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonInclude;

@Generated("POJO")
@RelationshipEntity(type = "TRANSCRIPT_OVERLAP")
public class EQTLOverlap extends AbstractEntity {

	/** The variant. */
	@StartNode
	private Located gene;
	
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
		buf.append(":START_ID(Gene-Id)");
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
		buf.append(gene!=null?gene.id():"NA");
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
	public Located getGene() {
		return gene;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setGene(Located gene) {
		this.gene = gene;
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
		result = prime * result + Objects.hash(intersectFraction, intersectRange, variant, gene);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof EQTLOverlap))
			return false;
		EQTLOverlap other = (EQTLOverlap) obj;
		return intersectFraction == other.intersectFraction && intersectRange == other.intersectRange
				&& Objects.equals(variant, other.variant) && Objects.equals(gene, other.gene);
	}

	@Override
	public String toString() {
		if (gene==null || variant==null) return super.toString();
		return "(Variant{rsId:"+variant.id()+"})-[EQTL_OVERLAP]-(Gene{geneId:"+gene.id()+")";
	}


}
