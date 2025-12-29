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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@Generated("POJO")
@NodeEntity(label="ChromatinInteraction")
public class ChromatinInteraction extends AbstractEntity implements AnchoredEntity {

	private ExperimentMetadata meta;	
	private Anchor left;	
	private Anchor right;
	private int petCount;
	private double p;
	private double fdr;
	private boolean overlapDNAPET;

	/**
	 * @return the meta
	 */
	public ExperimentMetadata getMeta() {
		return meta;
	}

	/**
	 * @param meta the meta to set
	 */
	public void setMeta(ExperimentMetadata meta) {
		this.meta = meta;
	}

	/**
	 * @return the left
	 */
	public Anchor getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(Anchor left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public Anchor getRight() {
		return right;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(Anchor right) {
		this.right = right;
	}

	/**
	 * @return the petCount
	 */
	public int getPetCount() {
		return petCount;
	}

	/**
	 * @param petCount the petCount to set
	 */
	public void setPetCount(int petCount) {
		this.petCount = petCount;
	}

	/**
	 * @return the p
	 */
	public double getP() {
		return p;
	}

	/**
	 * @param p the p to set
	 */
	public void setP(double p) {
		this.p = p;
	}

	/**
	 * @return the fdr
	 */
	public double getFdr() {
		return fdr;
	}

	/**
	 * @param fdr the fdr to set
	 */
	public void setFdr(double fdr) {
		this.fdr = fdr;
	}

	/**
	 * @return the overlapDNAPET
	 */
	public boolean isOverlapDNAPET() {
		return overlapDNAPET;
	}

	/**
	 * @param overlapDNAPET the overlapDNAPET to set
	 */
	public void setOverlapDNAPET(boolean overlapDNAPET) {
		this.overlapDNAPET = overlapDNAPET;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(fdr, left, meta, overlapDNAPET, p, petCount, right);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ChromatinInteraction))
			return false;
		ChromatinInteraction other = (ChromatinInteraction) obj;
		return Double.doubleToLongBits(fdr) == Double.doubleToLongBits(other.fdr) && Objects.equals(left, other.left)
				&& Objects.equals(meta, other.meta) && overlapDNAPET == other.overlapDNAPET
				&& Double.doubleToLongBits(p) == Double.doubleToLongBits(other.p) && petCount == other.petCount
				&& Objects.equals(right, other.right);
	}

	@Override
	public Collection<Anchor> anchors() {
		return Arrays.asList(getLeft(), getRight());
	}
}
