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
package org.jax.gweaver.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;


// TODO: Auto-generated Javadoc
/**
 * Any node or edge in our graph.
 * 
 * @author gerrim
 *
 */
public interface Entity {
	
	/** The Constant D. */
	// We purposely use a character unlikely, the default character "," appears in some values.
	public static final String D = System.getProperty("delimiter", "±"); // Character used for delimiter in csv files.

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
	public default boolean isRelationship() {
		return getClass().isAnnotationPresent(RelationshipEntity.class);
	}
	
	/**
	 * Checks if is node.
	 *
	 * @return true if this node is a node..
	 */
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
	default String getHeader() {
		return null; // TODO
	}
	
	/**
	 *  
	 * The csv line for this entity if it is imported by bulk import.
	 *
	 * @return the string
	 */
	default String toCsv() {
		return null; // TODO
	}
}
