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
package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * The Class Gene. A class which holds the gene data in from the Ensembl data files.
 *
 * @author Matthew Gerring
 * @see https://dzone.com/articles/introduction-to-neo4j-ogm
 * @see https://neo4j.com/docs/ogm-manual/current/tutorial/
 */
@Generated("POJO")
@NodeEntity(label="Gene")
public class Gene extends GeneticEntity {
	
	/** The gene name. */
    private String geneName;
    
    /** The gene version. */
    private String geneVersion;
	
	/** The gene biotype. */
	private String geneBiotype;
	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append("geneId:ID(Gene-Id)");
		buf.append(getDelimiter());
		buf.append("geneName");
		buf.append(getDelimiter());
		buf.append("geneVersion");
		buf.append(getDelimiter());
		buf.append("geneBiotype");
		buf.append(getDelimiter());
		buf.append(super.getHeader());
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
		buf.append(getGeneId());
		buf.append(getDelimiter());
		buf.append(getGeneName());
		buf.append(getDelimiter());
		buf.append(getGeneVersion());
		buf.append(getDelimiter());
		buf.append(getGeneBiotype());
		buf.append(getDelimiter());
		buf.append(super.toCsv());
		return buf.toString();
	}

	// Auto-generated:
	/**
	 * Gets the gene name.
	 *
	 * @return the gene_name
	 */
	public String getGeneName() {
		return geneName;
	}


	/**
	 * Sets the gene name.
	 *
	 * @param gene_name the gene_name to set
	 */
	public void setGeneName(String gene_name) {
		this.geneName = gene_name;
	}

	/**
	 * Gets the gene biotype.
	 *
	 * @return the gene_biotype
	 */
	public String getGeneBiotype() {
		return geneBiotype;
	}


	/**
	 * Sets the gene biotype.
	 *
	 * @param gene_biotype the gene_biotype to set
	 */
	public void setGeneBiotype(String gene_biotype) {
		this.geneBiotype = gene_biotype;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(geneBiotype, geneName, geneVersion);
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Gene))
			return false;
		Gene other = (Gene) obj;
		return Objects.equals(geneBiotype, other.geneBiotype) && Objects.equals(geneName, other.geneName)
				&& Objects.equals(geneVersion, other.geneVersion);
	}

	/**
	 * Gets the gene version.
	 *
	 * @return the geneVersion
	 */
	public String getGeneVersion() {
		return geneVersion;
	}

	/**
	 * Sets the gene version.
	 *
	 * @param geneVersion the geneVersion to set
	 */
	public void setGeneVersion(String geneVersion) {
		this.geneVersion = geneVersion;
	}

    // Auto-generated stuff

	@Override
	public String id() {
		return getGeneId();
	}
 
}