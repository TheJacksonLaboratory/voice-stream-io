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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class Timer {

	
	// Fields used for timing only.
	private volatile long inodes;
	private volatile long curT5;
	private volatile long start;
	
	private long timedChunkSize = 10000L;
	private Consumer<String> consumer;
	
	public Timer() {
		this(System.out::println);
	}
	
	public Timer(Consumer<String> consumer) {
		this.inodes = 0;
		this.start = System.currentTimeMillis();
		this.consumer = consumer;
	}

	
	/**
	 * Used for synchronous (non-chunked)
	 * only.
	 */
	public void time() {
		++inodes;
		if (inodes%timedChunkSize==0) {
			printTiming();
		}
	}
	
	/**
	 * Used for async chunks.
	 * @param info
	 */
	public void time(TimeInfo info) {
		time(info.getCount());
	}
	
	private void time(long count) {

		inodes += count;
		
		// % operator will not work here because of multiple threads
		// means a nice ordered procedure through the lines is not happening.
		long t5 = inodes/timedChunkSize;
		if (t5>curT5) {
			if (count<=0) return;
			printTiming();
			curT5 = t5;
		}
	}

	private void printTiming() {
		long time = System.currentTimeMillis()-start;
		double tpn = ((double)time)/inodes;
		
		String msg = String.format("Total %d in %d ms. Time per node %.2f ms", inodes, time, tpn);
		consumer.accept(msg);
	}

	public void setTimedChunkSize(long chunkSize) {
		timedChunkSize = chunkSize;
	}
	
	/**
	 * 
	 * @return formatted time compared to when this timer was made.
	 */
	public String getFormattedTime() {
		return getFormattedTime(System.currentTimeMillis());
	}

	/**
	 * @param ltime to compare to when this timer was made.
	 * @return formatted time compared to when this timer was made.
	 */
	public String getFormattedTime(long ltime) {
		
		if (ltime<start) throw new IllegalArgumentException("The time must be > when we started to time!");
		long millis = ltime-start;
		String time = String.format("%d:%d", 
			    TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
			);

		return time;
	}

}
