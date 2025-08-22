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

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Species {

	Integer getSpecies();
	
	void setSpecies(Integer species);
	
	/**
	 * We do this to save space in neo4j.
	 * @return
	 */
	@JsonIgnore
	default String getSpeciesName() {
		return getSpeciesName(getSpecies());
	}
	
	/**
	 * We use the ensembl species codes which
	 * take up less space (1 int) in neo4j
	// @see https://useast.ensembl.org/info/about/species.html
	 * @param code
	 * @return
	 */
	@JsonIgnore
	static String getSpeciesName(Integer code) {
		return SpeciesData.name(code);
	}

	@JsonIgnore
	default Long taxon() {
		return Long.valueOf(getSpecies());
	}

	/**
	 * We use the ensembl species codes which
	 * take up less space
	 * @see https://useast.ensembl.org/info/about/species.html
	 * @param code
	 * @return
	 */
	@JsonIgnore
	static int code(String species) {
		return SpeciesData.code(species);
	}
}

