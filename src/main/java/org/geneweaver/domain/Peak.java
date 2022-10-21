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
import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * A Genomic Region, usually from a bed file.
 * 
 * Required fields
The first three fields in each feature line are required:

chr - name of the chromosome or scaffold. Any valid seq_region_name can be used, and chromosome names can be given with or without the 'chr' prefix.
chromStart - Start position of the feature in standard chromosomal coordinates (i.e. first base is 0).
chromEnd - End position of the feature in standard chromosomal coordinates
chr1  213941196  213942363
chr1  213942363  213943530
chr1  213943530  213944697
chr2  158364697  158365864
chr2  158365864  158367031
chr3  127477031  127478198
chr3  127478198  127479365
chr3  127479365  127480532
chr3  127480532  127481699
Optional fields
Nine additional fields are optional. Note that columns cannot be empty - lower-numbered fields must always be populated if higher-numbered ones are used.

name - Label to be displayed under the feature, if turned on in "Configure this page".
score - A score between 0 and 1000. See track lines, below, for ways to configure the display style of scored data.
strand - defined as + (forward) or - (reverse).
thickStart - coordinate at which to start drawing the feature as a solid rectangle
thickEnd - coordinate at which to stop drawing the feature as a solid rectangle
itemRgb - an RGB colour value (e.g. 0,0,255). Only used if there is a track line with the value of itemRgb set to "on" (case-insensitive).
blockCount - the number of sub-elements (e.g. exons) within the feature
blockSizes - the size of these sub-elements
blockStarts - the start coordinate of each sub-element
chr7  127471196  127472363  Pos1  0  +  127471196  127472363  255,0,0
chr7  127472363  127473530  Pos2  0  +  127472363  127473530  255,0,0
chr7  127473530  127474697  Pos3  0  +  127473530  127474697  255,0,0
chr7  127474697  127475864  Pos4  0  +  127474697  127475864  255,0,0
chr7  127475864  127477031  Neg1  0  -  127475864  127477031  0,0,255
chr7  127477031  127478198  Neg2  0  -  127477031  127478198  0,0,255
chr7  127478198  127479365  Neg3  0  -  127478198  127479365  0,0,255
chr7  127479365  127480532  Pos5  0  +  127479365  127480532  255,0,0
chr7  127480532  127481699  Neg4  0  -  127480532  127481699  0,0,255

chr
start
end
name
score
strand
thickStart
thickEnd
itemRgb
blockCount
blockSizes
blockStarts


 * @author gerrim
 *
 */
@Generated("POJO")
@NodeEntity(label="Peak")
public class Peak  extends NamedEntity {


	@Index(unique=true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String peakId;

	/**
	 * The epigenome code from ensembl 
	 * E.g. A673/ sigmoid_colon/  etc. corresponding to the directory name in the source data.
	 * This property is called 'epigenome' in the gff format.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String epigenome;
	
	/**
	 * The epigenome description from ensembl 
	 * E.g. @see http://useast.ensembl.org/Homo_sapiens/Experiment/Sources?db=core;ex=all and 
	 * 		@see http://useast.ensembl.org/Mus_musculus/Experiment/Sources?db=core;ex=all
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String tissueDescription;
	
	/**
	 * The epigenome description from ensembl, usually something like "Cell/Tissue"
	 * E.g. @see http://useast.ensembl.org/Homo_sapiens/Experiment/Sources?db=core;ex=all and 
	 * 		@see http://useast.ensembl.org/Mus_musculus/Experiment/Sources?db=core;ex=all
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String filterType;

	/**
	 * The subdir of the new data sources e.g. H3K4me1
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String featureType;

    /** The chromosome e.g. chr1 or X. */
	@JsonInclude(JsonInclude.Include.NON_NULL)
 	private String chr;
	
	/**
	 * Start position of the feature in standard chromosomal coordinates (i.e. first base is 0).
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private int start;
	
	/**
	 * End position of the feature in standard chromosomal coordinates.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private int end;
	
	/**
	 * A score between 0 and 1000. See track lines, below, for ways to configure the display style of scored data.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private int score;
	
	/**
	 * defined as + (forward) or - (reverse).
	 */
	private Strand strand;
	
	/**
	 * coordinate at which to start drawing the feature as a solid rectangle
	 */
	private int thickStart;
	
	/**
	 * coordinate at which to stop drawing the feature as a solid rectangle
	 */
	private int thickEnd;
	
	/**
	 * an RGB colour value (e.g. 0,0,255). Only used if there is a track line with the value of itemRgb set to "on" (case-insensitive).
	 */
	private int[] itemRgb;
	
	/**
	 * the number of sub-elements (e.g. exons) within the feature
	 */
	private int blockCount;
	
	/**
	 * the size of these sub-elements
	 */
	private int[] blockSizes;
	
	/**
	 * the size of these sub-elements
	 */
	private int[] blockStarts;
	
	
	public enum Strand {
		
		FORWARD, REVERSE;
		
		public static Strand from(String seg) {
			if ("+".equals(seg)) return FORWARD; 
			if ("-".equals(seg)) return REVERSE; 
			return null; // They can choose to put null/none etc.
		}
		
		public String toString() {
			if (this==FORWARD) return "+";
			if (this==REVERSE) return "-";
			return null;
		}
	}

	public Peak() {
		
	}

	public Peak(String peakId, String species, String chr, int start, int end, String name, int score, Strand strand, int thickStart, int thickEnd) {
		
		this.peakId = peakId;
		setSpecies(species);
		this.chr = chr;
		this.start = start;
		this.end = end;
		this.setName(name);
		this.score = score;
		this.strand = strand;
		this.thickStart = thickStart;
		this.thickEnd = thickEnd;
	}

	public Peak(String speakId, int low, int high) {
		this.peakId = speakId;
		this.start = low;
		this.end = high;
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		String fields = delimify("peakId:ID(Peak-Id)",
				"epigenome", "tissueDescription", "featureType", "filterType",
				"start", "end", "chr", "score", "strand", ":LABEL");
		buf.append(fields);
		
		String sheader = super.getHeader();
		if (sheader!=null) {
			buf.append(getDelimiter());
			buf.append(sheader);
		}
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
		String values = delimify(getPeakId(),
				getEpigenome(), getTissueDescription(), getFeatureType(), getFilterType(),
				getStart(), getEnd(), getChr(), getScore(), strandCharacter(getStrand()),
				getClass().getSimpleName().toString());
		buf.append(values);
		
		String scsv = super.toCsv();
		if (scsv!=null) {
			buf.append(getDelimiter());
			buf.append(scsv);
		}
		return buf.toString();
	}

	@JsonIgnore
	private String strandCharacter(Strand s) {
		if (s==null) return null;
		return s.toString();
	}

	/**
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}


	/**
	 * @param chr the chr to set
	 */
	public void setChr(String chrom) {
		this.chr = chrom;
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
	 * @return the score
	 */
	public int getScore() {
		return score;
	}


	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}


	/**
	 * @return the strand
	 */
	public Strand getStrand() {
		return strand;
	}


	/**
	 * @param strand the strand to set
	 */
	public void setStrand(Strand strand) {
		this.strand = strand;
	}


	/**
	 * @return the thickStart
	 */
	public int getThickStart() {
		return thickStart;
	}


	/**
	 * @param thickStart the thickStart to set
	 */
	public void setThickStart(int thickStart) {
		this.thickStart = thickStart;
	}


	/**
	 * @return the thickEnd
	 */
	public int getThickEnd() {
		return thickEnd;
	}


	/**
	 * @param thickEnd the thickEnd to set
	 */
	public void setThickEnd(int thickEnd) {
		this.thickEnd = thickEnd;
	}


	/**
	 * @return the itemRgb
	 */
	public int[] getItemRgb() {
		return itemRgb;
	}


	/**
	 * @param itemRgb the itemRgb to set
	 */
	public void setItemRgb(int[] itemRgb) {
		this.itemRgb = itemRgb;
	}


	/**
	 * @return the blockCount
	 */
	public int getBlockCount() {
		return blockCount;
	}


	/**
	 * @param blockCount the blockCount to set
	 */
	public void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}


