/*-
 * 
 * Copyright 2018, 2020  The Jackson Laboratory Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Matthew Gerring
 */
package org.geneweaver.io.reader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.geneweaver.domain.Entity;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
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
