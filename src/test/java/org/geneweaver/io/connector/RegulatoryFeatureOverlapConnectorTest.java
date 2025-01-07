package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.IPrintStream;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.junit.Test;

public class RegulatoryFeatureOverlapConnectorTest extends AbstractDataFileTest {
	
	@Test
	public void simpleRegulatoryFeatureOverlapCreation() throws Exception {
		
		Path gdir = Paths.get("tmp/simpleRegulatoryFeatureOverlapCreation/hs");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
		
		// Should use just the transcript locations.
		Path dir =  getPath("data/gff_peaks/homo_sapiens");
		try (AbstractOverlapConnector<Variant, Entity> conn = new RegulatoryFeatureOverlapConnector<>("regfeats")) {
			conn.setLocation(gdir);
			conn.addAll(dir);
			conn.setLimit(100L);
		
		    long nFeats = conn.create(null, IPrintStream.of(System.out)); 
			assertEquals(300, nFeats);
		}
		
		gdir = Paths.get("tmp/simpleRegulatoryFeatureOverlapCreation/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);

		dir =  getPath("data/gff_peaks/mus_musculus");
		try (AbstractOverlapConnector<Variant, Entity> conn = new RegulatoryFeatureOverlapConnector<>("regfeats")) {
			conn.setLocation(gdir);
			conn.addAll(dir);
			conn.setLimit(100L);
	
		    long nFeats = conn.create(null, IPrintStream.of(System.out)); 
		    
		    // Do we want both feats from the mouse dir?
		    // That's a lot of features.
			assertEquals(200, nFeats);
		}

	}

	@Test
	public void newestByNameWorking() throws Exception {

		Path gdir1 = Paths.get("tmp/newestByNameWorking/mm1");
		FileUtils.deleteQuietly(gdir1.toFile());
		Path dir =  getPath("data/gff_peaks/mus_musculus");
		try (AbstractOverlapConnector<Variant, Entity> conn = new RegulatoryFeatureOverlapConnector<>("regfeats")) {
			conn.setLocation(gdir1);
			conn.setNewestInDirectoryByName(false);
			conn.addAll(dir);
			conn.setLimit(100L);
		
		    long nFeats = conn.create(null, IPrintStream.of(System.out)); 
		    
		    // Do we want both feats from the mouse dir?
		    // That's a lot of features and they seem to be repeated.
			assertEquals(400, nFeats);
		}
		
		Path gdir2 = Paths.get("tmp/newestByNameWorking/mm2");
		FileUtils.deleteQuietly(gdir2.toFile());
		try (AbstractOverlapConnector<Variant, Entity> conn = new RegulatoryFeatureOverlapConnector<>("regfeats")) {
			conn.setLocation(gdir2);
			conn.addAll(dir);
			conn.setLimit(100L);
		
		    long nFeats = conn.create(null, IPrintStream.of(System.out)); 
			conn.setNewestInDirectoryByName(true); // Normally we ignore newest by name.
		    
		    // Do we want both feats from the mouse dir?
		    // That's a lot of features and they seem to be repeated.
			assertEquals(200, nFeats);
		}

	}
}
