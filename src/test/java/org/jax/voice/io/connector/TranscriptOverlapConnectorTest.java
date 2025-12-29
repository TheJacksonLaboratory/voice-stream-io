package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Variant;
import org.jax.voice.io.IPrintStream;
import org.jax.voice.io.connector.AbstractOverlapConnector;
import org.jax.voice.io.connector.TranscriptOverlapConnector;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.junit.Test;

public class TranscriptOverlapConnectorTest extends AbstractDataFileTest{
	
	@Test
	public void simpleTranscriptOverlapCreationHuman() throws Exception {
		
		Path gdir = Paths.get("tmp/simpleTranscriptOverlapCreation/hs");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
		
		// Should use just the transcript locations.
		Path input =  getPath("data/1000/hs_gtf/hg38_2.gtf");
		try (AbstractOverlapConnector<Variant, Entity> conn = new TranscriptOverlapConnector<>("Homo sapiens", "transcripts")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long ntrans = conn.create(null, IPrintStream.of(System.out)); 
			assertEquals(171, ntrans);
		}
	}
	
	@Test
	public void simpleTranscriptOverlapCreationMouse() throws Exception {

		Path gdir = Paths.get("tmp/simpleTranscriptOverlapCreation/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
	
		Path input =  getPath("data/gz/mm10_1.gtf.gz");
		try (AbstractOverlapConnector<Variant, Entity> conn = new TranscriptOverlapConnector<>("Mus musculus", "transcripts")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long ntrans = conn.create(null, IPrintStream.of(System.out)); 
			assertEquals(68918, ntrans);
		}
	}



}
