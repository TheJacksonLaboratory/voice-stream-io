package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.jax.gweaver.domain.Entity;
import org.junit.Test;

public class ReaderFactoryTest {
	
	private static final List<String> exts = Arrays.asList(new String[] {
			"gtf",
			"gvf",
			"bed",
			"xls", 
			"tsv", 
			"rpt", 
			"egenes.txt.gz", 	
			"sgenes.txt", 	
			"signif_variant_gene_pairs.txt.gz",	
			"sqtl_signifpairs.txt", 	
			"allpairs.txt.gz", 		
			"sqtl_allpairs.txt",	
			"tar", 		
			"zip"
	});

	@Test
	public void isSupported() throws Exception {
		for (String ext : exts) {
			assertTrue(ReaderFactory.isSupported(new ReaderRequest("fred."+ext)));
		}
	}
	
	@Test
	public void doesNotIterate() throws Exception {
		for (String ext : exts) {
			ReaderRequest req = new ReaderRequest("fred."+ext);
			req.setInitRequired(false);
			StreamReader<Entity> reader = ReaderFactory.getReader(req);
			try {
				reader.stream().iterator().next();
				fail("Stream from non-file "+"fred."+ext+" worked!");
			} catch (Throwable ne) {
				continue;
			}
		}
	}

}
