package org.geneweaver.domain;

import java.util.Map;
import java.util.UUID;

/**
 * Some entities have a location.
 * @author gerrim
 *
 */
public interface Located extends IdGenerator {

	/**
	 * The start location (base pairs).
	 * @return
	 */
	Integer getStart();
	
	/**
	 * The end location (base pairs).
	 * @return
	 */
	Integer getEnd();
	
	/**
	 * The chromosome on which the entity is located.
	 * @return
	 */
	String getChr();
	
	/**
	 * This method returns any additional metadata
	 * which we should set on the child object which we
	 * create with this location. It is stored as an optional 
	 * fourth column in the database as json. Using it can
	 * build up the database and it should not always be set.
	 * @return map of metadata values e.g. extra properties to set on EQTLOverlap.
	 */
	default Map<String,Object> getMeta() {
		return null;
	}

	static Located at(String chr, Integer start, Integer end) {
		final UUID rand = UUID.randomUUID();
		return new Located() {
			
			@Override
			public String id() {
				return rand.toString();
			}
			
			@Override
			public Integer getStart() {
				return start;
			}
			
			@Override
			public Integer getEnd() {
				return end;
			}
			
			@Override
			public String getChr() {
				return chr;
			}
		};
	}
}
