package org.jax.voice.io.connector;

import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.Located;
import org.jax.voice.domain.Variant;

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
	<T extends AbstractEntity> T create(Located loc, Variant variant);

}
