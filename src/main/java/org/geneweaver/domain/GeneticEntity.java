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

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneticEntity.
 */
public abstract class GeneticEntity extends AbstractEntity implements Species {
	
	// May be unset for some implementors. Gene and 
	/** The gene id. */
	// Transcript must have a GeneId
	@Index(unique=true)
	private String geneId;

    /** The build. */
    private Long build = 0L;
    
    /** The active. */
    private Boolean active = true;
    
    /** The chr. */
    private String chr;
    
    /** The type. */
    private String type;
    
    /** The phase. */
    private String phase;
    
    /** The strand. */
    private String strand;
    
    /** The source. */
    private String source;
    
    /** The species. */
    private String species;
    
    /** The start. */
    private Integer start = 0;
    
    /** The end. */
    private Integer end = 0;
	
	/** The sequence id. */
	private String sequenceId;  // sequence ID stores chromosome
	
	/** The score. */
	private String score;   // TODO Should this be number?

	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append("active:boolean");
		buf.append(getDelimiter());
		buf.append("chr");
		buf.append(getDelimiter());
		buf.append("type");
		buf.append(getDelimiter());
		buf.append("phase");
		buf.append(getDelimiter());
		buf.append("strand");
		buf.append(getDelimiter());
		buf.append("source");
		buf.append(getDelimiter());
		buf.append("species");
		buf.append(getDelimiter());
		buf.append("start:int");
		buf.append(getDelimiter());
		buf.append("end:int");
		buf.append(getDelimiter());
		buf.append("sequenceId");
		buf.append(getDelimiter());
		buf.append("score");
		buf.append(getDelimiter());
		buf.append(":LABEL");
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
		buf.append(getActive().booleanValue());
		buf.append(getDelimiter());
		buf.append(getChr());
		buf.append(getDelimiter());
		buf.append(getType());
		buf.append(getDelimiter());
		buf.append(getPhase());
		buf.append(getDelimiter());
		buf.append(getStrand());
		buf.append(getDelimiter());
		buf.append(getSource());
		buf.append(getDelimiter());
		buf.append(getSpecies());
		buf.append(getDelimiter());
		buf.append(getStart());
		buf.append(getDelimiter());
		buf.append(getEnd());
		buf.append(getDelimiter());
		buf.append(getSequenceId());
		buf.append(getDelimiter());
		buf.append(getScore());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toString());
		return buf.toString();
	}

	/**
	 * Sets the builds the.
	 *
	 * @param build the build to set
	 */
	public void setBuild(Long build) {
		this.build = build;
	}

	/**
	 * Gets the active.
	 *
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Sets the active.
	 *
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * Gets the chr.
	 *
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}

	/**
	 * Sets the chr.
	 *
	 * @param chr the chr to set
	 */
	public void setChr(String chr) {
		this.chr = chr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(active, build, chr, end, geneId, phase, score, sequenceId, source,
				species, start, strand, type);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof GeneticEntity))
			return false;
		GeneticEntity other = (GeneticEntity) obj;
		return Objects.equals(active, other.active) && Objects.equals(build, other.build)
				&& Objects.equals(chr, other.chr) && Objects.equals(end, other.end)
				&& Objects.equals(geneId, other.geneId) && Objects.equals(phase, other.phase)
				&& Objects.equals(score, other.score) && Objects.equals(sequenceId, other.sequenceId)
				&& Objects.equals(source, other.source) && Objects.equals(species, other.species)
				&& Objects.equals(start, other.start) && Objects.equals(strand, other.strand)
				&& Objects.equals(type, other.type);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the phase.
	 *
	 * @return the phase
	 */
	public String getPhase() {
		return phase;
	}

	/**
	 * Sets the phase.
	 *
	 * @param phase the phase to set
	 */
	public void setPhase(String phase) {
		this.phase = phase;
	}

	/**
	 * Gets the strand.
	 *
	 * @return the strand
	 */
	public String getStrand() {
		return strand;
	}

	/**
	 * Sets the strand.
	 *
	 * @param strand the strand to set
	 */
	public void setStrand(String strand) {
		this.strand = strand;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * Sets the species.
	 *
	 * @param species the species to set
	 */
	public void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public Integer getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 *
	 * @param start the start to set
	 */
	public void setStart(Integer start) {
		this.start = start;
	}

	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	public Integer getEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 *
	 * @param end the end to set
	 */
	public void setEnd(Integer end) {
		this.end = end;
	}

	/**
	 * Gets the sequence id.
	 *
	 * @return the seq_id
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * Sets the sequence id.
	 *
	 * @param seq_id the seq_id to set
	 */
	public void setSequenceId(String seq_id) {
		this.sequenceId = seq_id;
	}

	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public String getScore() {
		return score;
	}

	/**
	 * Sets the score.
	 *
	 * @param score the score to set
	 */
	public void setScore(String score) {
		this.score = score;
	}

	/**
	 * Gets the gene id.
	 *
	 * @return the geneId
	 */
	public String getGeneId() {
		return geneId;
	}

	/**
	 * Sets the gene id.
	 *
	 * @param geneId the geneId to set
	 */
	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}

	/**
	 * @return the build
	 */
	public Long getBuild() {
		return build;
	}

}
