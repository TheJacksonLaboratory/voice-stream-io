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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;

/**
 * Base class for all reader implementations.
 * @author gerrim
 *
 * @param <T>
 */
public abstract class AbstractStreamReader<T extends Entity> implements StreamReader<T> {

	protected ReaderRequest request;
	protected String entryName; // When reading from an archive, the entry name is set by the archive reader.

	/**
	 * Amount to wind forward when using multi-threading.
	 * This is the maximum amount which one thread will tackle
	 * in a single job.
	 */
	protected int chunkSize = Integer.getInteger("org.geneweaver.io.windForward", 4096);

	/**
	 * All readers must have a create method which is called
	 * by the reader factory after the no-argument constructor.
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	public <R extends StreamReader<T>> R init(ReaderRequest request) throws ReaderException {
		this.request = request;
		return (R)this;
	}

	/**
	 * Gets the chunk size.
	 *
	 * @return the windForwardAmount
	 */
	public int getChunkSize() {
		return chunkSize;
	}
	
	/**
	 * Set the amount each thread will attempt to wind forward for
	 * its chunk. Adjusting this affects execution time for larhge runs.
	 *
	 * @param windForwardAmount the new chunk size
	 */
	public void setChunkSize(int windForwardAmount) {
		this.chunkSize = windForwardAmount;
	}

	private Iterator<T> iterator;
	
	/**
	 * Used to wind forward somewhere between 0 and the chunk size.
	 * @return the items passed when winding forward.
	 */
	public List<T> wind() throws ReaderException {
		
		if (iterator==null || !iterator.hasNext()) iterator = stream().iterator();
		
		List<T> ls = new LinkedList<>(); // linked list is fast to add/iterate
		for (int i = 0; i < getChunkSize() && iterator.hasNext(); i++) {
			ls.add(iterator.next());
		}
		return ls;
	}

	/**
	 * @return the entryName
	 */
	public String getEntryName() {
		return entryName;
	}

	/**
	 * @param entryName the entryName to set
	 */
	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}
	
	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		throw new RuntimeException("No connector for "+getClass().getSimpleName());
	}
	

}
