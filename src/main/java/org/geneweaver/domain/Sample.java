package org.geneweaver.domain;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sample extends AbstractEntity implements Comparable<Sample>{


	private String tissueGroup;
	private String tissueName;
	private String originalTissueName;
	private String tissueSecondaryGroup;
	
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
	public void setTissueName(String tissueName) {
		this.tissueName = tissueName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(originalTissueName, tissueGroup, tissueName, tissueSecondaryGroup);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Sample))
			return false;
		Sample other = (Sample) obj;
		return Objects.equals(originalTissueName, other.originalTissueName)
				&& Objects.equals(tissueGroup, other.tissueGroup) && Objects.equals(tissueName, other.tissueName)
				&& Objects.equals(tissueSecondaryGroup, other.tissueSecondaryGroup);
	}
	@Override
	public String toString() {
		return tissueGroup + " " + tissueName ;
	}
	
	private static final Pattern groupPattern = Pattern.compile("^(.+ \\- )(.+)$");
	
	/**
	 * Method trims the group name from the tissue name
	 * @return true if we trimmed something.
	 */
	public boolean trim() {
		
		if (tissueName==null) return false;
		
		String frag = tissueGroup+" - ";
		if (tissueName.startsWith(frag)) {
			originalTissueName = tissueName;
			tissueName = tissueName.substring(frag.length());
			return true;
		}
		
		Matcher m = groupPattern.matcher(tissueName);
		if (m.matches()) {
			originalTissueName = tissueName;
			tissueName = m.group(2);
			tissueSecondaryGroup = m.group(1);
			return true;
		}
		
		return false;
	}
	/**
	 * @return the tissueSecondaryGroup
	 */
	public String getTissueSecondaryGroup() {
		return tissueSecondaryGroup;
	}
	/**
	 * @param tissueSecondaryGroup the tissueSecondaryGroup to set
	 */
	public void setTissueSecondaryGroup(String tissueSecondaryGroup) {
		this.tissueSecondaryGroup = tissueSecondaryGroup;
	}
	
	private String getCompareString() {
		return tissueGroup+tissueName;
	}
	
	@Override
	public int compareTo(Sample o) {
		return getCompareString().compareTo(o.getCompareString());
	}
	/**
	 * @return the originalTissueName
	 */
	public String getOriginalTissueName() {
		return originalTissueName;
	}
	/**
	 * @param originalTissueName the originalTissueName to set
	 */
	public void setOriginalTissueName(String originalTissueName) {
		this.originalTissueName = originalTissueName;
	}
}
