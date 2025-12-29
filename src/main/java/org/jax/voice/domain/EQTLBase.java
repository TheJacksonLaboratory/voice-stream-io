package org.jax.voice.domain;

import java.util.Objects;

public abstract class EQTLBase extends AbstractEntity implements Located {


    private Double lod;
	private String tissueFileName;
	private String tissueGroup;
	private String tissueName;
	private String uberon;
    private Integer bp;
	private String studyId;
	/**
	 * @return the lod
	 */
	public Double getLod() {
		return lod;
	}
	/**
	 * @param lod the lod to set
	 */
	public void setLod(Double lod) {
		this.lod = lod;
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
	public void setTissueFileName(String tissueFileName) {
		this.tissueFileName = tissueFileName;
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
	public void setTissueName(String tissueName) {
		this.tissueName = tissueName;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(bp, lod, studyId, tissueFileName, tissueGroup, tissueName, uberon);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof EQTLBase))
			return false;
		EQTLBase other = (EQTLBase) obj;
		return Objects.equals(bp, other.bp) && Objects.equals(lod, other.lod) && Objects.equals(studyId, other.studyId)
				&& Objects.equals(tissueFileName, other.tissueFileName)
				&& Objects.equals(tissueGroup, other.tissueGroup) && Objects.equals(tissueName, other.tissueName)
				&& Objects.equals(uberon, other.uberon);
	}
	/**
	 * @return the bp
	 */
	public Integer getBp() {
		return bp;
	}
	/**
	 * @param bp the bp to set
	 */
	public void setBp(Integer bp) {
		this.bp = bp;
	}
	/**
	 * @return the studyId
	 */
	public String getStudyId() {
		return studyId;
	}
	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}


}
