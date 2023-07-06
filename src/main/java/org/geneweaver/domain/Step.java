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
 *
 */
public class Step extends AbstractContact {

	private String chr1;
	private Integer start1;
	private Integer end1;
	
	private String chr2;
	private Integer start2;
	private Integer end2;
	
	@Override
	public String getHeader() {
		throw new IllegalArgumentException("Cannot write Step to bulk file.");
	}
	@Override
	public String toCsv() {
		throw new IllegalArgumentException("Cannot write Step to bulk file.");
	}

	/**
	 * @return the chr1
	 */
	public String getChr1() {
		return chr1;
	}
	/**
	 * @param chr1 the chr1 to set
	 */
	public void setChr1(String chr1) {
		this.chr1 = chr1;
	}
	/**
	 * @return the start1
	 */
	public Integer getStart1() {
		return start1;
	}
	/**
	 * @param start1 the start1 to set
	 */
	public void setStart1(Integer start1) {
		this.start1 = start1;
	}
	/**
	 * @return the end1
	 */
	public Integer getEnd1() {
		return end1;
	}
	/**
	 * @param end1 the end1 to set
	 */
	public void setEnd1(Integer end1) {
		this.end1 = end1;
	}
	/**
	 * @return the chr2
	 */
	public String getChr2() {
		return chr2;
	}
	/**
	 * @param chr2 the chr2 to set
	 */
	public void setChr2(String chr2) {
		this.chr2 = chr2;
	}
	/**
	 * @return the start2
	 */
	public Integer getStart2() {
		return start2;
	}
	/**
	 * @param start2 the start2 to set
	 */
	public void setStart2(Integer start2) {
		this.start2 = start2;
	}
	/**
	 * @return the end2
	 */
	public Integer getEnd2() {
		return end2;
	}
	/**
	 * @param end2 the end2 to set
	 */
	public void setEnd2(Integer end2) {
		this.end2 = end2;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(chr1, chr2, end1, end2, start1, start2);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Step))
			return false;
		Step other = (Step) obj;
		return Objects.equals(chr1, other.chr1) && Objects.equals(chr2, other.chr2) && Objects.equals(end1, other.end1)
				&& Objects.equals(end2, other.end2) && Objects.equals(start1, other.start1)
				&& Objects.equals(start2, other.start2);
	}
	
}