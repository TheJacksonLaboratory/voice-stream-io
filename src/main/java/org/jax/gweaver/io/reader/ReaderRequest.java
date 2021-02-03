package org.jax.gweaver.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.annotation.processing.Generated;

import org.jax.gweaver.domain.Entity;

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
	private InputStream stream;
	private String name;
	
	/**
	 * Some readers can be verbose or truncated.
	 */
	private boolean includeAll = true;
	
	private int expectedSize;
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
	protected String getSpecies() {
		return species;
	}

	/**
	 * @param species the species to set
	 */
	protected void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * @return the file
	 */
	protected File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	protected void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the stream
	 */
	protected InputStream getStream() {
		return stream;
	}

	/**
	 * @param stream the stream to set
	 */
	protected void setStream(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * @return the name
	 */
	protected String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the includeAll
	 */
	protected boolean isIncludeAll() {
		return includeAll;
	}

	/**
	 * @param includeAll the includeAll to set
	 */
	protected void setIncludeAll(boolean includeAll) {
		this.includeAll = includeAll;
	}

	@Override
	public int hashCode() {
		return Objects.hash(expectedSize, file, includeAll, name, objType, species, stream);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReaderRequest))
			return false;
		ReaderRequest other = (ReaderRequest) obj;
		return expectedSize == other.expectedSize && Objects.equals(file, other.file) && includeAll == other.includeAll
				&& Objects.equals(name, other.name) && Objects.equals(objType, other.objType)
				&& Objects.equals(species, other.species) && Objects.equals(stream, other.stream);
	}

	String name() {
		if (file!=null) return file.getName();
		if (name!=null) return name;
		throw new IllegalArgumentException("A reader request must have a name for the resource!");
	}

	public InputStream stream() throws FileNotFoundException {
		if (file!=null) return new FileInputStream(file);
		if (stream!=null) return stream;
		return null;
	}

	public boolean isFileRequest() {
		return file!=null;
	}

	public void close() throws IOException {
		if (stream!=null) stream.close();
		stream = null;
	}

	/**
	 * @return the objType
	 */
	protected Class<? extends Entity> getObjType() {
		return objType;
	}

	/**
	 * @param objType the objType to set
	 */
	protected void setObjType(Class<? extends Entity> objType) {
		this.objType = objType;
	}

	/**
	 * @return the expectedSize
	 */
	protected int getExpectedSize() {
		return expectedSize;
	}

	/**
	 * @param expectedSize the expectedSize to set
	 */
	protected void setExpectedSize(int expectedSize) {
		this.expectedSize = expectedSize;
	}
	
}
