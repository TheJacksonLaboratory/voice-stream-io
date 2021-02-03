package org.jax.gweaver.io.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.jax.gweaver.domain.Entity;

/**
 * 
 * Class which reads large files as a stream which if they are processed
 * correctly will mean that the whole data is never in memory.
 * 
 * @author gerrim
 *
 * @param <T>
 */
public interface StreamReader<T extends Entity> {
	
	/**
	 * Create a stream of domain objects which may be processed
	 * into a datastructure without holding the data in memory.
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
	 */
	void close() throws IOException;

}
