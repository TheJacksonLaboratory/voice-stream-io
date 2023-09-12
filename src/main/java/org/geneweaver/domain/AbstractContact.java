package org.geneweaver.domain;

import java.util.Objects;

/**

# filename_num = 1
# method = 4 C
# species = mouse
# original_assembly = mm6
# cell_type = fetal liver
# Enhancer Source = NA
# Antibody = β-globin
# GEO Number = GSE5891
# DataSource = PubMed
# Reference = -2
# Resolution = 31.2kb
chr	start1	end1	chr	start2	end2	contact	FDR	p-value
chr7	103827929	103853207	chr7	73478840	73658205	NA	0.05	NA
chr7	103827929	103853207	chr7	78752268	78850141	NA	0.05	NA
chr7	103827929	103853207	chr7	80106278	80301277	NA	0.05	NA
chr7	103827929	103853207	chr7	81696768	81747483	NA	0.05	NA
chr7	103827929	103853207	chr7	83827289	83945446	NA	0.05	NA

 * @author gerrim
 * @see Contact
 * @see Step
 *
 */
public abstract class AbstractContact extends AbstractEntity {

	private String contact;
	private Float fdr;
	private Float pValue;
	
	private int fileNameNumber;
	private String method;
	private String speciesName;

	private String originalAssembly;
	private String cellType;
	private String enhancerSource;
	private String antibody;
	private String geoNumber;
	private String dataSource;
	private Integer reference;
	private String resolution;
	/**
	 * @return the contact
	 */
	public String getContact() {
		return contact;
	}
	/**
	 * @param contact the contact to set
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	/**
	 * @return the fdr
	 */
	public Float getFdr() {
		return fdr;
	}
	/**
	 * @param fdr the fdr to set
	 */
	public void setFdr(Float fdr) {
		this.fdr = fdr;
	}
	/**
	 * @return the pValue
	 */
	public Float getpValue() {
		return pValue;
	}
	/**
	 * @param pValue the pValue to set
	 */
	public void setpValue(Float pValue) {
		this.pValue = pValue;
	}
	/**
	 * @return the fileNameNumber
	 */
	public int getFileNameNumber() {
		return fileNameNumber;
	}
	/**
	 * @param fileNameNumber the fileNameNumber to set
	 */
	public void setFileNameNumber(int fileNameNumber) {
		this.fileNameNumber = fileNameNumber;
	}
	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	/**
	 * @return the speciesName
	 */
	public String getSpeciesName() {
		return speciesName;
	}
	/**
	 * @param speciesName the speciesName to set
	 */
	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	/**
	 * @return the originalAssembly
	 */
	public String getOriginalAssembly() {
		return originalAssembly;
	}
	/**
	 * @param originalAssembly the originalAssembly to set
	 */
	public void setOriginalAssembly(String originalAssembly) {
		this.originalAssembly = originalAssembly;
	}
	/**
	 * @return the cellType
	 */
	public String getCellType() {
		return cellType;
	}
	/**
	 * @param cellType the cellType to set
	 */
	public void setCellType(String cellType) {
		this.cellType = cellType;
	}
	/**
	 * @return the enhancerSource
	 */
	public String getEnhancerSource() {
		return enhancerSource;
	}
	/**
	 * @param enhancerSource the enhancerSource to set
	 */
	public void setEnhancerSource(String enhancerSource) {
		this.enhancerSource = enhancerSource;
	}
	/**
	 * @return the antibody
	 */
	public String getAntibody() {
		return antibody;
	}
	/**
	 * @param antibody the antibody to set
	 */
	public void setAntibody(String antibody) {
		this.antibody = antibody;
	}
	/**
	 * @return the geoNumber
	 */
	public String getGeoNumber() {
		return geoNumber;
	}
	/**
	 * @param geoNumber the geoNumber to set
	 */
	public void setGeoNumber(String geoNumber) {
		this.geoNumber = geoNumber;
	}
	/**
	 * @return the dataSource
	 */
	public String getDataSource() {
		return dataSource;
	}
	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	/**
	 * @return the reference
	 */
	public Integer getReference() {
		return reference;
	}
	/**
	 * @param reference the reference to set
	 */
	public void setReference(Integer reference) {
		this.reference = reference;
	}
	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return resolution;
	}
	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(antibody, cellType, contact, dataSource, enhancerSource, fdr,
				fileNameNumber, geoNumber, method, originalAssembly, pValue, reference, resolution, speciesName);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AbstractContact))
			return false;
		AbstractContact other = (AbstractContact) obj;
		return Objects.equals(antibody, other.antibody) && Objects.equals(cellType, other.cellType)
				&& Objects.equals(contact, other.contact) && Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(enhancerSource, other.enhancerSource) && Objects.equals(fdr, other.fdr)
				&& fileNameNumber == other.fileNameNumber && Objects.equals(geoNumber, other.geoNumber)
				&& Objects.equals(method, other.method) && Objects.equals(originalAssembly, other.originalAssembly)
				&& Objects.equals(pValue, other.pValue) && Objects.equals(reference, other.reference)
				&& Objects.equals(resolution, other.resolution) && Objects.equals(speciesName, other.speciesName);
	}

}
