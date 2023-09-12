package org.geneweaver.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IdGenerator {


	/**
	 * Get the value of the id field. 
	 * Not all entities will have the same field name
	 * for the id field but they all must have a value for it.
	 * e.g. 
	 * Gene the geneId from Ensembl.
	 * Variant the rsId used for all variants.
	 * Peak the generated peakId.
	 * @return id
	 */
	@JsonIgnore
	String id();

}
