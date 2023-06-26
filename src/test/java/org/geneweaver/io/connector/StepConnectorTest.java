package org.geneweaver.io.connector;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Gene;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class StepConnectorTest extends AbstractDataFileTest {

	
	@Test(expected=IllegalArgumentException.class)
	public void noAdd() throws Exception {
		try (StepConnector<Gene, Entity> func = new StepConnector<>(Gene.class)) {
			func.create(); // Creates indexed database.
		}
	}
	
	@Test(expected=FileNotFoundException.class)
	public void badGeneFile() throws Exception {
		Path mFile = getPath("data/1000/hs_gtf/hg38_2.gtf");
		Path hFile = getPath("data/NOTTHERE");
		try (StepConnector<Gene, Entity> func = new StepConnector<>(Gene.class)) {
			func.add(mFile);
			func.add(hFile);
		}
	}

	@Test
	public void aFewGenes() throws Exception {
		Path dir = getPath("data/1000/hs_gtf/");
		testCreate("aFewGenes", dir, 1, Gene.class, 64);
	}

	public void testCreate(String testName, Path ddir, int limit, Class<? extends Entity> type, int size) throws Exception {
		Path tdir = Paths.get("./tmp/"+testName);
		FileUtils.deleteQuietly(tdir.toFile());
		
		try (StepConnector<?, Entity> func = new StepConnector<>(type)) {
			func.setLocation(tdir);
			
			// limit is used here just to avoid caching all the test files i.e. test goes quicker.
			func.addAll(ddir, limit);
			
			Stopwatch timer = Stopwatch.createStarted();
			func.create(); // Creates indexed database.
			Stopwatch done = timer.stop();
			System.out.println("Created cache table size "+func.size()+" in "+done);
			assertTrue("Size is "+func.size(), func.size()>=size);
			assertTrue(done.elapsed().toMillis()<10000);
		}
	}

}
