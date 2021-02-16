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
package org.geneweaver.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.geneweaver.io.TimeInfo;
import org.geneweaver.io.Timer;
import org.junit.Test;

/**
 * Test made primarily to get coverage up to be honest.
 * @author gerrim
 *
 */
public class TimerTest {
	
	@Test
	public void time10() throws Exception {
		
		List<String> times = new ArrayList<>();
		Timer timer = new Timer(times::add);
		timer.setTimedChunkSize(1);
		for (int i = 0; i < 10; i++) {
			timer.time();
		}
		assertEquals(10, times.size());
	}
	
	@Test
	public void timeInfo10() throws Exception {
		
		List<String> times = new ArrayList<>();
		Timer timer = new Timer(times::add);
		timer.setTimedChunkSize(1);
		
		TimeInfo info = new TimeInfo();
		for (int i = 0; i < 10; i++) {
			info.increment();
			timer.time(info);
		}
		info.stop();
		assertEquals(10, times.size());
	}

	@Test
	public void formatNow() throws Exception {
		
		Timer timer = new Timer();
		String stime = timer.getFormattedTime();
		assertNotNull(stime);
	}
	
	@Test
	public void formatThen() throws Exception {
		
		Timer timer = new Timer();
		String stime = timer.getFormattedTime(System.currentTimeMillis());
		assertNotNull(stime);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void formatBad1() throws Exception {
		
		Timer timer = new Timer();
		String stime = timer.getFormattedTime(-Long.MAX_VALUE);
		assertNotNull(stime);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void formatBad2() throws Exception {
		
		Timer timer = new Timer();
		String stime = timer.getFormattedTime(100);
		assertNotNull(stime);
	}

}
