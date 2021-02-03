package org.jax.gweaver.domain;

import java.util.Collection;

public interface AnchoredEntity extends Entity {

	/**
	 * Call to get Anchors
	 * @return
	 */
	Collection<Anchor> anchors();
}
