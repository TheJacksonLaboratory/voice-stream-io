package org.geneweaver.domain;

import java.util.Map;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.NodeEntity;

@Generated("POJO")
@NodeEntity(label="VariantCall")
public class VariantCall extends AbstractEntity implements Species {

	/** The species. */
    private String species;
    
    private String chrom;
    private int pos;
    private String id; // rsId
    private char ref;
    private char alt;
    private int qual;
    private String filter;
    private Map<String, String> info;
    private String format;
    

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + alt;
		result = prime * result + ((chrom == null) ? 0 : chrom.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + pos;
		result = prime * result + qual;
		result = prime * result + ref;
		result = prime * result + ((species == null) ? 0 : species.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariantCall other = (VariantCall) obj;
		if (alt != other.alt)
			return false;
		if (chrom == null) {
			if (other.chrom != null)
				return false;
		} else if (!chrom.equals(other.chrom))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		if (pos != other.pos)
			return false;
		if (qual != other.qual)
			return false;
		if (ref != other.ref)
			return false;
		if (species == null) {
			if (other.species != null)
				return false;
		} else if (!species.equals(other.species))
			return false;
		return true;
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
