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
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class EQTL extends EQTLBase {
	
	public enum Type {
		PEAK, INTERVAL;
	}
	
	private Type type=Type.PEAK; // PEAK or INTERVAL
    private String marker;
    private String population;

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
	private Double pos;
	private String version;
	private String source; // e.g. GTEx
	
	private String  chrGRCm39;
	private Integer bpGRCm39;
	
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
		
		return delimify(":START_ID(Rs-Id)",
				"type",
				"chr",
				"slope:double",
				"tissueFileName",
				"tissueGroup",
				"tissueName",
				"version",
				"uberon",
				"source",
				"marker",
				"lod:double",
				"bp:int",
				":END_ID(Gene-Id)",
				":TYPE");
	}
	
	/**
	 * To csv.
	 *
	 * @return the string
	 */
	@Override
	public String toCsv() {
		
		return delimify(getRsId(),
				getType(),
				getChrGRCm39()!=null?getChrGRCm39():getChr(),
				getSlope(),
				getTissueFileName(),
				getTissueGroup(),
				getTissueName(),
				getVersion(),
				getUberon(),
				getSource(),
				getMarker(),
				getLod()!=null?getLod():-1,
				getBpGRCm39()!=null?getBpGRCm39():-1,
				getGeneId(),
				getClass().getSimpleName().toUpperCase());
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
		result = prime * result + Objects.hash(altSeq, bpGRCm39, chrGRCm39, eqtlVariantId, fullGeneId, geneId, geneTo,
				marker, population, pos, refSeq, rsId, slope, source, type, variantFrom, version);
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
		return Objects.equals(altSeq, other.altSeq) && Objects.equals(bpGRCm39, other.bpGRCm39)
				&& Objects.equals(chrGRCm39, other.chrGRCm39) && Objects.equals(eqtlVariantId, other.eqtlVariantId)
				&& Objects.equals(fullGeneId, other.fullGeneId) && Objects.equals(geneId, other.geneId)
				&& Objects.equals(geneTo, other.geneTo) && Objects.equals(marker, other.marker)
				&& Objects.equals(population, other.population) && Objects.equals(pos, other.pos)
				&& Objects.equals(refSeq, other.refSeq) && Objects.equals(rsId, other.rsId)
				&& Double.doubleToLongBits(slope) == Double.doubleToLongBits(other.slope)
				&& Objects.equals(source, other.source) && type == other.type
				&& Objects.equals(variantFrom, other.variantFrom) && Objects.equals(version, other.version);
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


	public String getMarker() {
		return marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}

	public String getPopulation() {
		return population;
	}

	public void setPopulation(String population) {
		this.population = population;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the pos
	 */
	public Double getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Double pos) {
		this.pos = pos;
	}

	/**
	 * @return the chrGRCm39
	 */
	public String getChrGRCm39() {
		return chrGRCm39;
	}

	/**
	 * @param chrGRCm39 the chrGRCm39 to set
	 */
	public void setChrGRCm39(String chrGRCm39) {
		this.chrGRCm39 = chrGRCm39;
	}

	/**
	 * @return the bpGRCm39
	 */
	public Integer getBpGRCm39() {
		return bpGRCm39;
	}

	/**
	 * @param bpGRCm39 the bpGRCm39 to set
	 */
	public void setBpGRCm39(Integer bpGRCm39) {
		this.bpGRCm39 = bpGRCm39;
	}

	@JsonIgnore
	@Override
	public String id() {
		return geneId;
	}

	@Override
	public String getChr() {
		String chr = getChrGRCm39();
		if (chr!=null) {
			if (chr.toLowerCase().startsWith("chr")) {
				return chr.substring(3);
			}
		}
		return super.getChr();
	}

	@JsonIgnore
	@Override
	public Integer getStart() {
		return getBpGRCm39();
	}

	@JsonIgnore
	@Override
	public Integer getEnd() {
		return getBpGRCm39();
	}


}
