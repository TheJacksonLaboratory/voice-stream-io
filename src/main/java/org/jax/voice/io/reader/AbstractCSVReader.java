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
package org.jax.voice.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.jax.voice.domain.Entity;

public abstract class AbstractCSVReader<T> implements StreamReader<T> {

	private ReaderRequest request;
	private int lines;
	
	private List<String> headerOverride;

	@SuppressWarnings("unchecked")
	@Override
	public AbstractCSVReader<T> init(ReaderRequest request) {
		if (request.getDelimiter()==null) request.setDelimiter(",");
		this.request = request;
		this.lines = 0;
		return this;
	}
	
	/**
	 * Parse the line to type T.
	 *
	 * @param line the line
	 * @return the t
	 * @throws ReaderException the reader exception
	 */
	protected abstract T create(CSVRecord row) throws ReaderException;
		 
	@Override
	public Stream<T> stream() throws ReaderException {
		
		try {
			Reader in = createReader(request);
			
			char delim = request.getDelimiter().charAt(0);
			Iterable<CSVRecord> records =  getFormat(delim).parse(in);
			
			return StreamSupport.stream(records.spliterator(), false)
								
								// Make sure we close
						 		.onClose(()->IOUtils.closeQuietly(in, ex->{throw new RuntimeException(ex);}))
	 							
						 		// For each record, make the object
						 		.map(rec->{
	 								try{
	 									return create(rec);
	 								} catch (Exception ne) {
	 									throw new RuntimeException(ne);
	 								}
	 							})
						 		
						 		// Null is a way of saying the line is invalid and legal
	 							.filter(m->m!=null)
	 							
	 							// Count the objects made.
	 							.map(t->{
	 								this.lines++;
	 								return t;
	 							});
			
			
		} catch (IOException ne) {
			throw new ReaderException(ne);
		}
	}
	
	public List<String> headers() throws ReaderException {

		if (headerOverride!=null) return headerOverride;
		try {
			Reader in = createReader(request);
			
			char delim = request.getDelimiter().charAt(0);
			return  getFormat(delim)
						 .parse(in)
						 .getHeaderNames();
		} catch (IOException ne) {
			throw new ReaderException(ne);
		}

	}
	
	private CSVFormat getFormat(char delim) {

		Builder builder = CSVFormat.DEFAULT.builder()
				.setCommentMarker('#');
		
		if (headerOverride!=null) {
			builder = builder.setHeader(headerOverride.toArray(new String[headerOverride.size()]))
					.setSkipHeaderRecord(false); // It's not a header
		} else {
			builder = builder.setHeader()
			.setSkipHeaderRecord(true); // It's  a header
		}
				
		builder	= builder.setDelimiter(delim)
				.setTrim(true)
				.setIgnoreEmptyLines(true)
				.setTrailingDelimiter(true);
		
		if (!request.isIncludeAll()) {
			builder = builder.setAllowMissingColumnNames(true);
		}
		return builder.build();
	}

	private BufferedReader createReader(ReaderRequest req) throws IOException {
		
		InputStream in;
		boolean gz = false;
		if (req.getStream()!=null) {
			in = req.getStream();
			gz = req.getName().toLowerCase().endsWith(".gz");
		} else {
			in = Files.newInputStream(req.getFile().toPath());
			gz = req.getFile().getName().toLowerCase().endsWith(".gz");
		}
		if (gz) {
			in = new GZIPInputStream(in);
		}
		
		return new BufferedReader(new InputStreamReader(in));
	}


	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		return null;
	}

	@Override
	public int linesProcessed() {
		return lines;
	}

	@Override
	public boolean isDataSource() {
		return request.isFileRequest();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void close() throws IOException {
		// Stream uses onClose(...)
	}

	/**
	 * @return the species
	 */
	protected String getSpecies() {
		return request.getSource();
	}

	@Override
	public int getChunkSize() {
		return -1;
	}

	@Override
	public void setChunkSize(int chunkSize) {
		
	}

	@Override
	public List<T> wind() throws ReaderException {
		throw new ReaderException("Wind is not supported by "+getClass().getSimpleName());
	}

	/**
	 * @return the headerOverride
	 */
	public List<String> getHeaderOverride() {
		return headerOverride;
	}

	/**
	 * @param headerOverride the headerOverride to set
	 */
	public void setHeaderOverride(List<String> headerOverride) {
		this.headerOverride = headerOverride;
	}

	/**
	 * This reads the headers from the last comment line as a csv.
	 * It cannot be used for InputStream readers and will throw an
	 * exception in this case.
	 * @throws IOException 
	 * @throws ReaderException 
	 */
	public void readHeadersFromLastCommentLine() throws IOException, ReaderException {
		
		if (!request.isFileRequest()) throw new ReaderException("Reading headers from last comment line is only supported in file mode!");
		
		try(BufferedReader reader = createReader(request)) {
			String line = null;
			String previousline = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("#")) {
					previousline = previousline.substring(1);
					String delim = request.getDelimiter().substring(0, 1);
					String[] headers = previousline.split(delim);
					setHeaderOverride(Arrays.asList(headers));
					return;
				}
					
				previousline = line;
			}
			throw new ReaderException("Last line of headers not found!");
		}
	}

}
