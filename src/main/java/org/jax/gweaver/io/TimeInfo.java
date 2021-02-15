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
 */
package org.jax.gweaver.io;

/**
 * Simple object for timing line node adding.
 * It is needed when parallel streams are used with readers, otherwise
 * it does not give any advantage over Timer.time().
 * 
 * @author gerrim
 *
 */
public class TimeInfo implements AutoCloseable {

	private long start;
	private long stop;
	private long count;
	
	/** If we are counting the full amount or a chunk (default) **/
	private boolean fullCount; 
	
	public TimeInfo() {
		this(false);
	}
	
	public TimeInfo(boolean fullCount) {
		this.fullCount = fullCount;
		this.start = System.currentTimeMillis();
		this.count = 0;
		this.stop = Integer.MIN_VALUE;
	}
	
	public <T> T increment(T ignored) {
		increment();
		return ignored;
	}
	
	public long increment() {
		return ++count;
	}
	
	public void stop() {
		stop = System.currentTimeMillis();
	}
	
	public void close() {
		stop();
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public TimeInfo setStart(long start) {
		this.start = start;
		return this;
	}

	/**
	 * @return the stop
	 */
	public long getStop() {
		return stop;
	}

	/**
	 * @param stop the stop to set
	 */
	public TimeInfo setStop(long stop) {
		this.stop = stop;
		return this;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public TimeInfo setCount(long count) {
		this.count = count;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + (int) (stop ^ (stop >>> 32));
		return result;
	}

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

	public long getTime() {
		return stop-start;
	}

	/**
	 * @return the fullCount
	 */
	public boolean isFullCount() {
		return fullCount;
	}

	/**
	 * @param fullCount the fullCount to set
	 */
	public void setFullCount(boolean fullCount) {
		this.fullCount = fullCount;
	}
	
	
}
