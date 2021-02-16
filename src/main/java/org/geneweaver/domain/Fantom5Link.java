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
package org.geneweaver.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import javax.annotation.processing.Generated;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * This file is generated to find intersection of Fantom5 and Ensembl
 * @see http://cs.ecs.baylor.edu/~reynolds/geneweaver-regulatory-integration/
 * Section 'Mapping experimentally verified TSS-enhancers onto stable Ensembl IDs'
 
 Example:

## UID CHROM F5_CHROM_START F5_CHROM_END GENE_TARGET ENSEMBL_ID(S)
0	1	66331609	66733058	PDE4B	ENSR00000008110|ENSR00000008134|ENSR00000008117|ENSR00000008081|ENSR00000008071|ENSR00000008088|ENSR00000008109|ENSR00000008083|ENSR00000008080|ENSR00000008072|ENSR00000008127|ENSR00000008105|ENSR00000008132
1	1	66415892	66752957	TCTEX1D1	ENSR00000008110|ENSR00000008134|ENSR00000008117|ENSR00000008088|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
2	1	66438978	66930743	MIER1	ENSR00000008110|ENSR00000008134|ENSR00000008137|ENSR00000008117|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
3	1	66445327	66930743	MIER1	ENSR00000008110|ENSR00000008134|ENSR00000008137|ENSR00000008117|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
4	1	66489150	66930743	MIER1	ENSR00000008110|ENSR00000008134|ENSR00000008137|ENSR00000008117|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
 
 * @author gerrim
 *
 */
@Generated("POJO")
public class Fantom5Link extends NamedEntity {

	private String chrom;
	
	private int start;
	
	private int end;
	
	private Collection<String> ensemblIds = new LinkedList<>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(chrom, end, ensemblIds, start);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Fantom5Link))
			return false;
		Fantom5Link other = (Fantom5Link) obj;
		return Objects.equals(chrom, other.chrom) && end == other.end
				&& Objects.equals(ensemblIds, other.ensemblIds) && start == other.start;
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

	/**
	 * @return the ensemblNames
	 */
	public Collection<String> getEnsemblIds() {
		return ensemblIds;
	}

	/**
	 * @param ensemblNames the ensemblNames to set
	 */
	public void setEnsemblIds(Collection<String> ensemblNames) {
		this.ensemblIds = ensemblNames;
	}
	
	
	public boolean addEnsemblIds(String... geneIds) {
		return ensemblIds.addAll(Arrays.asList(geneIds));
	}
	
	public boolean removeEnsemblIds(String... geneIds) {
		return ensemblIds.removeAll(Arrays.asList(geneIds));
	}

	/**
	 * Span between start and end of bases
	 * @return
	 */
	@JsonIgnore
	public int span() {
		return end-start;
	}

}
