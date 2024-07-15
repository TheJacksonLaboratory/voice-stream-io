package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.junit.Test;

public class TranscriptOverlapConnectorTest extends AbstractDataFileTest{
	
	@Test
	public void simpleTranscriptOverlapCreation() throws Exception {
		
		Path gdir = Paths.get("tmp/simpleTranscriptOverlapCreation/hs");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
		
		// Should use just the transcript locations.
		Path input =  getPath("data/1000/hs_gtf/hg38_2.gtf");
		try (AbstractOverlapConnector<Variant, Entity> conn = new TranscriptOverlapConnector<>("transcripts")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long ntrans = conn.create(null, System.out); 
			assertEquals(171, ntrans);
		}
		
		gdir = Paths.get("tmp/simpleTranscriptOverlapCreation/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);

		input =  getPath("data/gz/mm10_1.gtf.gz");
		try (AbstractOverlapConnector<Variant, Entity> conn = new TranscriptOverlapConnector<>("transcripts")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long ntrans = conn.create(null, System.out); 
			assertEquals(68918, ntrans);
		}

	}

}
