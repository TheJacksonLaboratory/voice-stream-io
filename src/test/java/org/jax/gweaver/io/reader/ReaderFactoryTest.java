package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ReaderFactoryTest {

	
	@Test
	public void isSupported() throws Exception {
		
		List<String> exts = Arrays.asList(new String[] {
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
				"lookup_table.txt.gz",	
				"tar", 		
				"zip"
		});
		
		for (String ext : exts) {
			assertTrue(ReaderFactory.isSupported(new ReaderRequest("fred."+ext)));
		}
	}
}
