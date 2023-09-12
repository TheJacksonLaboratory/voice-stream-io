package org.geneweaver.domain;

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
