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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.geneweaver.domain.Entity;

public abstract class AbstractCSVReader<T> implements StreamReader<T> {

	private ReaderRequest request;
	private static final CSVFormat format = CSVFormat.DEFAULT;
	private int lines;
	
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
	
	@SuppressWarnings("deprecation")
	private CSVFormat getFormat(char delim) {
		CSVFormat ret = format.withCommentMarker('#')
				  .withFirstRecordAsHeader()
				  .withDelimiter(delim)
				  .withTrim(true)
				  .withIgnoreEmptyLines()
				  .withTrailingDelimiter();
		if (!request.isIncludeAll()) {
			ret = ret.withAllowMissingColumnNames();
		}
		return ret;
	}

	private Reader createReader(ReaderRequest req) throws IOException {
		
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
}
