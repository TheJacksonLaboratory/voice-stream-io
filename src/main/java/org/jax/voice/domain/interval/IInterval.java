package org.jax.voice.domain.interval;

import java.io.Serializable;

public interface IInterval extends Serializable {

	int start();
	
	int end();
	
	Object id();
}
