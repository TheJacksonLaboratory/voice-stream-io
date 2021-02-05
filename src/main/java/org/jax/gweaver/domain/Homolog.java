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
package org.jax.gweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * The Class Ortholog.
 */
@Generated("POJO")
@RelationshipEntity(type = "HOMOLOG")
public class Homolog extends AbstractEntity {
	
	/** The uid. */
	@Id
	@GeneratedValue
    private Long uid;
	
	private Long hid;

	/** The species from. */
	@StartNode
	private HomologGene speciesFrom;
	
	/** The species to. */
	@EndNode
	private HomologGene speciesTo;
	
	private String source;
	
	/** The gene name from. */
	private String geneNameFrom;
	
	/** The gene name to. */
	private String geneNameTo;
	
	private Long taxonFrom;
	
	private Long taxonTo;

	/**
	 * Instantiates a new ortholog.
	 */
	public Homolog() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new Homolog.
	 * This has only been tried from mouse, to human.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public Homolog(Long hid, Long taxonFrom, String from, Long taxonTo, String to) {
		this.hid = hid;
		this.taxonFrom = taxonFrom;
		this.geneNameFrom = from;
		this.taxonTo = taxonTo;
		this.geneNameTo = to;
	}

	/**
	 * Instantiates a new ortholog.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public Homolog(HomologGene from, HomologGene to) {
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
		// TODO Does not work, gene names not unique
		buf.append(":START_NAME(Gene-Taxon)");
		buf.append(getDelimiter());
		buf.append(":homologId");
		buf.append(getDelimiter());
		buf.append(":Source");
		buf.append(getDelimiter());
		buf.append(":END_NAME(Gene-Taxon)");
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
		buf.append(getGeneTaxonFrom());
		buf.append(getDelimiter());
		buf.append(getHid());
		buf.append(getDelimiter());
		buf.append(getSource());
		buf.append(getDelimiter());
		buf.append(getGeneTaxonTo());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
	}

	protected String getGeneTaxonFrom() {
		String name = speciesFrom!=null ? speciesFrom.getSymbol() : geneNameFrom;
		Long taxon = speciesFrom!=null ? speciesFrom.getTaxonId() : taxonFrom;
		return taxon+":"+name;
	}
	
	protected String getGeneTaxonTo() {
		String name = speciesTo!=null ? speciesTo.getSymbol() : geneNameTo;
		Long taxon = speciesTo!=null ? speciesTo.getTaxonId() : taxonTo;
		return taxon+":"+name;
	}


	/**
	 * Gets the uid.
	 *
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
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

	@Override
	public int hashCode() {
		return Objects.hash(geneNameFrom, geneNameTo, hid, source, speciesFrom, speciesTo, taxonFrom, taxonTo, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Homolog))
			return false;
		Homolog other = (Homolog) obj;
		return Objects.equals(geneNameFrom, other.geneNameFrom) && Objects.equals(geneNameTo, other.geneNameTo)
				&& Objects.equals(hid, other.hid) && Objects.equals(source, other.source)
				&& Objects.equals(speciesFrom, other.speciesFrom) && Objects.equals(speciesTo, other.speciesTo)
				&& Objects.equals(taxonFrom, other.taxonFrom) && Objects.equals(taxonTo, other.taxonTo)
				&& Objects.equals(uid, other.uid);
	}
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return getGeneTaxonFrom()+"-["+getClass().getSimpleName().toUpperCase()+"]->"+getGeneTaxonTo();
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



}
