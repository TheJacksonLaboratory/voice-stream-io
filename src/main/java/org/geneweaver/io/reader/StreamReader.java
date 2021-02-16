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

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;

/**
 * 
 * Class which reads large files as a stream which if they are processed
 * correctly will mean that the whole data is never in memory.
 * 
 * All readers must have a no-argument constructor and an init(...) method
 * to create the reader.
 * 
 * @author gerrim
 *
 * @param <T>
 */
public interface StreamReader<T extends Entity> {
	
	/**
	 * The name of the init method.
	 */
	String INIT = "init";

	/**
	 * All readers must have a create method which is called
	 * by the reader factory after the no-argument constructor.
	 * @param request
	 */
	public <R extends StreamReader<T>> R init(ReaderRequest request) throws ReaderException;
	
	/**
	 * Create a stream of domain objects which may be processed
	 * into a data-structure without holding the data in memory.
	 * For example make a stream from a variant file, set the connector
	 * to find links using a VariantConnector and flat map on the stream 
	 * then pipe each object to a row in a bulk import file.
	 * 
	 * @throws ReaderException
	 * @return stream of type we are parsing.
	 */
	Stream<T> stream() throws ReaderException;

	/**
	 * Most reader types will have a default connector.
	 * The connector is a way to process the stream of objects 
	 * using a flat map operation. Some readers may not have a 
	 * default connector.
	 * 
	 * @param <U>
	 * @return
	 */
	<U extends Entity> Function<T, Stream<U>> getDefaultConnector();
	
	/**
	 * Get the lines processed by this reader. This is mostly
	 * used to test that the expected lines from a large file were
	 * processed.
	 * 
	 * @return number of active (non-comment or header) lines.
	 */
	int linesProcessed();
	
	/**
	 * If the scanner is rereadable, stream() may be called more than once and it will 
	 * re initiate the scanner again. Scanners created with InputStreams do
	 * not hold Data Source and can be read only once. They return false here.
	 * @return false if we do not hold a data source and are callable only once.
	 */
	boolean isDataSource();

	/**
	 * If this reader does not have it data source and the iteration has 
	 * happened then stream() will throw an exception and isEmpty() will be true.
	 * @return
	 */
	boolean isEmpty();

	/**
	 * Call to close resources. May do nothing if steam is exhausted.
	 * Readers are intentionally not auto closable because most can clean up their
	 * own resources on the last iteration.
	 */
	void close() throws IOException;

	/**
	 * Chunk size to use with this reader
	 * @return
	 */
	int getChunkSize();
	
	/**
	 * Set the chunk size.
	 * @param chunkSize
	 */
	void setChunkSize(int chunkSize);

	/**
	 * Some readers support the ability to wind forward through the 
	 * stream of items in chunks. In the case where the read is 
	 * writing a single transaction, this may be faster than a pure stream.
	 * 
	 * @return the next chunk of data.
	 */
	List<T> wind() throws ReaderException;

}
