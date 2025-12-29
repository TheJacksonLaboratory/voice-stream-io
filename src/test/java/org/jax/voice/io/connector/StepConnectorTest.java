package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jax.voice.domain.Contact;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Gene;
import org.jax.voice.domain.Step;
import org.jax.voice.domain.Variant;
import org.jax.voice.io.IPrintStream;
import org.jax.voice.io.connector.StepConnector;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.jax.voice.io.reader.ReaderException;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;
import org.jax.voice.io.writer.ExportBuilder;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class StepConnectorTest extends AbstractDataFileTest {

	
	@Test(expected=IllegalArgumentException.class)
	public void noAdd() throws Exception {
		try (StepConnector func = new StepConnector("Mus musculus", Gene.class)) {
			func.create(); // Creates indexed database.
		}
	}
	
	@Test(expected=FileNotFoundException.class)
	public void badGeneFile() throws Exception {
		Path mFile = getPath("data/1000/hs_gtf/hg38_2.gtf");
		Path hFile = getPath("data/NOTTHERE");
		try (StepConnector func = new StepConnector("Mus musculus", Gene.class)) {
			func.add(mFile);
			func.add(hFile);
		}
	}

	@Test
	public void aFewGenes() throws Exception {
		Path dir = getPath("data/1000/hs_gtf/");
		
		// Find how many genes there are and use this number in the 
		// database row size call after the parse.
		Path file = dir.resolve("hg38_2.gtf");
		StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(file.getFileName().toString(), file));
		long size = reader.stream().filter(e->e instanceof Gene).count();
		testCreate("aFewGenes", dir, 1, Gene.class, size);
	}
	
	@Test
	public void aFewVariants() throws Exception {
		Path dir = getPath("data/1000/hs_gvf/");
		testCreate("aFewVariants", dir, 1, Variant.class, 1000);
	}
	
	private void testCreate(String testName, Path ddir, int fileLimit, Class<? extends Entity> type, long size) throws Exception {
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());
		
		try (StepConnector func = new StepConnector("Mus musculus", type)) {
			func.setLocation(tdir);
			
			// limit is used here just to avoid caching all the test files i.e. test goes quicker.
			func.addAll(ddir, fileLimit);
			
			Stopwatch timer = Stopwatch.createStarted();
			func.create(); // Creates indexed database.
			Stopwatch done = timer.stop();
			long rsize = func.size();
			System.out.println("Created cache table size "+rsize+" in "+done);
			assertTrue("Size is "+rsize, rsize>=size && rsize<=(size*2)); // There can be repeats because of the sharding.
			assertTrue(done.elapsed().toMillis()<10000);
		}
	}

	/**
	 * Check that we can actually read variants from their txt.gz file.
	 * 
	 * @throws Exception
	 */
	@Test
	public void readAFewMouseCCSIVariants() throws Exception {
		variants("prod/steps/mm/snp137.txt.gz");
	}
	
	@Test
	public void readAFewHumanCCSIVariants() throws Exception {
		variants("prod/steps/hs/snp141.txt.gz");
	}
	
	private void variants(String path) throws IOException, ReaderException {
		Path file = getPath(path);
		ReaderRequest request = new ReaderRequest(file.getFileName().toString(), file);
		request.setDelimiter("\t");
		request.setIncludeAll(false);
		StreamReader<Map<String,Object>> reader = ReaderFactory.getReader(request);

		List<Variant> some = reader.stream()
								   .skip(100)
								   .limit(100)
								   .map(line->Entity.coerce(line, new Variant()))
								   .collect(Collectors.toList());
		
		Variant v0 = some.get(0);
		
		// Properties we need for step database.
		assertNotNull(v0.getStart());
		assertNotNull(v0.getEnd());
		assertNotNull(v0.id());
		assertNotNull(v0.getChr());
	}
	
	/**
	 * Check that we can actually read genes from their gtf file.
	 * 
	 * @throws Exception
	 */
	@Test
	public void readHumanCCSIGenes() throws Exception {
		Path file = getPath("prod/steps/hs/hg38.gtf.gz");
		ReaderRequest request = new ReaderRequest(file.getFileName().toString(), file);
		StreamReader<Gene> reader = ReaderFactory.getReader(request);
		assertEquals(60483, reader.stream().count());
		
		Gene agene = reader.stream()
						   .map(StepConnector::fixId)
						   .findFirst().get();
		assertNotNull(agene.getStart());
		assertNotNull(agene.getEnd());
		assertNotNull(agene.id());
		assertFalse(agene.id().contains("."));
		assertNotNull(agene.getChr());
	}

	@Test
	public void readMouseCCSIGenes() throws Exception {
		Path file = getPath("prod/steps/mm/mm10.gtf.gz");
		ReaderRequest request = new ReaderRequest(file.getFileName().toString(), file);
		StreamReader<Gene> reader = ReaderFactory.getReader(request);
		assertEquals(43346, reader.stream().count());
		
		Gene agene = reader.stream()
						   .map(StepConnector::fixId)
						   .findFirst().get();
		assertNotNull(agene.getStart());
		assertNotNull(agene.getEnd());
		assertNotNull(agene.id());
		assertFalse(agene.id().contains("."));
		assertNotNull(agene.getChr());
	}

	@Test
	public void mouseLimitedParse() throws Exception {
		
		String testName = "mouse";
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());

		Path file = getPath("prod/steps/mm/mm10.gtf.gz");
		testRealParse(testName, file, Gene.class, "ENS", 0L, 100000L);
		
		file = getPath("prod/steps/mm/snp137.txt.gz");
		testRealParse(testName, file, Variant.class, "rs", 10000L, 100000L);
		
		assertTrue(Files.exists(Paths.get("tmp/mouse/Variant_1.mv.db")));
		assertTrue(Files.exists(Paths.get("tmp/mouse/Gene_15.mv.db")));
		assertFalse(Files.exists(Paths.get("tmp/mouse/Gene_22.mv.db")));
	}
	
	@Test
	public void humanLimitedParse() throws Exception {
		
		String testName = "human";
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());

		Path file = getPath("prod/steps/hs/hg38.gtf.gz");
		testRealParse(testName, file, Gene.class, "ENS", 0L, 100000L);
		
		file = getPath("prod/steps/hs/snp141.txt.gz");
		testRealParse(testName, file, Variant.class, "rs", 10000L, 100000L);
		
		assertTrue(Files.exists(Paths.get("tmp/human/Variant_1.mv.db")));
		assertTrue(Files.exists(Paths.get("tmp/human/Gene_22.mv.db")));
	}
	
	// NOT FOR UNIT TESTS
	// But need to test parsing whole file so that can run StepConnector 
	// on cache.
	
	// Slow
	// @Test
	public void mouseFullVariantParse() throws Exception {
		
		String testName = "mouseFull";
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());

		Path file = getPath("prod/steps/mm/mm10.gtf.gz");
		testRealParse(testName, file, Gene.class,  "ENS", null, null);

		file = getPath("prod/steps/mm/snp137.txt.gz");
		testRealParse(testName, file, Variant.class, "rs", null, null);
	}

	// Slow
	// @Test
	public void humanFullVariantParse() throws Exception {
		
		String testName = "humanFull";
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());

		Path file = getPath("prod/steps/hs/hg38.gtf.gz");
		testRealParse(testName, file, Gene.class, "ENS", null, null);

		file = getPath("prod/steps/hs/snp141.txt.gz");
		testRealParse(testName, file, Variant.class, "rs", null, null);
	}
	
	// Must populate "./tmp/mouseFull" first
	//@Test
	public void parseMouseStepLocationsChia16() throws Exception {
		
		// Use the previously created dir.
		Path tdir = Paths.get("./tmp/mouseFull");

		StreamReader<Step> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("prod/steps/mm/chia-16.step.gz")));
		try (StepConnector conn = new StepConnector()) {

			conn.setParentDirectory(tdir);
			
			List<Contact> stream = reader.stream()
										 .flatMap(step->conn.stream(step))
										 .collect(Collectors.toList());
				  
			assertEquals(1236, stream.size()); 
			
		}
	}
	
	// Slow
	// @Test
	public void mouseBulkFileWrite() throws Exception {
		
		Path stepDir = getPath("prod/steps/mm/");
		
		List<Path> stepFiles = Files.list(stepDir)
								 .filter(file -> file.getFileName().toString().toLowerCase().endsWith(".step.gz"))
								 .collect(Collectors.toList());

		Path dir = Paths.get("./tmp/mouseBulkWrite");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		try (StepConnector conn = new StepConnector()) {
			
			// Use the previously created dir for the locations database
			conn.setParentDirectory(Paths.get("./tmp/mouseFull"));

			try (@SuppressWarnings("resource")
				ExportBuilder builder = new ExportBuilder().setSpecies("Mus musculus")
										.setChunkProperty("1000")
										.addConnector(conn)
										.setDir(dir)
										.setInputs(stepFiles)
										.setParallelFiles(false)
										.setDefaultChunkSize(10000)) {

				builder.export();
				System.out.println(builder.status());
			}
		}

	}


	private Path testRealParse(String testName, Path file, Class<? extends Entity> type, String prefix, Long skip, Long limit) throws Exception {
		
		Path tdir = Paths.get("./tmp/"+testName);
		assertTrue(Files.exists(file));
		
		try (StepConnector func = new StepConnector("Mus musculus", type)) {
			func.setLocation(tdir);
			func.setSkip(skip);
			func.setLimit(limit);
			
			func.add(file);
			func.create(prefix, IPrintStream.of(System.out)); // Creates indexed database.
		}
		return tdir;
	}

}
