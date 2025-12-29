/*-
 * 
 * Copyright 2018, 2020  The Jackson Laboratory Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Matthew Gerring
 */
package org.jax.voice.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

@Generated("POJO")
public class ExperimentMetadata extends AbstractEntity {
	
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(assay, assayTarget, cellGroup, cellType, reference);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ExperimentMetadata))
			return false;
		ExperimentMetadata other = (ExperimentMetadata) obj;
		return Objects.equals(assay, other.assay) && Objects.equals(assayTarget, other.assayTarget)
				&& Objects.equals(cellGroup, other.cellGroup) && Objects.equals(cellType, other.cellType)
				&& Objects.equals(reference, other.reference);
	}

}
