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
package org.geneweaver.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import javax.annotation.processing.Generated;

import org.geneweaver.domain.Entity;

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
	 * If the reader is reading a zip or tar file, setting the filter
	 * means that if the entry has more files than required,
	 * those not matching the filter can be ignored when the reader
	 * streams the data from the file.
	 * e.g. "^.+\\.egenes\\.txt(\\.gz)?$" to take only egenes files.
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

	@JsonIgnore
	private transient Matcher matcher;
	
	/**
	 * Delimiter for parsing text files.
	 */
	private String delimiter = null; // Use the default of the reader.
	
	/**
	 * The hint for which reader to use. It should be the full class name,
	 * the simple name or a string which is contained in the name for instance:
	 * "org.geneweaver.io.reader.MapCSVReader"
	 * "MapCSVReader"
	 * "Map"
	 * Obviously if hinting with a simpler string, you are a bit less likely to get the
	 * reader you prefer but more likely to be future proof if underlying readers change.
	 */
	private String readerHint;
	
	
	public ReaderRequest() {
		
	}
	
	public ReaderRequest(File file)  {
		this(null, file, true);
	}
	
	public ReaderRequest(File file, String fileFilter)  {
		this(null, file, true);
		this.fileFilter = fileFilter;
	}

	public ReaderRequest(URL url) throws IOException  {
		this.stream = url.openStream();
		this.name = Paths.get(url.toString()).getFileName().toString();
		this.file = null; // It's not a file.
	}

	public ReaderRequest(String source, File file)  {
		this(source, file, true);
	}
	
	public ReaderRequest(String source, File file, String delimiter)  {
		this(source, file, true);
		setDelimiter(delimiter);
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
	public ReaderRequest setSource(String species) {
		this.source = species;
		return this;
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
	public ReaderRequest setFile(File file) {
		this.file = file!=null ? file.getAbsoluteFile() : null;
		return this;
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
	public ReaderRequest setStream(InputStream stream) {
		this.stream = stream;
		return this;
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
	public ReaderRequest setName(String name) {
		this.name = name;
		return this;
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
	public ReaderRequest setIncludeAll(boolean includeAll) {
		this.includeAll = includeAll;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(closeInputStream, expectedSize, file, fileFilter, includeAll, initRequired, name,
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
				&& Objects.equals(name, other.name) && noInputStream == other.noInputStream
				&& Objects.equals(source, other.source);
	}

	@JsonIgnore
	String name() {
		if (file!=null) return file.getName();
		if (name!=null) return name;
		throw new IllegalArgumentException("A reader request must have a name for the resource!");
	}
	
	@JsonIgnore
	String path() {
		if (file!=null) return file.getAbsolutePath();
		return toString();
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
	public ReaderRequest setExpectedSize(int expectedSize) {
		this.expectedSize = expectedSize;
		return this;
	}

	@Override
	public String toString() {
		return "ReaderRequest [source=" + source + ", file=" + file + ", stream=" + stream
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
	public ReaderRequest setNoInputStream(boolean noInputStream) {
		this.noInputStream = noInputStream;
		return this;
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
	public ReaderRequest setCloseInputStream(boolean closeInputStream) {
		this.closeInputStream = closeInputStream;
		return this;
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
	public ReaderRequest setInitRequired(boolean initRequired) {
		this.initRequired = initRequired;
		return this;
	}

	/**
	 * @return the fileFilter
	 */
	public String getFileFilter() {
		return fileFilter;
	}

	/**
	 * @param fileFilter the fileFilter to set
	 */
	public ReaderRequest setFileFilter(String fileFilter) {
		this.fileFilter = fileFilter;
		return this;
	}

	@JsonIgnore
	public Matcher getMatcher() {
		return matcher;
	}

	/**
	 * @param matcher the matcher to set
	 */
	@JsonIgnore
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the readerHint
	 */
	public String getReaderHint() {
		return readerHint;
	}

	/**
	 * @param readerHint the readerHint to set
	 */
	public void setReaderHint(String readerHint) {
		this.readerHint = readerHint;
	}
	
}
