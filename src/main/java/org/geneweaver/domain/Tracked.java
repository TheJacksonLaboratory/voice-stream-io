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

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@Generated("POJO")
@RelationshipEntity(type = "TRACKED")
public class Tracked  extends AbstractEntity {

	/** The gene. */
	@StartNode
	private Track track;
	
	/** The transcript. */
	@EndNode
	private Peak peak;

	private String chr;

	public Tracked() {
		
	}
	
	public Tracked(Peak peak, Track track) {
		this.peak = peak;
		this.track = track;
		this.chr = peak.getChr();
	}

	/**
	 * @return the track
	 */
	public Track getTrack() {
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(Track track) {
		this.track = track;
	}

	/**
	 * @return the region
	 */
	public Peak getRegion() {
		return peak;
	}

	/**
	 * @param peak the region to set
	 */
	public void setRegion(Peak peak) {
		this.peak = peak;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		String tname = track!=null ? track.getName() : null;
		String rname = peak!=null ? peak.getName() : null;
		return tname+"-[TRACKED]->"+rname;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(peak, track);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Tracked))
			return false;
		Tracked other = (Tracked) obj;
		return Objects.equals(peak, other.peak) && Objects.equals(track, other.track);
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
	public void setChr(String chr) {
		this.chr = chr;
	}


}
