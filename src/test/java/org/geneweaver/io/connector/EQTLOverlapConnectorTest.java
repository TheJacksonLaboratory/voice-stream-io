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

public class EQTLOverlapConnectorTest extends AbstractDataFileTest{
	
	
	@Test
	public void simpleEQTLOverlapCreationMouse() throws Exception {

		Path gdir = Paths.get("tmp/simpleEQTLOverlapCreation/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
	
		Path input =  getPath("data/eQTL/mm/lo/Chesler_Striatum_DO_pr69k_lo.csv");
		try (AbstractOverlapConnector<Variant, Entity> conn = new EQTLOverlapConnector<>("eqtloverlaps")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long neqtl = conn.create(null, System.out); 
			assertEquals(27444, neqtl);
		}
	}

}
