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
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVRecord;
import org.geneweaver.domain.Entity;

public abstract class AbstractCSVReader<T extends Entity> implements StreamReader<T> {

	private ReaderRequest request;

	@Override
	public AbstractCSVReader<T> init(ReaderRequest request) {
		this.request = request;
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
	public Stream<T> stream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int linesProcessed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDataSource() {
		return request.isFileRequest();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the species
	 */
	protected String getSpecies() {
		return request.getSource();
	}

}
