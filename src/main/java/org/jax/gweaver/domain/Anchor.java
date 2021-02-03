package org.jax.gweaver.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

@Generated("POJO")
public class Anchor implements AnchoredEntity {

	/** The uid. */
	@Id
	@GeneratedValue
    private Long uid;
	
	private String chrom;
	
	private int start;
	
	private int end;

	private int intensity;

	public Anchor() {
		
	}
	
	public Anchor(String chr, int start, int end) {
		this.chrom = chr;
		this.start = start;
		this.end   = end;
	}
	
	public Anchor(String chr, int start, int end, int intensity) {
		this.chrom = chr;
		this.start = start;
		this.end   = end;
		this.intensity   = intensity;
	}

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
	 * @return the chrom
	 */
	public String getChrom() {
		return chrom;
	}

	/**
	 * @param chrom the chrom to set
	 */
	public void setChrom(String chrom) {
		this.chrom = chrom;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(chrom, end, intensity, start, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Anchor))
			return false;
		Anchor other = (Anchor) obj;
		return Objects.equals(chrom, other.chrom) && end == other.end && intensity == other.intensity
				&& start == other.start && Objects.equals(uid, other.uid);
	}


	@Override
	public Collection<Anchor> anchors() {
		return Arrays.asList(this);
	}

	/**
	 * @return the intensity
	 */
	protected int getIntensity() {
		return intensity;
	}

	/**
	 * @param intensity the intensity to set
	 */
	protected void setIntensity(int intensity) {
		this.intensity = intensity;
	}


}
