package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@Generated("POJO")
@RelationshipEntity(type = "REGULATORY_FEATURE_OVERLAP")
public class RegulatoryFeatureOverlap extends AbstractEntity {

	/** The variant. */
	@StartNode
	private Located regFeature;
	
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
		buf.append(":START_ID(Feature-Id)");
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
		buf.append(regFeature!=null?regFeature.id():"NA");
		buf.append(getDelimiter());
		buf.append(variant!=null?variant.id():"NA");
		buf.append(getDelimiter());
		buf.append("REGULATORY_FEATURE_OVERLAP");
		return buf.toString();
	}

	/**
	 * @return the variant
	 */
	public Located getRegFeature() {
		return regFeature;
	}

	/**
	 * @param variant the variant to set
	 */
	public void setRegFeature(Located rfeature) {
		this.regFeature = rfeature;
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

	@Override
	public String toString() {
		if (regFeature==null || variant==null) return super.toString();
		return "(Variant{rsId:"+variant.id()+"})-[REGULATORY_FEATURE_OVERLAP]-(RegulatoryFeature{featureId:"+regFeature.id()+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(regFeature, variant);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RegulatoryFeatureOverlap))
			return false;
		RegulatoryFeatureOverlap other = (RegulatoryFeatureOverlap) obj;
		return Objects.equals(regFeature, other.regFeature) && Objects.equals(variant, other.variant);
	}


}
