package org.jax.gweaver.domain;

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

	/** The uid. */
	@Id
	@GeneratedValue
    private Long uid;
	
	private ExperimentMetadata meta;	
	private Anchor left;	
	private Anchor right;
	private int petCount;
	private double p;
	private double fdr;
	private boolean overlapDNAPET;

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}

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
		return Objects.hash(fdr, left, meta, overlapDNAPET, p, petCount, right, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ChromatinInteraction))
			return false;
		ChromatinInteraction other = (ChromatinInteraction) obj;
		return Double.doubleToLongBits(fdr) == Double.doubleToLongBits(other.fdr) && Objects.equals(left, other.left)
				&& Objects.equals(meta, other.meta) && overlapDNAPET == other.overlapDNAPET
				&& Double.doubleToLongBits(p) == Double.doubleToLongBits(other.p) && petCount == other.petCount
				&& Objects.equals(right, other.right) && Objects.equals(uid, other.uid);
	}

	@Override
	public Collection<Anchor> anchors() {
		return Arrays.asList(getLeft(), getRight());
	}

	
}
