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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class Ortholog.
 * 
Example of Orthologs from spreadsheet produced once via somewhat opaque mechanims
 at Baylor: (NOTE it is not clear why HGNC and MGI appear to swap columns.)
 
MGI:88282,Cbln2,ENSMUSG00000024647,HGNC:1544,CBLN2,ENSG00000141668
MGI:1098806,Commd2,ENSMUSG00000036513,HGNC:24993,COMMD2,ENSG00000114744
HGNC:18287,VPS37D,ENSMUSG00000043614,MGI:2159402,Vps37d,ENSG00000176428
MGI:1932682,Cxcl16,ENSMUSG00000018920,HGNC:16642,CXCL16,ENSG00000161921
HGNC:20837,PAOX,ENSMUSG00000025464,MGI:1916983,Paox,ENSG00000148832
MGI:88607,Cyp2e1,ENSMUSG00000025479,HGNC:2631,CYP2E1,ENSG00000130649
MGI:1919192,Myof,ENSMUSG00000048612,HGNC:3656,MYOF,ENSG00000138119
MGI:2141942,Ccp110,ENSMUSG00000033904,HGNC:24342,CCP110,ENSG00000103540

Other Orthologs are from the ENSEMBL web service.

 */
@Generated("POJO")
@RelationshipEntity(type = "ORTHOLOG")
public class Ortholog extends AbstractEntity {
	
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
	 * Source of the original link between genes.
	 * e.g. ENSEMBL, BAYLOR
	 */
	private String source;
	
	/** e.g. "MGI:3040707" appears as field value mgi=3040707 */
	private Integer mgi=-1;

	/** e.g. "HGNC:28841" appears as field value hgnc=28841 */
	private Integer hgnc=-1;

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
		buf.append("source");
		buf.append(getDelimiter());
		buf.append("mgi");
		buf.append(getDelimiter());
		buf.append("hgnc");
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
		buf.append(getSource());
		buf.append(getDelimiter());
		buf.append(getMgi());
		buf.append(getDelimiter());
		buf.append(getHgnc());
		buf.append(getDelimiter());
		buf.append(getSpeciesToId());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(geneIdFrom, geneIdTo, hgnc, mgi, source, speciesFrom, speciesTo);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Ortholog))
			return false;
		Ortholog other = (Ortholog) obj;
		return Objects.equals(geneIdFrom, other.geneIdFrom) && Objects.equals(geneIdTo, other.geneIdTo)
				&& Objects.equals(hgnc, other.hgnc) && Objects.equals(mgi, other.mgi)
				&& Objects.equals(source, other.source) && Objects.equals(speciesFrom, other.speciesFrom)
				&& Objects.equals(speciesTo, other.speciesTo);
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

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public Ortholog setSource(String source) {
		this.source = source;
		return this;
	}

	/**
	 * @return the mgi
	 */
	public Integer getMgi() {
		return mgi;
	}

	/**
	 * @param mgi the mgi to set
	 */
	public void setMgi(Integer mgi) {
		this.mgi = mgi;
	}

	/**
	 * @return the hgnc
	 */
	public Integer getHgnc() {
		return hgnc;
	}

	/**
	 * @param hgnc the hgnc to set
	 */
	public void setHgnc(Integer hgnc) {
		this.hgnc = hgnc;
	}

	/**
	 * @return the geneIdFrom
	 */
	@JsonIgnore
	public String getGeneIdFrom() {
		return geneIdFrom;
	}

	/**
	 * @return the geneIdTo
	 */
	@JsonIgnore
	public String getGeneIdTo() {
		return geneIdTo;
	}

}
