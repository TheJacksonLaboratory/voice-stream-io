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
 * The Class VariantEffect.
 *
 * @author gerrim
 * @see https://github.com/The-Sequence-Ontology/Specifications/blob/master/gvf.md
 * Section Variant Effect (Variant_eff)
 */
@Generated("POJO")
@RelationshipEntity(type = "VARIANT_EFFECT")
public class VariantEffect extends AbstractEntity {
	
	/** The uid. */
	@Id
	@GeneratedValue
    private Long uid;
	
	/** The variant. */
	@StartNode
	private Variant variant;
	
	/** The transcript. */
	@EndNode
	private Transcript transcript;

	/** The sequence variant. */
	private String sequenceVariant;
	
	/** The index. */
	private int index;
	
	/** The feature type. */
	private String featureType;
	
	/** The feature id. */
	private String featureId;
	
	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append(":START_ID(Variant-Id)");
		buf.append(getDelimiter());
		buf.append("sequenceVariant");
		buf.append(getDelimiter());
		buf.append("index:int");
		buf.append(getDelimiter());
		buf.append("featureType");
		buf.append(getDelimiter());
		buf.append("featureId");
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
		buf.append(variant.getUuid());
		buf.append(getDelimiter());
		buf.append(getSequenceVariant());
		buf.append(getDelimiter());
		buf.append(getIndex());
		buf.append(getDelimiter());
		buf.append(getFeatureType());
		buf.append(getDelimiter());
		buf.append(getFeatureId());
		buf.append(getDelimiter());
		buf.append(getFeatureId()); // Transcript id is the feature id.
		buf.append(getDelimiter());
		buf.append("VARIANT_EFFECT");
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
	 * Gets the sequence variant.
	 *
	 * @return the sequenceVariant
	 */
	public String getSequenceVariant() {
		return sequenceVariant;
	}
	
	/**
	 * Sets the sequence variant.
	 *
	 * @param sequenceVariant the sequenceVariant to set
	 */
	public void setSequenceVariant(String sequenceVariant) {
		this.sequenceVariant = sequenceVariant;
	}
	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Sets the index.
	 *
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * Gets the feature type.
	 *
	 * @return the featureType
	 */
	public String getFeatureType() {
		return featureType;
	}
	
	/**
	 * Sets the feature type.
	 *
	 * @param featureType the featureType to set
	 */
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	
	/**
	 * Gets the feature id.
	 *
	 * @return the featureId
	 */
	public String getFeatureId() {
		return featureId;
	}
	
	/**
	 * Sets the feature id.
	 *
	 * @param featureId the featureId to set
	 */
	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}
	
	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(featureId, featureType, index, sequenceVariant, uid);
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
		if (!(obj instanceof VariantEffect))
			return false;
		VariantEffect other = (VariantEffect) obj;
		return Objects.equals(featureId, other.featureId) && Objects.equals(featureType, other.featureType)
				&& index == other.index && Objects.equals(sequenceVariant, other.sequenceVariant)
				&& Objects.equals(uid, other.uid);
	}

	/**
	 * Gets the variant.
	 *
	 * @return the variant
	 */
	public Variant getVariant() {
		return variant;
	}
	
	/**
	 * Sets the variant.
	 *
	 * @param variant the variant to set
	 */
	public void setVariant(Variant variant) {
		this.variant = variant;
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
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		String rsId = variant!=null ? variant.getRsId() : null;
		String transId = transcript!=null ? transcript.getTranscriptId() : null;
		return rsId+"-[VARIANT_EFFECT]->"+transId;
	}
}
