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

// TODO: Auto-generated Javadoc
/**
 * Simple object for timing line node adding.
 * 
 * @author gerrim
 *
 */
class TimeInfo implements AutoCloseable {

	/** The start. */
	private long start;
	
	/** The stop. */
	private long stop;
	
	/** The count. */
	private long count;
	
	/**  If we are counting the full amount or a chunk (default) *. */
	private boolean fullCount; 
	
	/**
	 * Instantiates a new time info.
	 */
	public TimeInfo() {
		this(false);
	}
	
	/**
	 * Instantiates a new time info.
	 *
	 * @param fullCount the full count
	 */
	public TimeInfo(boolean fullCount) {
		this.fullCount = fullCount;
		this.start = System.currentTimeMillis();
		this.count = 0;
		this.stop = Integer.MIN_VALUE;
	}
	
	/**
	 * Increment.
	 *
	 * @param <T> the generic type
	 * @param ignored the ignored
	 * @return the t
	 */
	public <T> T increment(T ignored) {
		increment();
		return ignored;
	}
	
	/**
	 * Increment.
	 *
	 * @return the long
	 */
	public long increment() {
		return ++count;
	}
	
	/**
	 * Stop.
	 */
	public void stop() {
		stop = System.currentTimeMillis();
	}
	
	/**
	 * Close.
	 */
	public void close() {
		stop();
	}

	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 *
	 * @param start the start to set
	 * @return the time info
	 */
	public TimeInfo setStart(long start) {
		this.start = start;
		return this;
	}

	/**
	 * Gets the stop.
	 *
	 * @return the stop
	 */
	public long getStop() {
		return stop;
	}

	/**
	 * Sets the stop.
	 *
	 * @param stop the stop to set
	 * @return the time info
	 */
	public TimeInfo setStop(long stop) {
		this.stop = stop;
		return this;
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 *
	 * @param count the count to set
	 * @return the time info
	 */
	public TimeInfo setCount(long count) {
		this.count = count;
		return this;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + (int) (stop ^ (stop >>> 32));
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeInfo other = (TimeInfo) obj;
		if (count != other.count)
			return false;
		if (start != other.start)
			return false;
		if (stop != other.stop)
			return false;
		return true;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public long getTime() {
		return stop-start;
	}

	/**
	 * Checks if is full count.
	 *
	 * @return the fullCount
	 */
	public boolean isFullCount() {
		return fullCount;
	}

	/**
	 * Sets the full count.
	 *
	 * @param fullCount the fullCount to set
	 */
	public void setFullCount(boolean fullCount) {
		this.fullCount = fullCount;
	}
	
	
}
