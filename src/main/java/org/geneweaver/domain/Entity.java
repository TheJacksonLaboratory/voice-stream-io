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

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Any node or edge in our graph.
 * 
 * @author gerrim
 *
 */
public interface Entity {
	
	public static final String NO_CHR = "NO-CHROMOSOME";
	
	/** 
	 * The chromosome on which this entity exists. 
	 **/
	String getChr();
	
	/**
	 * The unique Id of the entity set by Neo4j
	 * when the object is saved. Might be null if the 
	 * object has not been saved with OGM.
	 *
	 * @return the uid
	 */
	Long getUid();
	
	/**
	 * Checks if is relationship.
	 *
	 * @return true if this node is a node..
	 */
	@JsonIgnore
	public default boolean isRelationship() {
		return getClass().isAnnotationPresent(RelationshipEntity.class);
	}
	
	/**
	 * Checks if is node.
	 *
	 * @return true if this node is a node..
	 */
	@JsonIgnore
	public default boolean isNode() {
		return getClass().isAnnotationPresent(NodeEntity.class);
	}

	/**
	 * Get the header for this entity type. The header 
	 * defines the properties of the bulk import for the object.
	 * It is only needed once per object type to write the files.
	 *
	 * @return the header
	 */
	@JsonIgnore
	default String getHeader() {
		return null; 
	}
	
	/**
	 *  
	 * The csv line for this entity if it is imported by bulk import.
	 *
	 * @return the string
	 */
	@JsonIgnore
	default String toCsv() {
		return null; 
	}
	
	/**
	 * Interleave the values with the delimiter and return
	 * as a string without a terminating delim.
	 * @param values
	 * @return
	 */
	default String delimify(Object... values) {
		
		StringBuilder buf = new StringBuilder();
		buf.append(values[0]);
		if (values.length>1) {
			for (int i = 1; i < values.length; i++) {
				buf.append(getDelimiter());
				buf.append(values[i]);
			}
		}
		return buf.toString();
	}
	
	/**
	 * Override to change delimiter for an object.
	 * @return
	 */
	default String getDelimiter() {
		// We purposely use a character unlikely, the default character "," appears in some values.
		// You can override the delimier or set it if not writing bulk import files.
		return System.getProperty("delimiter", "|");// Character used for delimiter in bulk import files.
	}
}
