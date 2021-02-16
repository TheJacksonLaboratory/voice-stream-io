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
 * 
Track lines
Track definition lines can be used to configure the display further, e.g. by grouping features into separate tracks. Track lines should be placed at the beginning of the list of features they are to affect.

The track line consists of the word 'track' followed by space-separated key=value pairs - see the example below. Valid parameters used by Ensembl are:

name - unique name to identify this track when parsing the file
description - Label to be displayed under the track in Region in Detail
priority - integer defining the order in which to display tracks, if multiple tracks are defined.
color - as RGB, hex or X11 named color.
useScore - set to 1 to render the track in greyscale based on the values in the score column.
itemRgb - if set to 'on' (case-insensitive), the individual RGB values defined in tracks will be used.
track name="ItemRGBDemo" description="Item RGB demonstration" itemRgb="On" 
chr7  127471196  127472363  Pos1  0  +  127471196  127472363  255,0,0
chr7  127472363  127473530  Pos2  0  +  127472363  127473530  255,0,0
chr7  127473530  127474697  Pos3  0  +  127473530  127474697  255,0,0
chr7  127474697  127475864  Pos4  0  +  127474697  127475864  255,0,0
chr7  127475864  127477031  Neg1  0  -  127475864  127477031  0,0,255
chr7  127477031  127478198  Neg2  0  -  127477031  127478198  0,0,255
chr7  127478198  127479365  Neg3  0  -  127478198  127479365  0,0,255
chr7  127479365  127480532  Pos5  0  +  127479365  127480532  255,0,0
chr7  127480532  127481699  Neg4  0  -  127480532  127481699  0,0,255
BedGraph format
BedGraph is a suitable format for moderate amounts of scored data. It is based on the BED format (see above) with the following differences:

The score is placed in column 4, not column 5
Track lines are compulsory, and must include type=bedGraph. Currently the only optional parameters supported by Ensembl are:
name - see above
description - see above
priority - see above
graphType - either 'bar' or 'points'.
track type=bedGraph name="BedGraph Format" description="BedGraph format" priority=20
chr1 59302000 59302300 -1.0
chr1 59302300 59302600 -0.75
chr1 59302600 59302900 -0.50
chr1 59302900 59303200 -0.25
chr1 59303200 59303500 0.0
chr1 59303500 59303800 0.25
chr1 59303800 59304100 0.50
chr1 59304100 59304400 0.75

 * @author gerrim
 *
 */
@Generated("POJO")
@NodeEntity(label="Track")
public class Track  extends NamedEntity {
	
	/**
	 * type=bedGraph for graphs
	 */
	private String type;
	
	/**
	 * Either bar or points.
	 */
	private String graphType;
	
	/**
	 * Label to be displayed under the track in Region in Detail
	 */
	private String description;
	
	/**
	 * integer defining the order in which to display tracks, if multiple tracks are defined.
	 */
	private int priority;
	
	/**
	 * as RGB, hex or X11 named color.
	 */
	private int[] color;
	
	/**
	 * set to 1 to render the track in greyscale based on the values in the score column.
	 */
	private int useScore;
	
	/**
	 * if set to 'on' (case-insensitive), the individual RGB values defined in tracks will be used.
	 */
	private boolean itemRgb;


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int[] getColor() {
		return color;
	}

	public void setColor(int[] color) {
		this.color = color;
	}

	public int getUseScore() {
		return useScore;
	}

	public void setUseScore(int useScore) {
		this.useScore = useScore;
	}

	public boolean isItemRgb() {
		return itemRgb;
	}

	public void setItemRgb(boolean itemRgb) {
		this.itemRgb = itemRgb;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(color);
		result = prime * result + Objects.hash(description, graphType, itemRgb, priority, type, useScore);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Track))
			return false;
		Track other = (Track) obj;
		return Arrays.equals(color, other.color) && Objects.equals(description, other.description)
				&& Objects.equals(graphType, other.graphType) && itemRgb == other.itemRgb && priority == other.priority
				&& Objects.equals(type, other.type) && useScore == other.useScore;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the graphType
	 */
	public String getGraphType() {
		return graphType;
	}

	/**
	 * @param graphType the graphType to set
	 */
	public void setGraphType(String graphType) {
		this.graphType = graphType;
	}
}
