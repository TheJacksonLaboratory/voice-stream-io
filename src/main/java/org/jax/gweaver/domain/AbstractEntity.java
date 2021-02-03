package org.jax.gweaver.domain;

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
}
