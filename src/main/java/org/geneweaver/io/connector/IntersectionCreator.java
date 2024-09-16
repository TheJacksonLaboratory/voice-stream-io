package org.geneweaver.io.connector;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Located;
import org.geneweaver.domain.Variant;

public interface IntersectionCreator {

	/**
	 * 
	 * @param <T>
	 * @param loc - The intersected object
	 * @param variant - The variant with which we are intersecting
	 * @param intersectRange - stat about closeness of relationship
	 * @param intersectFaction - stat about closeness of relationship
	 * @return the entiry or an exception if this intersection cannot be parsed.
	 */
	<T extends AbstractEntity> T create(Located loc, Variant variant, int intersectRange, float intersectFaction);

	
}
