package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.geneweaver.domain.Step;
import org.junit.Test;

public class StepReaderTest extends AbstractDataFileTest {

	
	
	@Test
	public void simpleStepRead() throws Exception {
		
		StreamReader<Step> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("prod/ccsi/mm/4c-1.step.gz")));
		List<Step> steps = reader.stream().collect(Collectors.toList());
		assertNotNull(steps);
		assertEquals(58, steps.size());
	}
	
	@Test
	public void readAllHuman() throws Exception {
		
		Path dir = getPath("prod/ccsi/hs/");
		readAll("Homo sapiens", dir, 10);
	}
	
	@Test
	public void readAllMouse() throws Exception {
		Path dir = getPath("prod/ccsi/mm/");
		readAll("Mus musculus", dir, 10);
	}
	
	private void readAll(String species, Path dir, Integer limit) throws Exception {
		
		List<Path> files = Files.list(dir).collect(Collectors.toList());
		int count = 0;
		for (Path path : files) {
			if (!path.getFileName().toString().toLowerCase().endsWith(".step.gz")) return;
			try {
				StreamReader<Step> reader = ReaderFactory.getReader(new ReaderRequest(species, path));
				List<Step> steps = reader.stream().collect(Collectors.toList());
				assertNotNull(steps);
				System.out.println("Read "+steps.size()+" steps from "+path.getFileName());
				count++;
				if (limit!=null && limit>0 && count>limit) return;
			} catch (Exception ne) {
				fail(ne.getMessage());
			}
		};
	}

}
