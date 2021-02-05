package org.jax.gweaver.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class SpeciesData {
	// @see https://www.ncbi.nlm.nih.gov/Taxonomy/Browser
	// We cannot deal with that many species in our graph for now.
	// Later this map can be replaced with a service call or a larger
	// cache or another mechanism.
	public static final Map<String, Long> species;
	static {
		Map<String, Long> tmp = new HashMap<>();
		tmp.put("mus musculus", 10090L);
		tmp.put("homo sapiens", 9606L);
		tmp.put("rattus norvegicus", 10116L);
		tmp.put("danio rerio", 7955L);
		tmp.put("pan troglodytes", 9598L);
		species = Collections.unmodifiableMap(tmp);
	}
	public static Long get(String sspecies) {
		if (sspecies==null) return null;
		sspecies = sspecies.toLowerCase();
		return species.get(sspecies);
	}
}
