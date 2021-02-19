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

/**
 * 
I *think* EQTL is best as just a relationship. It could be either:
1. (Gene)-[EQTL]-(Variant)
2. (Gene)-[eLINK]-(EQTL)-[LOOKUP]-(Variant)

I.e. I think it should be 1. and not 2. but this is debatable. EQTL variant Id is not unique and for the
same eqtlVariantId different properties may exist.

Also shortening the chain means less nodes/rels. A disadvantage is that it is a different structure to Transcripts.

 * 
 * @author gerrim
 *
 */
@Generated("POJO")
@RelationshipEntity(type = "EQTL")
public class EQTL extends AbstractEntity {
	
    /** The chr. */
    private String chr;

	@StartNode
	private Variant variantFrom;
	private String rsId;
	
	/* Attributes required for biology:
	1. Directionality (slope of ref and alt)
	2. Strength p-value 
	3. Tissue type. Use Uberon to transform to standardised between mouse and human.
	4. Source
	*/
	private String refSeq;
	private String altSeq;
	private double slope;
	private String tissueFileName;
	private String tissueGroup;
	private String tissueName;
	private String version;
	private String uberon;
	private String source; // e.g. GTEx

	@EndNode
	private Gene geneTo;
	private String geneId;

	private String fullGeneId;

	// Just used to map rsId using EQTLFunction.
	private String eqtlVariantId;
	

	/**
	 * Instantiates a new ortholog.
	 */
	public EQTL() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new Homolog.
	 * This has only been tried from mouse, to human.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public EQTL(String geneId, String variantId, String rsId) {
		this.geneId = geneId;
		this.eqtlVariantId = variantId;
		this.rsId = rsId;
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append(":START_ID(Rs-Id)");
		
		buf.append(getDelimiter());
		buf.append("refSeq");
		buf.append(getDelimiter());
		buf.append("altSeq");
		buf.append(getDelimiter());
		buf.append("double:slope");
		buf.append(getDelimiter());
		buf.append("tissueFileName");
		buf.append(getDelimiter());
		buf.append("tissueGroup");
		buf.append(getDelimiter());
		buf.append("tissueName");
		buf.append(getDelimiter());
		buf.append("version");
		buf.append(getDelimiter());
		buf.append("uberon");
		buf.append(getDelimiter());
		buf.append("source");
		buf.append(getDelimiter());
		buf.append("fullGeneId");
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
		buf.append(getRsId());
		
		buf.append(getDelimiter());
		buf.append(getRefSeq());
		buf.append(getDelimiter());
		buf.append(getAltSeq());
		buf.append(getDelimiter());
		buf.append(getSlope());
		buf.append(getDelimiter());
		buf.append(getTissueFileName());
		buf.append(getDelimiter());
		buf.append(getTissueGroup());
		buf.append(getDelimiter());
		buf.append(getTissueName());
		buf.append(getDelimiter());
		buf.append(getVersion());
		buf.append(getDelimiter());
		buf.append(getUberon());
		buf.append(getDelimiter());
		buf.append(getSource());
		buf.append(getDelimiter());
		buf.append(getFullGeneId());
		buf.append(getDelimiter());
		
		buf.append(getGeneId());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
	}

	/**
	 * @return the variantId
	 */
	public String getEqtlVariantId() {
		return eqtlVariantId;
	}

	/**
	 * @param variantId the variantId to set
	 */
	public void setEqtlVariantId(String variantId) {
		this.eqtlVariantId = variantId;
	}

	/**
	 * @return the rsId
	 */
	public String getRsId() {
		if (rsId==null && variantFrom!=null) return variantFrom.getRsId();
		return rsId;
	}

	/**
	 * @param rsId the rsId to set
	 */
	public void setRsId(String rsId) {
		this.rsId = rsId;
	}

	/**
	 * @return the geneId
	 */
	public String getGeneId() {
		if (geneId==null && geneTo!=null) return geneTo.getGeneId();
		return geneId;
	}

