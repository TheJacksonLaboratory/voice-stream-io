/*-
 * 
 * Copyright 2018, 2020  The Jackson Laboratory Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Matthew Gerring
 */
package org.jax.voice.domain;

import java.util.Map;
import java.util.stream.Collectors;

final class SpeciesData {
	// @see https://useast.ensembl.org/info/about/species.html
	// @see https://www.ncbi.nlm.nih.gov/Taxonomy/Browser
	// We cannot deal with that many species in our graph for now.
	// Later this map can be replaced with a service call or a larger
	// cache or another mechanism.
	public static final Map<String, Integer> species;
	public static final Map<Integer, String> codes;
	static {
		codes = Map.of(
				10090, "Mus musculus", 
				9606, "Homo sapiens",
				10116, "Rattus norvegicus",
				7955, "Danio rerio",
				9598, "Pan troglodytes");
		species = codes.entrySet().stream()
		                    .collect(Collectors.toMap(
		                    	e->e.getValue().toLowerCase(), 
		                        Map.Entry::getKey     
		                    ));
	}
	
	/**
	 * Get code for species, case insensitive
	 * @param sspecies
	 * @return
	 */
	public static Integer code(String sspecies) {
		if (sspecies==null) return null;
		sspecies = sspecies.toLowerCase();
		return species.get(sspecies);
	}
	
	/**
	 * Get name for code.
	 * @param code
	 * @return spcies or null if not found.
	 */
	public static String name(Integer code) {
		if (code==null) return null;
		return codes.get(code);
	}
}
