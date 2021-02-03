package org.jax.gweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

@Generated("POJO")
public class ExperimentMetadata extends AbstractEntity {
	
	/** The uid. */
	@Id
	@GeneratedValue
    private Long uid;
	
    private String cellType;
    
    private String cellGroup;
    
    private String assayTarget;
    
    private String assay;
    
    private String reference;

    public ExperimentMetadata() {
    	
    }
    
	public ExperimentMetadata(String cellType, String cellGroup, String assayTarget, String assay, String reference) {
		this.cellType = cellType;
		this.cellGroup = cellGroup;
		this.assayTarget = assayTarget;
		this.assay = assay;
		this.reference = reference;
	}

	/**
	 * @return the cellType
	 */
	public String getCellType() {
		return cellType;
	}

	/**
	 * @param cellType the cellType to set
	 */
	public void setCellType(String cellType) {
		this.cellType = cellType;
	}

	/**
	 * @return the cellGroup
	 */
	public String getCellGroup() {
		return cellGroup;
	}

	/**
	 * @param cellGroup the cellGroup to set
	 */
	public void setCellGroup(String cellGroup) {
		this.cellGroup = cellGroup;
	}

	/**
	 * @return the assayTarget
	 */
	public String getAssayTarget() {
		return assayTarget;
	}

	/**
	 * @param assayTarget the assayTarget to set
	 */
	public void setAssayTarget(String assayTarget) {
		this.assayTarget = assayTarget;
	}

	/**
	 * @return the assay
	 */
	public String getAssay() {
		return assay;
	}

	/**
	 * @param assay the assay to set
	 */
	public void setAssay(String assay) {
		this.assay = assay;
	}

	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
	public int hashCode() {
		return Objects.hash(assay, assayTarget, cellGroup, cellType, reference, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExperimentMetadata))
			return false;
		ExperimentMetadata other = (ExperimentMetadata) obj;
		return Objects.equals(assay, other.assay) && Objects.equals(assayTarget, other.assayTarget)
				&& Objects.equals(cellGroup, other.cellGroup) && Objects.equals(cellType, other.cellType)
				&& Objects.equals(reference, other.reference) && Objects.equals(uid, other.uid);
	}

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}

}
