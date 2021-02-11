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

// TODO: Auto-generated Javadoc
/**
 * The Class Ortholog.
 */
@Generated("POJO")
@RelationshipEntity(type = "ORTHOLOG")
public class Ortholog extends AbstractEntity {
	
	/** The uid. */
	@Id
	@GeneratedValue
    private Long uid;

	/** The species from. */
	@StartNode
	private Gene speciesFrom;
	
	/** The species to. */
	@EndNode
	private Gene speciesTo;
	
	/** The gene id from. */
	private String geneIdFrom;
	
	/** The gene id to. */
	private String geneIdTo;

	/**
	 * Instantiates a new ortholog.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public Ortholog(String from, String to) {
		geneIdFrom = from;
		geneIdTo = to;
	}

	/**
	 * Instantiates a new ortholog.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public Ortholog(Gene from, Gene to) {
		this.speciesFrom = from;
		this.speciesTo = to;
	}
	

	/**
	 * Instantiates a new ortholog.
	 */
	public Ortholog() {
		// TODO Auto-generated constructor stub
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
		buf.append(getSpeciesFromId());
		buf.append(getDelimiter());
		buf.append(getSpeciesToId());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
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
	public Gene getSpeciesFrom() {
		return speciesFrom;
	}

	/**
	 * Sets the species from.
	 *
	 * @param speciesFrom the speciesFrom to set
	 */
	public void setSpeciesFrom(Gene speciesFrom) {
		this.speciesFrom = speciesFrom;
	}

	/**
	 * Gets the species to.
	 *
	 * @return the speciesTo
	 */
	public Gene getSpeciesTo() {
		return speciesTo;
	}

	/**
	 * Sets the species to.
	 *
	 * @param speciesTo the speciesTo to set
	 */
	public void setSpeciesTo(Gene speciesTo) {
		this.speciesTo = speciesTo;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(uid);
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
		if (!(obj instanceof Ortholog))
			return false;
		Ortholog other = (Ortholog) obj;
		return Objects.equals(uid, other.uid);
	}
	
	protected String getSpeciesFromId() {
		return speciesFrom!=null ? speciesFrom.getGeneId() : geneIdFrom;
	}
	
	protected String getSpeciesToId() {
		return speciesTo!=null ? speciesTo.getGeneId() : geneIdTo;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return getSpeciesFromId()+"-[ORTHOLOG]->"+getSpeciesToId();
	}


}
