package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.jax.voice.domain.interval.FlatIntervalTree;
import org.jax.voice.domain.interval.Interval;
import org.jax.voice.io.IPrintStream;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.junit.Test;

public class IntervalMarshallTest extends AbstractDataFileTest {
	
	@Test
	public void testDir() throws Exception {
		Path from = getPath("data/eQTL/eqtloverlaps");
		Path to   = Paths.get("./tmp/eqtloverlaps/");
		FileUtils.deleteQuietly(to.toFile());
		to.toFile().mkdirs();
		FileUtils.copyDirectory(from.toFile(), to.toFile());
		
		List<Path> written = IntervalMarshall.createTrees(to);
		assertNotNull(written);
		assertEquals(21, written.size());
		
		checkChromosomeNames(to);
	}
	
	private void checkChromosomeNames(Path dir) throws IOException {
		// Check that there are no Na, nA nor na files. Only NA
		Optional<Path> found = Files.list(dir)
		     .filter(p->{
		    	 return p.getFileName().toString().contains(".Na.") || 
		    		    p.getFileName().toString().contains(".nA.") || 
		    		    p.getFileName().toString().contains(".na.") ||
			    	 	p.getFileName().toString().contains("_Na.") || 
		    		    p.getFileName().toString().contains("_nA.") || 
		    		    p.getFileName().toString().contains("_na.");
		     })
		     .findAny();
		assertTrue(found.isEmpty());
	}


	@Test
	public void groupsByTypeAndChrAndWritesTrees() throws Exception {
		Path dir = Files.createTempDirectory("IntervalMarshallCreateTrees-");
		try {
			// Variant chr 1 => 2 shards with 2 + 1 intervals
			Path v11 = dir.resolve("Variant_intervals.1.AbcDef.ser");
			Path v12 = dir.resolve("Variant_intervals.1.GhiJkl.ser");
			IntervalMarshall.saveIntervals(v11, List.of(
					new Interval(1, 2, "v1", "1", Map.of()),
					new Interval(5, 6, "v2", "1", Map.of())
			));
			IntervalMarshall.saveIntervals(v12, List.of(
					new Interval(10, 11, "v3", "1", Map.of())
			));

			// Variant chr X => 1 shard with 1 interval
			Path vX = dir.resolve("Variant_intervals.X.MnOpQr.ser");
			IntervalMarshall.saveIntervals(vX, List.of(
					new Interval(7, 9, "vx1", "X", Map.of())
			));

			// Gene chr 1 => 1 shard with 4 intervals
			Path g1 = dir.resolve("Gene_intervals.1.StUvWx.ser");
			IntervalMarshall.saveIntervals(g1, List.of(
					new Interval(20, 30, "g1", "1", Map.of()),
					new Interval(40, 50, "g2", "1", Map.of()),
					new Interval(60, 70, "g3", "1", Map.of()),
					new Interval(80, 90, "g4", "1", Map.of())
			));

			IntervalMarshall.createTrees(dir);

			Path variant1Tree = dir.resolve("Variant_1.ser");
			Path variantXTree = dir.resolve("Variant_X.ser");
			Path gene1Tree = dir.resolve("Gene_1.ser");

			assertTrue(Files.exists(variant1Tree));
			assertTrue(Files.exists(variantXTree));
			assertTrue(Files.exists(gene1Tree));

			assertEquals(3, readTreeSize(variant1Tree));
			assertEquals(1, readTreeSize(variantXTree));
			assertEquals(4, readTreeSize(gene1Tree));

			// A tiny behavioral check: query hits the interval we know exists.
			FlatIntervalTree v1tree = readTree(variant1Tree);
			assertEquals(1, v1tree.query(1, 1).size());
		} finally {
			// clean up
			try (var s = Files.list(dir)) {
				s.forEach(p -> {
					try {
						Files.deleteIfExists(p);
					} catch (Exception e) {
						// ignore
					}
				});
			}
			Files.deleteIfExists(dir);
		}
	}

	private static int readTreeSize(Path treeFile) throws Exception {
		return readTree(treeFile).size();
	}

	private static FlatIntervalTree readTree(Path treeFile) throws Exception {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(treeFile))) {
			return (FlatIntervalTree) ois.readObject();
		}
	}
	
	@Test
	public void makeTrees() throws Exception {
		Path from = getPath("data/tree");
		Path to   = Paths.get("./tmp/makeTree/");
		FileUtils.deleteQuietly(to.toFile());
		to.toFile().mkdirs();
		
		List<Path> written = IntervalMarshall.createTrees(from, to, IPrintStream.of(System.out), false);
		assertNotNull(written);
		assertEquals(1, written.size());
		assertEquals(1, Files.list(written.get(0).getParent()).count());
		
	}
	

}
