package org.geneweaver.domain;

import java.util.Objects;

import org.apache.commons.beanutils.BeanMap;


/**
 * Contact and Step are similar and could be the same object. 
 * We translate the Step into a Contact by looking up the
 * geneId and rsId of the other entities.
 * 
 * @author gerrim
 * @see Step
 */
public class Contact extends AbstractContact {

	
	private String geneId;
	private String rsId;
	
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
		buf.append("method");
		buf.append(getDelimiter());
		buf.append("cellType");
		buf.append(getDelimiter());
		buf.append("originalAssembly");
		buf.append(getDelimiter());
		buf.append(":END_ID(Rs-Id-"+getSpecies()+")");
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
		buf.append(getGeneId());
		buf.append(getDelimiter());
		buf.append(getMethod());
		buf.append(getDelimiter());
		buf.append(getCellType());
		buf.append(getDelimiter());
		buf.append(getOriginalAssembly());
		buf.append(getDelimiter());
		buf.append(getRsId());
		buf.append(getDelimiter());
		buf.append(getClass().getSimpleName().toUpperCase());
		return buf.toString();
	}

	/**
	 * @return the geneId
	 */
	public String getGeneId() {
		return geneId;
	}
	/**
	 * @param geneId the geneId to set
	 */
	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}
	/**
	 * @return the rsId
	 */
	public String getRsId() {
		return rsId;
	}
	/**
	 * @param rsId the rsId to set
	 */
	public void setRsId(String rsId) {
		this.rsId = rsId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(geneId, rsId);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Contact))
			return false;
		Contact other = (Contact) obj;
		return Objects.equals(geneId, other.geneId) && Objects.equals(rsId, other.rsId);
	}
	
	public static Contact of(AbstractContact other) {
		Contact ret = new Contact();
		BeanMap to = new BeanMap(ret);
		to.putAllWriteable(new BeanMap(other));
		return ret;
	}

}
