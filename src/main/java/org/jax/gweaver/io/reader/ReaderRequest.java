package org.jax.gweaver.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.annotation.processing.Generated;

import org.jax.gweaver.domain.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Get a reader for a given request. This object holds all the required information
 * for determining which concrete reader should be used for a given format.
 * 
 * @author gerrim
 *
 */
@Generated("POJO")
public class ReaderRequest {

	
	private String species;
	private File file;
	
	@JsonIgnore
	private InputStream stream;
	
	private String name;
	
	/**
	 * Some readers can be verbose or truncated.
	 */
	private boolean includeAll = true;
	
	private int expectedSize;
	
	/**
	 * objType is only used for test requests.
	 */
	@JsonIgnore
	private Class<? extends Entity> objType;
	
	public ReaderRequest() {
		
	}
	
	public ReaderRequest(String species, File file) {
		this(species, file, true);
	}
	
	public ReaderRequest(String species, File file, boolean includeAll) {
		this.species = species;
		this.file = file;
		this.includeAll = includeAll;
	}
	
	public ReaderRequest(String species, InputStream stream, String name) {
		this.species = species;
		this.stream = stream;
		this.name = name;
	}
	
	public ReaderRequest(String species, int expectedSize, Class<? extends Entity> objType) {
		this.species = species;
		this.expectedSize = expectedSize;
		this.objType = objType;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @param species the species to set
	 */
	public void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file!=null ? file.getAbsoluteFile() : null;
	}

	/**
	 * @return the stream
	 */
	@JsonIgnore
	public InputStream getStream() {
		return stream;
	}

	/**
	 * @param stream the stream to set
	 */
	@JsonIgnore
	public void setStream(InputStream stream) {
		this.stream = stream;
	}

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
	 * @return the includeAll
	 */
	public boolean isIncludeAll() {
		return includeAll;
	}

	/**
	 * @param includeAll the includeAll to set
	 */
	public void setIncludeAll(boolean includeAll) {
		this.includeAll = includeAll;
	}

	@Override
	public int hashCode() {
		return Objects.hash(expectedSize, file, includeAll, name, species);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReaderRequest))
			return false;
		ReaderRequest other = (ReaderRequest) obj;
		return expectedSize == other.expectedSize && Objects.equals(file, other.file) && includeAll == other.includeAll
				&& Objects.equals(name, other.name) && Objects.equals(species, other.species);
	}

	@JsonIgnore
	String name() {
		if (file!=null) return file.getName();
		if (name!=null) return name;
		throw new IllegalArgumentException("A reader request must have a name for the resource!");
	}

	@JsonIgnore
	public InputStream stream() throws FileNotFoundException {
		if (file!=null) return new FileInputStream(file);
		if (stream!=null) return stream;
		return null;
	}

	@JsonIgnore
	public boolean isFileRequest() {
		return file!=null;
	}

	@JsonIgnore
	public void close() throws IOException {
		if (stream!=null) stream.close();
		stream = null;
	}

	/**
	 * @return the objType
	 */
	@JsonIgnore
	protected Class<? extends Entity> getObjType() {
		return objType;
	}

	/**
	 * @param objType the objType to set
	 */
	@JsonIgnore
	protected void setObjType(Class<? extends Entity> objType) {
		this.objType = objType;
	}

	/**
	 * @return the expectedSize
	 */
	public int getExpectedSize() {
		return expectedSize;
	}

	/**
	 * @param expectedSize the expectedSize to set
	 */
	public void setExpectedSize(int expectedSize) {
		this.expectedSize = expectedSize;
	}
	
}
