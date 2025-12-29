package org.jax.voice.domain;

import java.util.Map;
import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.NodeEntity;

@Generated("POJO")
@NodeEntity(label="VariantCall")
public class VariantCall extends AbstractEntity implements Species {

	/** The species code. */
    private Integer species;
    
    private String chrom;
    private int pos;
    private String id; // rsId
    private char ref;
    private char alt;
    private int qual;
    private String filter;
    private Map<String, String> info;
    private String format;
    

	public Integer getSpecies() {
		return species;
	}

	public void setSpecies(Integer species) {
		this.species = species;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(alt, chrom, filter, format, id, info, pos, qual, ref, species);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof VariantCall))
			return false;
		VariantCall other = (VariantCall) obj;
		return alt == other.alt && Objects.equals(chrom, other.chrom) && Objects.equals(filter, other.filter)
				&& Objects.equals(format, other.format) && Objects.equals(id, other.id)
				&& Objects.equals(info, other.info) && pos == other.pos && qual == other.qual && ref == other.ref
				&& Objects.equals(species, other.species);
	}

	public String getChrom() {
		return chrom;
	}

	public void setChrom(String chrom) {
		this.chrom = chrom;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public char getRef() {
		return ref;
	}

	public void setRef(char ref) {
		this.ref = ref;
	}

	public char getAlt() {
		return alt;
	}

	public void setAlt(char alt) {
		this.alt = alt;
	}

	public int getQual() {
		return qual;
	}

	public void setQual(int qual) {
		this.qual = qual;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Map<String, String> getInfo() {
		return info;
	}

	public void setInfo(Map<String, String> info) {
		this.info = info;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
}
