package org.jax.gweaver.domain;

public abstract class AbstractEntity implements Entity {

	private String delimiter;
	
	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		// We purposely use a character unlikely, the default character "," appears in some values.
		// You can override the delimier or set it if not writing bulk import files.
		if (delimiter==null) return System.getProperty("delimiter", "±");// Character used for delimiter in bulk import files.
		
		// If one ios set e.g. "," we use that
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public AbstractEntity setDelimiter(String delimiter) {
		this.delimiter = delimiter;
		return this;
	}
}