	/**
	 * @param geneId the geneId to set
	 */
	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(altSeq, chr, eqtlVariantId, fullGeneId, geneId, geneTo, refSeq, rsId,
				slope, source, tissueFileName, tissueGroup, tissueName, uberon, variantFrom, version);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof EQTL))
			return false;
		EQTL other = (EQTL) obj;
		return Objects.equals(altSeq, other.altSeq) && Objects.equals(chr, other.chr)
				&& Objects.equals(eqtlVariantId, other.eqtlVariantId) && Objects.equals(fullGeneId, other.fullGeneId)
				&& Objects.equals(geneId, other.geneId) && Objects.equals(geneTo, other.geneTo)
				&& Objects.equals(refSeq, other.refSeq) && Objects.equals(rsId, other.rsId)
				&& Double.doubleToLongBits(slope) == Double.doubleToLongBits(other.slope)
				&& Objects.equals(source, other.source) && Objects.equals(tissueFileName, other.tissueFileName)
				&& Objects.equals(tissueGroup, other.tissueGroup)
				&& Objects.equals(tissueName, other.tissueName)
				&& Objects.equals(uberon, other.uberon) && Objects.equals(variantFrom, other.variantFrom)
				&& Objects.equals(version, other.version);
	}

	/**
	 * @return the variantFrom
	 */
	public Variant getVariantFrom() {
		return variantFrom;
	}

	/**
	 * @param variantFrom the variantFrom to set
	 */
	public void setVariantFrom(Variant variantFrom) {
		this.variantFrom = variantFrom;
	}

	/**
	 * @return the geneTo
	 */
	public Gene getGeneTo() {
		return geneTo;
	}

	/**
	 * @param geneTo the geneTo to set
	 */
	public void setGeneTo(Gene geneTo) {
		this.geneTo = geneTo;
	}

	/**
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}

	/**
	 * @param chr the chr to set
	 */
	public void setChr(String chr) {
		this.chr = chr;
	}

	/**
	 * @return the refSeq
	 */
	public String getRefSeq() {
		return refSeq;
	}

	/**
	 * @param refSeq the refSeq to set
	 */
	public void setRefSeq(String refSeq) {
		this.refSeq = refSeq;
	}

	/**
	 * @return the altSeq
	 */
	public String getAltSeq() {
		return altSeq;
	}

	/**
	 * @param altSeq the altSeq to set
	 */
	public void setAltSeq(String altSeq) {
		this.altSeq = altSeq;
	}

	/**
	 * @return the slope
	 */
	public double getSlope() {
		return slope;
	}

	/**
	 * @param slope the slope to set
	 */
	public void setSlope(double slope) {
		this.slope = slope;
	}

	/**
	 * @return the tissueFileName
	 */
	public String getTissueFileName() {
		return tissueFileName;
	}

	/**
	 * @param tissueFileName the tissueFileName to set
	 */
	public void setTissueFileName(String tissue) {
		this.tissueFileName = tissue;
	}

	/**
	 * @return the uberon
	 */
	public String getUberon() {
		return uberon;
	}

	/**
	 * @param uberon the uberon to set
	 */
	public void setUberon(String uberon) {
		this.uberon = uberon;
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
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the fullGeneId
	 */
	public String getFullGeneId() {
		return fullGeneId;
	}

	/**
	 * @param fullGeneId the fullGeneId to set
	 */
	public void setFullGeneId(String fullGeneId) {
		this.fullGeneId = fullGeneId;
	}

	/**
	 * @return the tissueGroup
	 */
	public String getTissueGroup() {
		return tissueGroup;
	}

	/**
	 * @param tissueGroup the tissueGroup to set
	 */
	public void setTissueGroup(String tissueGroup) {
		this.tissueGroup = tissueGroup;
	}

	/**
	 * @return the tissueName
	 */
	public String getTissueName() {
		return tissueName;
	}

	/**
	 * @param tissueName the tissueName to set
	 */
	public void setTissueName(String tissueSecondaryGroup) {
		this.tissueName = tissueSecondaryGroup;
	}


}
