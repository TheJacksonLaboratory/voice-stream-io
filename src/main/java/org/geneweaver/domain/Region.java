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

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * 
 * A Genomic Region, usually from a bed file.
 * 
 * Required fields
The first three fields in each feature line are required:

chrom - name of the chromosome or scaffold. Any valid seq_region_name can be used, and chromosome names can be given with or without the 'chr' prefix.
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

chrom
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
@NodeEntity(label="Region")
public class Region  extends NamedEntity {

	/**
	 * name of the chromosome or scaffold. Any valid seq_region_name can be used, and chromosome names can be given with or without the 'chr' prefix.
	 */
	private String chrom;
	
	/**
	 * Start position of the feature in standard chromosomal coordinates (i.e. first base is 0).
	 */
	private int start;
	
	/**
	 * End position of the feature in standard chromosomal coordinates.
	 */
	private int end;
	
	/**
	 * A score between 0 and 1000. See track lines, below, for ways to configure the display style of scored data.
	 */
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
	}

	public Region() {
		
	}

	public Region(String species, String chr, int start, int end, String name, int score, Strand strand, int thickStart, int thickEnd) {
		setSpecies(species);
		this.chrom = chr;
		this.start = start;
		this.end = end;
		this.setName(name);
		this.score = score;
		this.strand = strand;
		this.thickStart = thickStart;
		this.thickEnd = thickEnd;
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
		result = prime * result + Objects.hash(blockCount, chrom, end, score, start, strand, thickEnd, thickStart);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Region))
			return false;
		Region other = (Region) obj;
		return blockCount == other.blockCount && Arrays.equals(blockSizes, other.blockSizes)
				&& Arrays.equals(blockStarts, other.blockStarts) && Objects.equals(chrom, other.chrom)
				&& end == other.end && Arrays.equals(itemRgb, other.itemRgb) && score == other.score
				&& start == other.start && strand == other.strand && thickEnd == other.thickEnd
				&& thickStart == other.thickStart;
	}

}
