package org.jax.gweaver.domain;

import java.util.Objects;

public abstract class AbstractEntity implements Entity {

	// We purposely use a character unlikely, the default character "," appears in some values.
	// You can override the delimier or set it if not writing bulk import files.
	private String delimiter = System.getProperty("delimiter", "±");// Character used for delimiter in bulk import files.
	
	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		
		// If one ios set e.g. "," we use that
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
		return Objects.hash(delimiter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AbstractEntity))
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		return Objects.equals(delimiter, other.delimiter);
	}
}
