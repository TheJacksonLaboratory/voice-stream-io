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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

// TODO: Auto-generated Javadoc
/**
 * The Class Produces.
 */
@Generated("POJO")
@RelationshipEntity(type = "PRODUCES")
public class Produces extends AbstractEntity {
	
	/** The gene. */
	@StartNode
	private Gene gene;
	
	/** The transcript. */
	@EndNode
	private Transcript transcript;

	/**
	 * Instantiates a new produces.
	 */
	public Produces() {
		
	}
	
	/**
	 * Instantiates a new produces.
	 *
	 * @param gene the gene
	 * @param transcript the transcript
	 */
	public Produces(Gene gene, Transcript transcript) {
		this.gene = gene;
		this.transcript = transcript;
	}

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
		buf.append(":END_ID(Transcript-Id)");
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
		buf.append(gene.getGeneId());
		buf.append(getDelimiter());
		buf.append(transcript.getTranscriptId());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
	}
	
	/**
	 * Gets the gene.
	 *
	 * @return the gene
	 */
	public Gene getGene() {
		return gene;
	}

	/**
	 * Sets the gene.
	 *
	 * @param gene the gene to set
	 */
	public void setGene(Gene gene) {
		this.gene = gene;
	}

	/**
	 * Gets the transcript.
	 *
	 * @return the transcript
	 */
	public Transcript getTranscript() {
		return transcript;
	}

	/**
	 * Sets the transcript.
	 *
	 * @param transcript the transcript to set
	 */
	public void setTranscript(Transcript transcript) {
		this.transcript = transcript;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(gene, transcript);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Produces))
			return false;
		Produces other = (Produces) obj;
		return Objects.equals(gene, other.gene) && Objects.equals(transcript, other.transcript);
	}
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		String geneId = gene!=null ? gene.getGeneId() : null;
		String transId = transcript!=null ? transcript.getTranscriptId() : null;
		return geneId+"-[PRODUCES]->"+transId;
	}


}
