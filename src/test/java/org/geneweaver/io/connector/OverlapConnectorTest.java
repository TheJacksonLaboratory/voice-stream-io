package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.geneweaver.io.writer.ExportBuilder;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class OverlapConnectorTest extends AbstractDataFileTest{

	
	@Test(expected=FileNotFoundException.class)
	public void badRegionFile1() throws Exception {
		Path hFile = getPath("data/NOTTHERE");
		try (OverlapConnector<Variant, Entity> func = new OverlapConnector<>()) {
			func.add(hFile);
		}
	}
	
	@Test(expected=FileNotFoundException.class)
	public void badRegionFile2() throws Exception {
		Path mFile = getPath("data/bed_peaks/homo_sapiens/CD14_monocyte_1/H3K4me1/homo_sapiens.GRCh38.CD14_monocyte_1.H3K4me1.ccat_histone.peaks.20210107.bed.gz");
		Path hFile = getPath("data/NOTTHERE");
		try (OverlapConnector<Variant, Entity> func = new OverlapConnector<>()) {
			func.add(mFile);
			func.add(hFile);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noAdd() throws Exception {
		try (OverlapConnector<Variant, Entity> func = new OverlapConnector<>()) {
			func.create(); // Creates indexed database.
		}
	}
	

	@Test
	public void addAllMouse() throws Exception {
		testCreate("regionsAllMouse", getPath("data/bed_peaks/mus_musculus/"), 2);
	}
	@Test
	public void addAllHuman() throws Exception {
		testCreate("regionsAllHuman", getPath("data/bed_peaks/homo_sapiens/"), 2);
	}
	
	public void testCreate(String testName, Path ddir, int limit) throws Exception {
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());
		
		try (OverlapConnector<Variant, Entity> func = new OverlapConnector<>()) {
			func.setLocation(tdir);
			
			// limit is used here just to avoid caching all the test files i.e. test goes quicker.
			func.addAll(ddir, limit);
			
			Stopwatch timer = Stopwatch.createStarted();
			func.create(); // Creates indexed database.
			System.out.println("Created cache table size "+func.size()+" in "+timer.stop());
			assertTrue(func.size()>10000);
		}
	}
	
	@Test
	public void testOnePerVariant() throws Exception {
		Path vpath = getPath("data/bed_peaks/oneVarOnePeak.gvf");
		Path rpath = getPath("data/bed_peaks/oneVarOnePeak.bed");
		List<Entity> ret = testIntersections("testInterleaves", vpath, rpath);
		
		// In this data the first three variants all match the same peak
		String peak1 = ((Overlap)ret.get(1)).getPeak().getPeakId();
		assertEquals(peak1, ((Overlap)ret.get(3)).getPeak().getPeakId());
		assertEquals(peak1, ((Overlap)ret.get(5)).getPeak().getPeakId());
		
		// The 4th variant matches a different peak.
		assertNotEquals(peak1, ((Overlap)ret.get(7)).getPeak().getPeakId());
		String peak2 = ((Overlap)ret.get(7)).getPeak().getPeakId();
		assertEquals(peak2, ((Overlap)ret.get(9)).getPeak().getPeakId());
		assertEquals(peak2, ((Overlap)ret.get(11)).getPeak().getPeakId());
		assertEquals(peak2, ((Overlap)ret.get(13)).getPeak().getPeakId());
		assertEquals(peak2, ((Overlap)ret.get(15)).getPeak().getPeakId());
	}
	
	@Test
	public void testNone() throws Exception {
		Path vpath = getPath("data/bed_peaks/none.gvf");
		Path rpath = getPath("data/bed_peaks/none.bed");
		List<Entity> ret = testIntersections("testNoMatch", vpath, rpath);
		assertTrue(ret.stream().allMatch(e->!(e instanceof Overlap)));
	}
	
	@Test
	public void testSome() throws Exception {
		
		// Test finding the peaks
		Path vpath = getPath("data/bed_peaks/some.gvf");
		Path rpath = getPath("data/bed_peaks/some.bed");
		List<Entity> ret  = testIntersections("testSomeMatch", vpath, rpath);
		List<Entity> laps = ret.stream().filter(o->(o instanceof Overlap)).collect(Collectors.toList());
		assertEquals(30, laps.size());
		
		// The six non-matches should result in variants which have no Overlap
		// First check that they are there.
		long nos = ret.stream()
				   .filter(e->(e instanceof Variant))
				   .filter(v->"n".equals(((Variant)v).getType()))
				   .count();
		assertEquals(4, nos);
		
		// Now check that any with n type have no following Overlaps
		boolean checkNotOverlap = false;
		for (Entity e : ret) {
			if (checkNotOverlap) {
				assertFalse(e instanceof Overlap);
				checkNotOverlap = false;
			}
			checkNotOverlap = (e instanceof Variant) && "n".equals(((Variant)e).getType());
		}
	}


	private List<Entity> testIntersections(String testName, Path vpath, Path rpath) throws Exception {
		
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());
		
		try (OverlapConnector<Variant, Entity> conn = new OverlapConnector<>()) {
			conn.setLocation(tdir);
			conn.add(rpath);
			conn.create();
			
			StreamReader<Variant> vars = ReaderFactory.getReader(new ReaderRequest(testName, vpath));
			List<Entity> varsAndIntersections = vars.stream()
				.flatMap(conn::stream)
				.collect(Collectors.toList());
			
			assertNotNull(varsAndIntersections);
			return varsAndIntersections;
		}
	}
	
	/**
	 * Trying to see speed of a full scale connectors.
	 * The connector database may have to be copied backs from the k8s node. E.g.:
	 * <pre>
	 * kubectl cp $NAMESPACE/bulk-import-dev-7594b86847-qvp2n:/neo4j/bulk/ensembl-107/hs/peaks.mv.db /Volumes/Work/JAX/data/peaks.mv.db
	 * </pre>
	 * 
	 * In the database for human peaks there are 170059268 rows in ensembl-107
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMassiveDatabase() throws Exception {
		
		Path dir = Paths.get("./tmp/testBedExportWithMassiveOverlaps");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		// Create a massive database
		Path rpath = getPath("data/bed_peaks/some.bed");
		try (OverlapConnector<Variant, Entity> conn = new OverlapConnector<>("peaks")) {
			// 1. Create a database with things that match.
			conn.setLocation(dir);
			conn.add(rpath);
			conn.create();
			
			// 2. Add random rows to this database, increase size to check performance of 
			// many rows. 10mill is a reasonable test when it comes to our peaks which are
			// size 170059268 for all ensembl-107 data (but splittable my chromosome).
			int added = conn.testAddRandomRows("chr1", 100000);
			System.out.println("Added "+added+" rows");
		}
		
		long time = System.currentTimeMillis();
		
		// Test how long it takes to interact with it.
		Path vpath = getPath("data/bed_peaks/some.gvf");
		try (OverlapConnector<Variant, Entity> conn = new OverlapConnector<>("peaks")) {
			conn.setLocation(dir);

			try (@SuppressWarnings("resource")
			ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
										.setChunkProperty("1000")
										.setAlwaysUseDefaultConnector(true)
										.addConnector(conn)
										.setDir(dir)
										.setInput(vpath)
										.setDefaultChunkSize(10000)) {

				builder.export();
			}
		}
		System.out.println("Time to lookup ten = "+(System.currentTimeMillis()-time)+"ms");
		
		ReaderRequest reader = new ReaderRequest("test", dir.resolve("Overlap.csv.gz"));
		reader.setReaderHint("MapCSVReader");
		assertEquals(29, ReaderFactory.getReader(reader).stream().count());
		assertTrue(Files.exists(dir.resolve("Overlap-header.csv")));
		assertTrue(Files.size(dir.resolve("Variant.csv.gz"))>100);
		assertTrue(Files.exists(dir.resolve("Variant-header.csv")));
		assertTrue(Files.exists(dir.resolve("VariantEffect.csv.gz")));
		assertTrue(Files.exists(dir.resolve("VariantEffect-header.csv")));

	}
}
