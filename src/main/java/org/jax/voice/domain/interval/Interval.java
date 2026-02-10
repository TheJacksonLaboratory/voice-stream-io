package org.jax.voice.domain.interval;

import java.io.Serializable;
import java.util.Map;

public record Interval(int start, 
		int end, 
		Object id, 
		String chr, 
		Map<String, Object> meta) implements Serializable {
	
	
}