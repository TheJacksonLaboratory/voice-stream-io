package org.jax.gweaver.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

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
