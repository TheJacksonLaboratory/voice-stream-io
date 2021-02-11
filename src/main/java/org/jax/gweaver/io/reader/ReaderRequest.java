package org.jax.gweaver.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import javax.annotation.processing.Generated;

import org.apache.commons.io.FilenameUtils;
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

	/**
	 * Name of the source e.g. species or name of data source.
	 */
	private String source;
	
	/**
	 * The file in which the data is in.
	 */
	private File file;
	
	/**
	 * If a request needs an additional file to 
	 * provide a mapping, for instance to the default connector,
	 * then it should be set here.
	 * 
	 * This is usually a URL or a file.
	 */
	private Serializable mapping;
	
	/**
	 * Instead of file, the data may be an input stream.
	 */
	@JsonIgnore
	private InputStream stream;
	
	/**
	 * Name of the data, usually the file name.
	 */
	private String name;
	
	/**
	 * Some readers can be verbose or truncated.
	 */
	private boolean includeAll = true;
	
	/**
	 * If the reader is reading a tar file, setting the filter
	 * means that if the tar has more files in than required,
	 * those not matching the filter can be ignored when the reader
	 * streams the data from the file.
	 * e.g. "^.+\\.egenes\\.txt(\\.gz)?$"
	 */
	private String fileFilter;
	
	/**
	 * Used only testing, this field marks size for RepeatedLineReader which
	 * pumps the same line through the stream in order to test it.
	 */
	private int expectedSize;
	
	/**
	 * Set to allow no inputstream request. In which case the file source will not
	 * setup an input stream. 
	 */
	private boolean noInputStream = false;
	
	/**
	 * Normallly we would like to close the input stream
	 * as we read from it. In the case of reading from a tar or
	 * zip, we do not want to close the stream at reader time because
	 * other files will be read from it.
	 */
	private boolean closeInputStream = true;
	
	/**
	 * objType is only used for test requests.
	 */
	@JsonIgnore
	private Class<? extends Entity> objType;
	
	/**
	 * Calling init can be turned off.
	 */
	private boolean initRequired = true;
	
	
	public ReaderRequest() {
		
	}
	
	public ReaderRequest(File file)  {
		this(null, file, true);
	}
	
	public ReaderRequest(URL url) throws IOException  {
		this.stream = url.openStream();
		this.name = Paths.get(url.toString()).getFileName().toString();
		this.file = null; // It's not a file.
	}
	
	public ReaderRequest(File file, File mapping)  {
		this(null, file, true);
		this.mapping = mapping;
	}

	public ReaderRequest(File file, URL mapping)  {
		this(null, file, true);
		this.mapping = mapping;
	}

	public ReaderRequest(String source, File file)  {
		this(source, file, true);
	}
	
	public ReaderRequest(String source, Path path) throws IOException{
		this.source = source;
		
		if (path.startsWith("http:/") || path.startsWith("ftp:/")) {
			String spath = path.toString().replaceAll(":/", "://");
			URL url =  new URL(spath);
			this.stream = url.openStream();
			this.name = Paths.get(url.toString()).getFileName().toString();
			this.file = null; // It's not a file.
		} else {
			this.file = path.toFile();
		}
	}

	public ReaderRequest(String source, File file, boolean includeAll) {
		this.source = source;
		this.file = file;
		this.includeAll = includeAll;
	}
	
	public ReaderRequest(InputStream stream, String name) {
		if (stream instanceof GZIPInputStream) throw new IllegalArgumentException("There is no need to wrap inputstream in compressed streams!");
		this.stream = stream;
		this.name = name;
	}
	
	public ReaderRequest(InputStream stream, String name, boolean close) {
		if (stream instanceof GZIPInputStream) throw new IllegalArgumentException("There is no need to wrap inputstream in compressed streams!");
		this.stream = stream;
		this.name = name;
		this.closeInputStream = close;
	}
	
	public ReaderRequest(String source, InputStream stream, String name) {
		if (stream instanceof GZIPInputStream) throw new IllegalArgumentException("There is no need to wrap inputstream in compressed streams!");
		this.source = source;
		this.stream = stream;
		this.name = name;
	}
	
	public ReaderRequest(String source, int expectedSize, Class<? extends Entity> objType) {
		this.source = source;
		this.expectedSize = expectedSize;
		this.objType = objType;
	}

	public ReaderRequest(String name) {
		this.name = name;
	}

	/**
	 * @return the species
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param species the species to set
	 */
	public void setSource(String species) {
		this.source = species;
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
		return Objects.hash(closeInputStream, expectedSize, file, fileFilter, includeAll, initRequired, mapping, name,
				noInputStream, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReaderRequest))
			return false;
		ReaderRequest other = (ReaderRequest) obj;
		return closeInputStream == other.closeInputStream && expectedSize == other.expectedSize
				&& Objects.equals(file, other.file) && Objects.equals(fileFilter, other.fileFilter)
				&& includeAll == other.includeAll && initRequired == other.initRequired
				&& Objects.equals(mapping, other.mapping) && Objects.equals(name, other.name)
				&& noInputStream == other.noInputStream && Objects.equals(source, other.source);
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
		if (!isCloseInputStream()) return;
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

	/**
	 * @return the mapping
	 */
	@JsonIgnore
	public Serializable getMapping() {
		return mapping;
	}

	/**
	 * @param mapping the mapping to set
	 */
	@JsonIgnore
	public ReaderRequest setMapping(Serializable mapping) {
		this.mapping = mapping;
		return this;
	}

	/**
	 * Create an input stream for the mapping or none if no mapping file.
	 * @return
	 * @throws IOException
	 */
	@JsonIgnore
	public InputStream mappingInputStream() throws IOException {
		if (mapping==null) return null;
		if (mapping instanceof File) return new FileInputStream((File)mapping);
		if (mapping instanceof URL) return ((URL)mapping).openStream();
		return null;
	}
	
	/**
	 * Get the name from the mapping or null if no mapping.
	 * @return
	 * @throws IOException
	 */
	@JsonIgnore
	public String mappingName() throws IOException {
		if (mapping==null) return null;
		return FilenameUtils.getName(mapping.toString());
	}

	@Override
	public String toString() {
		return "ReaderRequest [source=" + source + ", file=" + file + ", mapping=" + mapping + ", stream=" + stream
				+ ", name=" + name + ", includeAll=" + includeAll + ", fileFilter=" + fileFilter + ", expectedSize="
				+ expectedSize + ", noInputStream=" + noInputStream + ", objType=" + objType + "]";
	}

	/**
	 * @return the noInputStream
	 */
	public boolean isNoInputStream() {
		return noInputStream;
	}

	/**
	 * @param noInputStream the noInputStream to set
	 */
	public void setNoInputStream(boolean noInputStream) {
		this.noInputStream = noInputStream;
	}

	/**
	 * @return the closeInputStream
	 */
	public boolean isCloseInputStream() {
		return closeInputStream;
	}

	/**
	 * @param closeInputStream the closeInputStream to set
	 */
	public void setCloseInputStream(boolean closeInputStream) {
		this.closeInputStream = closeInputStream;
	}

	/**
	 * @return the initRequired
	 */
	public boolean isInitRequired() {
		return initRequired;
	}

	/**
	 * @param initRequired the initRequired to set
	 */
	public void setInitRequired(boolean initRequired) {
		this.initRequired = initRequired;
	}
	
}
