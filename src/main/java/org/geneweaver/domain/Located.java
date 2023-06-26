package org.geneweaver.domain;

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
}
