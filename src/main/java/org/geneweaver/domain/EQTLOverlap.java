package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Generated("POJO")
@RelationshipEntity(type = "EQTL_OVERLAP")
public class EQTLOverlap extends EQTLBase {

	/** The variant. */
	@StartNode
	private Located gene;
	
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
		
		return delimify(":START_ID(Rs-Id-"+getSpecies()+")",
				"chr",
				"tissueFileName",
				"tissueGroup",
				"tissueName",
				"uberon",
				"lod:double",
				"bp:int",
				"studyId",
				":END_ID(Gene-Id)",
				":TYPE");
	}
	
	/**
	 * To csv.
	 *
	 * @return the string
	 */
	@Override
	public String toCsv() {
		
		return delimify(variant!=null?variant.id():null,
						getChr(),
						getTissueFileName(),
						getTissueGroup(),
						getTissueName(),
						getUberon(),
						getLod()!=null?getLod():-1,
						getBp()!=null?getBp():-1,
						getStudyId(),
						gene!=null?gene.id():null,
						"EQTL_OVERLAP");
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(gene, variant);
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
		return Objects.equals(gene, other.gene) && Objects.equals(variant, other.variant);
	}

	@Override
	public String toString() {
		if (gene==null || variant==null) return super.toString();
		return "(Variant{rsId:"+variant.id()+"})-[EQTL_OVERLAP]-(Gene{geneId:"+gene.id()+")";
	}

	@JsonIgnore
	@Override
	public Object id() {
		return gene.id();
	}


	@JsonIgnore
	@Override
	public Integer getStart() {
		return getBp();
	}

	@JsonIgnore
	@Override
	public Integer getEnd() {
		return getBp();
	}


}
