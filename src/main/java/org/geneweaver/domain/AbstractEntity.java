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

import org.geneweaver.io.connector.ChromosomeService;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class AbstractEntity implements Entity {
	
	private static ChromosomeService cservice = ChromosomeService.getInstance();

	/** The uid. */
	@Id
	@GeneratedValue
	@JsonInclude(JsonInclude.Include.NON_NULL)
    private Long uid;

	// We purposely use a character unlikely, the default character "," appears in some values.
	// You can override the delimier or set it if not writing bulk import files.
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String delimiter = System.getProperty("delimiter", "|");// Character used for delimiter in bulk import files.
	
	/**
	 * The chromosome on which this entity belongs.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String chr;
	
	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		
		// If one is set e.g. "," we use that
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(chr, delimiter, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AbstractEntity))
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		return Objects.equals(chr, other.chr) && Objects.equals(delimiter, other.delimiter)
				&& Objects.equals(uid, other.uid);
	}

	/**
	 * @return the uid
	 */
	public final Long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public final void setUid(Long uid) {
		this.uid = uid;
	}

	/**
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}

	/**
	 * @param chr the chr to set
	 */
	public void setChr(String chr) {
		setChr(chr, false);
	}
	
	/**
	 * @param chr the chr to set
	 */
	@JsonIgnore
	public void setChr(String chr, boolean force) {
		// We standardize chromosome
		if (!force) chr = cservice.getChromosome(chr);
		this.chr = chr;
	}

}
