package org.jax.voice.domain.interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class FlatIntervalTreeTest {

	@Test
	public void four() {

		Interval[] inter = {
		    new Interval(1, 2, "Q1", "8", Collections.emptyMap()),
		    new Interval(9, 16, "Q2", "X", null),
		    new Interval(19, 31, "Q3", "NA", Map.of("fred", 1)),
		    new Interval(100, 200, "Q4", "nA", null)  // no overlaps
		};
		FlatIntervalTree shard = new FlatIntervalTree(Arrays.asList(inter));
		
		assertTrue(shard.query(0,0).isEmpty());
		assertEquals(1, shard.query(0,1).size());
		assertEquals(1, shard.query(1,1).size());
		assertEquals(1, shard.query(1,2).size());
		assertEquals(1, shard.query(2,3).size());
		assertEquals(2, shard.query(1,9).size());
		assertEquals(2, shard.query(16,19).size());
		assertEquals(3, shard.query(2,19).size());
		assertEquals(1, shard.query(17,19).size());
		assertEquals(1, shard.query(100,100).size());
		assertEquals(1, shard.query(101,101).size());
		assertEquals(1, shard.query(100,200).size());
		assertEquals(1, shard.query(150,250).size());

	}
	
	@Test
	public void fourInside1mill() {

		List<Interval> all = new ArrayList<>(1000_000);
		all.add(new Interval(1, 2, "Q1", "8", Collections.emptyMap()));
		all.add(new Interval(9, 16, "Q2", "NA", null));
		all.add(new Interval(19, 31, "Q3", "1", Map.of("fred",1)));
		all.add(new Interval(100, 200, "Q4", null, null)); // no overlaps
		
		// Make it 100mill in size.
		// with things larger.
		for (int i = 0; i < 1000_000-4; i++) {
			double rand = Math.random();
			int scale = 500_000_000;
			
			int start = 251 + (int)Math.round(rand*scale);
			int end	  = 252 + (int)Math.round(rand*scale);
			all.add(new Interval(start, end, "X"+i, "NA", null));
		}
		
		FlatIntervalTree shard = new FlatIntervalTree(all);
		assertTrue(shard.query(0,0).isEmpty());
		assertEquals(1, shard.query(0,1).size());
		assertEquals(1, shard.query(1,1).size());
		assertEquals(1, shard.query(1,2).size());
		assertEquals(1, shard.query(2,3).size());
		assertEquals(2, shard.query(1,9).size());
		assertEquals(2, shard.query(16,19).size());
		assertEquals(3, shard.query(2,19).size());
		assertEquals(1, shard.query(17,19).size());
		assertEquals(1, shard.query(100,100).size());
		assertEquals(1, shard.query(101,101).size());
		assertEquals(1, shard.query(100,200).size());
		assertEquals(1, shard.query(150,250).size());

	}
	
	@Test
	public void oneMillAllSame() {

		List<Interval> all = new ArrayList<>(1000_000);
		
		// Make it 100mill in size.
		// with things larger.
		for (int i = 0; i < 1000_000; i++) {
			all.add(new Interval(10, 11, "X"+i, "NA", null));
		}
		
		FlatIntervalTree shard = new FlatIntervalTree(all);
		assertEquals(0, shard.query(0,1).size());
		assertEquals(0, shard.query(12,12).size());
		assertEquals(1000_000, shard.query(0,10).size());
		assertEquals(1000_000, shard.query(10,10).size());
		assertEquals(1000_000, shard.query(10,11).size());
		assertEquals(1000_000, shard.query(11,11).size());
		assertEquals(1000_000, shard.query(11,12).size());

	}
	
	@Test
	public void oneThousandAllSameSerialization() throws Exception {

		List<Interval> all = new ArrayList<>(1000);

		for (int i = 0; i < 1000; i++) {
			all.add(new Interval(10, 11, "X" + i, "NA", null));
		}

		FlatIntervalTree shard = new FlatIntervalTree(all);

		Path tmp = Files.createTempFile("FlatIntervalTree-", ".ser");
		try {
			try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tmp))) {
				oos.writeObject(shard);
			}

			FlatIntervalTree reloaded;
			try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(tmp))) {
				reloaded = (FlatIntervalTree)ois.readObject();
			}

			assertNotNull(reloaded);

			// Structural equality (FlatIntervalTree implements equals/hashCode)
			assertEquals(shard, reloaded);
			assertEquals(shard.hashCode(), reloaded.hashCode());

			// Behavioral equality on representative queries
			assertEquals(shard.query(0, 1).size(), reloaded.query(0, 1).size());
			assertEquals(shard.query(12, 12).size(), reloaded.query(12, 12).size());
			assertEquals(shard.query(0, 10).size(), reloaded.query(0, 10).size());
			assertEquals(shard.query(10, 10).size(), reloaded.query(10, 10).size());
			assertEquals(shard.query(10, 11).size(), reloaded.query(10, 11).size());
			assertEquals(shard.query(11, 11).size(), reloaded.query(11, 11).size());
			assertEquals(shard.query(11, 12).size(), reloaded.query(11, 12).size());
		} finally {
			Files.deleteIfExists(tmp);
		}
	}

	/**
	 * Run with -Xmx32g so that we can test intervals without OOM.
	 * We need about 500mill in one tree in the real build.
	 * 
	 * This must pass without a stack overflow if we are to use
	 * the algorithm for large builds on sumner.
	 */
	@Ignore
	@Test
	public void upperLimit() {

		int upper = 500_000_000;
		List<Interval> all = new ArrayList<>(upper);
		
		// Make it 100mill in size.
		// with things larger.
		for (int i = 0; i < upper; i++) {
			all.add(new Interval(i, i+1, "X"+i, "NA", null));
		}
		
		FlatIntervalTree shard = new FlatIntervalTree(all);
		assertEquals(0, shard.query(upper+10,upper+11).size());
		assertEquals(2, shard.query(1,1).size());

	}
	
	@Test
	public void boundariesAndReversedQuery() {
		Interval[] inter = {
			    new Interval(1, 2, "Q1", "8", Collections.emptyMap()),
			    new Interval(9, 16, "Q2", "X", null),
			    new Interval(19, 31, "Q3", "NA", Map.of("fred", 1)),
			    new Interval(100, 200, "Q4", "nA", null)  // no overlaps
		};
		FlatIntervalTree shard = new FlatIntervalTree(Arrays.asList(inter));

		// Boundary behavior consistent with the inclusive overlap used by the tests.
		assertEquals(1, shard.query(2, 2).size());  // touches end of [1,2]
		assertEquals(1, shard.query(9, 9).size());  // touches start of [9,16]
		assertEquals(1, shard.query(16, 16).size());
		assertEquals(1, shard.query(19, 19).size());

		// Query outside all
		assertTrue(shard.query(-10, -1).isEmpty());
		assertTrue(shard.query(201, 300).isEmpty());

		// Reversed query should behave the same as normalized.
		assertEquals(shard.query(1, 9).size(), shard.query(9, 1).size());
		assertEquals(shard.query(16, 19).size(), shard.query(19, 16).size());
	}

	@Test
	public void randomSmallCrossCheckAgainstNaive() {
		// Deterministic random test (small) to validate sort+query against a naive scan.
		java.util.Random rnd = new java.util.Random(1234);
		List<Interval> intervals = new ArrayList<>();
		for (int i = 0; i < 200; i++) {
			int a = rnd.nextInt(200);
			int b = rnd.nextInt(200);
			int start = Math.min(a, b);
			int end = Math.max(a, b);
			intervals.add(new Interval(start, end, "I" + i,"NA", null));
		}

		FlatIntervalTree shard = new FlatIntervalTree(intervals);

		for (int q = 0; q < 200; q++) {
			int a = rnd.nextInt(220) - 10;
			int b = rnd.nextInt(220) - 10;
			int qs = Math.min(a, b);
			int qe = Math.max(a, b);

			// Naive inclusive overlap: start <= qe && end >= qs
			int expected = 0;
			for (Interval in : intervals) {
				if (in.start() <= qe && in.end() >= qs) {
					expected++;
				}
			}

			assertEquals("Mismatch for query [" + qs + "," + qe + "]", expected, shard.query(qs, qe).size());
		}
	}
}