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
package org.geneweaver.domain;

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
