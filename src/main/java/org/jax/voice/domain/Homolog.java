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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The Class Ortholog.
 */
@Generated("POJO")
@RelationshipEntity(type = "HOMOLOG")
public class Homolog extends AbstractEntity {
	
	private Long hid;

	/** The species from. */
	@StartNode
	private HomologGene speciesFrom;
	
	/** The species to. */
	@EndNode
	private HomologGene speciesTo;
	
	private String source;
	
	/** The gene name from. */
	private String geneIdFrom;
	
	/** The gene name to. */
	private String geneIdTo;
	

	/**
	 * Instantiates a new ortholog.
	 */
	public Homolog() {
		setChr(Entity.NO_CHR, true);
	}

	/**
	 * Instantiates a new Homolog.
	 * This has only been tried from mouse, to human.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public Homolog(Long hid, String geneIdFrom, String geneIdTo) {
		this();
		this.hid = hid;
		this.geneIdFrom = geneIdFrom;
		this.geneIdTo = geneIdTo;
	}

	/**
	 * Instantiates a new ortholog.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public Homolog(HomologGene from, HomologGene to) {
		this();
		this.speciesFrom = from;
		this.speciesTo = to;
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
		buf.append("homologId");
		buf.append(getDelimiter());
		buf.append("source");
		buf.append(getDelimiter());
		buf.append(":END_ID(Gene-Id)");
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
		buf.append(getGeneIdFrom());
		buf.append(getDelimiter());
		buf.append(getHid());
		buf.append(getDelimiter());
		buf.append(getSource());
		buf.append(getDelimiter());
		buf.append(getGeneIdTo());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
	}

	protected String getGeneIdFrom() {
		if (geneIdFrom==null && speciesFrom==null) return null;
		String id = geneIdFrom!=null ? geneIdFrom : speciesFrom.getGeneId();
		if (id==null) throw new IllegalArgumentException("The geneid must not be null!");
		return id;
	}
	
	protected String getGeneIdTo() {
		if (geneIdTo==null && speciesTo==null) return null;
		String id =  geneIdTo!=null ? geneIdTo : speciesTo.getGeneId();
		if (id==null) throw new IllegalArgumentException("The geneid must not be null!");
		return id;
	}

	/**
	 * Gets the species from.
	 *
	 * @return the speciesFrom
	 */
	public HomologGene getSpeciesFrom() {
		return speciesFrom;
	}

	/**
	 * Sets the species from.
	 *
	 * @param speciesFrom the speciesFrom to set
	 */
	public void setSpeciesFrom(HomologGene speciesFrom) {
		this.speciesFrom = speciesFrom;
	}

	/**
	 * Gets the species to.
	 *
	 * @return the speciesTo
	 */
	public HomologGene getSpeciesTo() {
		return speciesTo;
	}

	/**
	 * Sets the species to.
	 *
	 * @param speciesTo the speciesTo to set
	 */
	public void setSpeciesTo(HomologGene speciesTo) {
		this.speciesTo = speciesTo;
	}

	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return getGeneIdFrom()+"-["+getClass().getSimpleName().toUpperCase()+"]->"+getGeneIdTo();
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the hid
	 */
	public Long getHid() {
		return hid;
	}

	/**
	 * @param hid the hid to set
	 */
	public void setHid(Long hid) {
		this.hid = hid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(geneIdFrom, geneIdTo, hid, source, speciesFrom, speciesTo);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Homolog))
			return false;
		Homolog other = (Homolog) obj;
		return Objects.equals(geneIdFrom, other.geneIdFrom) && Objects.equals(geneIdTo, other.geneIdTo)
				&& Objects.equals(hid, other.hid) && Objects.equals(source, other.source)
				&& Objects.equals(speciesFrom, other.speciesFrom) && Objects.equals(speciesTo, other.speciesTo);
	}


	@JsonIgnore
	public void setChr(String chr) {
		super.setChr(chr);
	}

	@JsonIgnore
	public String getChr() {
		return super.getChr();
	}

}
