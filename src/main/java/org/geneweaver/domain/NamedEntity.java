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

import java.util.Objects;

public abstract class NamedEntity extends AbstractEntity implements Species {


	private String name;
	private Integer species;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the species
	 */
	public Integer getSpecies() {
		return species;
	}
	/**
	 * @param species the species to set
	 */
	public void setSpecies(Integer species) {
		this.species = species;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(name, species);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof NamedEntity))
			return false;
		NamedEntity other = (NamedEntity) obj;
		return Objects.equals(name, other.name) && species == other.species;
	}
}
