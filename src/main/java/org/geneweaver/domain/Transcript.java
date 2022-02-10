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

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

// TODO: Auto-generated Javadoc
/**
 * The Class Transcript.
 */
@Generated("POJO")
@NodeEntity(label="Transcript")
public class Transcript extends GeneticEntity {

	/** The transcript id. */
	@Index(unique=true)
	private String transcriptId;
	
    /** The transcript name. */
    private String transcriptName;
    
    /** The gene name. */
    private String geneName;
    
    /** The gene version. */
    private String geneVersion;
	
	/** The gene biotype. */
	private String geneBiotype;
    
    /** The transcript biotype. */
    private String transcriptBiotype;

	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append("transcriptId:ID(Transcript-Id)");
		buf.append(getDelimiter());
		buf.append("transcriptName");
		buf.append(getDelimiter());
		buf.append("geneId");
		buf.append(getDelimiter());
		buf.append("geneName");
		buf.append(getDelimiter());
		buf.append("geneVersion");
		buf.append(getDelimiter());
		buf.append("geneBiotype");
		buf.append(getDelimiter());
		buf.append("transcriptBiotype");
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
		buf.append(getTranscriptId());
		buf.append(getDelimiter());
		buf.append(getTranscriptName());
		buf.append(getDelimiter());
		buf.append(getGeneId());
		buf.append(getDelimiter());
		buf.append(getGeneName());
		buf.append(getDelimiter());
		buf.append(getGeneVersion());
		buf.append(getDelimiter());
		buf.append(getGeneBiotype());
		buf.append(getDelimiter());
		buf.append(getTranscriptBiotype());
		buf.append(getDelimiter());
		buf.append(super.toCsv());
		return buf.toString();
	}

	// Auto-generated
	/**
	 * Gets the transcript id.
	 *
	 * @return the transcript_id
	 */
	public String getTranscriptId() {
		return transcriptId;
	}


	/**
	 * Sets the transcript id.
	 *
	 * @param transcript_id the transcript_id to set
	 */
	public void setTranscriptId(String transcript_id) {
		this.transcriptId = transcript_id;
	}


	/**
	 * Gets the transcript name.
	 *
	 * @return the transcript_name
	 */
	public String getTranscriptName() {
		return transcriptName;
	}


	/**
	 * Sets the transcript name.
	 *
	 * @param transcript_name the transcript_name to set
	 */
	public void setTranscriptName(String transcript_name) {
		this.transcriptName = transcript_name;
	}


	/**
	 * Gets the transcript biotype.
	 *
	 * @return the transcript_biotype
	 */
	public String getTranscriptBiotype() {
		return transcriptBiotype;
	}


	/**
	 * Sets the transcript biotype.
	 *
	 * @param transcript_biotype the transcript_biotype to set
	 */
	public void setTranscriptBiotype(String transcript_biotype) {
		this.transcriptBiotype = transcript_biotype;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(geneBiotype, geneName, geneVersion, transcriptBiotype, transcriptId, transcriptName);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Transcript))
			return false;
		Transcript other = (Transcript) obj;
		return Objects.equals(geneBiotype, other.geneBiotype) && Objects.equals(geneName, other.geneName)
				&& Objects.equals(geneVersion, other.geneVersion)
				&& Objects.equals(transcriptBiotype, other.transcriptBiotype)
				&& Objects.equals(transcriptId, other.transcriptId)
				&& Objects.equals(transcriptName, other.transcriptName);
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
		return getTranscriptId();
	}

}