	/**
	 * @return the blockSizes
	 */
	public int[] getBlockSizes() {
		return blockSizes;
	}


	/**
	 * @param blockSizes the blockSizes to set
	 */
	public void setBlockSizes(int... blockSizes) {
		this.blockSizes = blockSizes;
	}


	/**
	 * @return the blockStarts
	 */
	public int[] getBlockStarts() {
		return blockStarts;
	}


	/**
	 * @param blockStarts the blockStarts to set
	 */
	public void setBlockStarts(int... blockStarts) {
		this.blockStarts = blockStarts;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(blockSizes);
		result = prime * result + Arrays.hashCode(blockStarts);
		result = prime * result + Arrays.hashCode(itemRgb);
		result = prime * result + Objects.hash(blockCount, chr, end, epigenome, featureType, filterType, peakId, score,
				start, strand, thickEnd, thickStart, tissueDescription);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Peak))
			return false;
		Peak other = (Peak) obj;
		return blockCount == other.blockCount && Arrays.equals(blockSizes, other.blockSizes)
				&& Arrays.equals(blockStarts, other.blockStarts) && Objects.equals(chr, other.chr) && end == other.end
				&& Objects.equals(epigenome, other.epigenome) && Objects.equals(featureType, other.featureType)
				&& Objects.equals(filterType, other.filterType) && Arrays.equals(itemRgb, other.itemRgb)
				&& Objects.equals(peakId, other.peakId) && score == other.score && start == other.start
				&& strand == other.strand && thickEnd == other.thickEnd && thickStart == other.thickStart
				&& Objects.equals(tissueDescription, other.tissueDescription);
	}

	/**
	 * @return the peakId
	 */
	public String getPeakId() {
		return peakId;
	}

	/**
	 * @param peakId the peakId to set
	 */
	public void setPeakId(String peakId) {
		this.peakId = peakId;
	}

	/**
	 * @return the epigenome
	 */
	public String getEpigenome() {
		return epigenome;
	}

	/**
	 * @param epigenome the epigenome to set
	 */
	public void setEpigenome(String epigenome) {
		this.epigenome = epigenome;
	}

	/**
	 * @return the tissueDescription
	 */
	public String getTissueDescription() {
		return tissueDescription;
	}

	/**
	 * @param tissueDescription the tissueDescription to set
	 */
	public void setTissueDescription(String tissueDescription) {
		this.tissueDescription = tissueDescription;
	}

	/**
	 * @return the featureType
	 */
	public String getFeatureType() {
		return featureType;
	}

	/**
	 * @param featureType the featureType to set
	 */
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	/**
	 * @return the filterType
	 */
	public String getFilterType() {
		return filterType;
	}

	/**
	 * @param filterType the filterType to set
	 */
	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

}
