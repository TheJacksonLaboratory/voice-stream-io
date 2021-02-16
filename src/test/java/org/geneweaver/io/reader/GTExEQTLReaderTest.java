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

import static org.geneweaver.io.DirectSave.save;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.geneweaver.domain.EQTL;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.NamedEntity;
import org.geneweaver.io.connector.EQTLFunction;
import org.geneweaver.io.reader.FileFilters;
import org.geneweaver.io.reader.GTExEQTLReader;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.junit.Ignore;
import org.junit.Test;

public class GTExEQTLReaderTest extends AbstractDataFileTest {

	
	@Test
	public void lookup() throws Exception {
		File lookup = getFile("data/eQTL/GTExLookup-frag.lookup_table.txt");
		assertFalse(ReaderFactory.isSupported(new ReaderRequest(lookup)));
	}

	@Test
	public void egenesSupported() throws Exception {
		File egenes = getFile("data/eQTL/Brain_Substantia_nigra.v8.egenes.txt.gz");
		assertTrue(ReaderFactory.isSupported(new ReaderRequest(egenes)));
	}
	
	@Test
	public void genePairsSupported() throws Exception {
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		assertTrue(ReaderFactory.isSupported(new ReaderRequest(pairs)));
	}
	
	@Test
	public void genePairsMapRsId() throws Exception {
		
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		Path dir = Paths.get("./tmp/eqtlMapRsId");
		fakeLookupTest(pairs, 283774, dir, "Brain_Substantia_nigra_pairs_lookup_fake.txt");
 	}
	
	@Test
	public void eGenesMapRsId() throws Exception {
		
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.egenes.txt.gz");
		Path dir = Paths.get("./tmp/eqtlMapRsId");
		fakeLookupTest(pairs, 0, dir, "Brain_Substantia_nigra_pairs_lookup_fake.txt");
 	}
	
	@Test
	public void genePairsMapRsId2() throws Exception {
		
		File pairs = getFile("data/eQTL/Uterus.v8.signif_variant_gene_pairs.txt.gz");
		Path dir = Paths.get("./tmp/eqtlMapRsId");
		fakeLookupTest(pairs, 328520, dir, "Uterus_pairs_lookup_fake.txt");
 	}

	private void fakeLookupTest(File pairs, long size, Path dir, String dbName)  throws Exception {
		
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		// Check rsIds are not there before map
		StreamReader<EQTL> eqtls = ReaderFactory.getReader(new ReaderRequest(pairs));	
		long count = eqtls.stream()
						 .filter(e->e.getRsId()==null)
						 .count();
		assertEquals(size, count);

		// Write a fake lookup with indexed rsId
	 	Path fake = dir.resolve(dbName);
		Set<String> done = new HashSet<>();
	 	try (BufferedWriter w = Files.newBufferedWriter(fake)) {
			w.write("variant_id\tchr\tvariant_pos\tref\talt\tnum_alt_per_site\trs_id_dbSNP151_GRCh38p7\tvariant_id_b37");
			w.newLine();
			
			int[] index = new int[] {1};
			eqtls.stream().forEach(e->{
				try {
					if (done.contains(e.getEqtlVariantId())) return;
					w.write(e.getEqtlVariantId());
					w.write('\t');
					w.write(e.getChr());
					w.write("\t155964792\tG\tC\t1\t");
					w.write("r");
					w.write(""+index[0]);
					w.write("FAKE\tX_155194457_G_A_b37");
					
					done.add(e.getEqtlVariantId());
					index[0]++;
					w.newLine();
				} catch (IOException ne) {
					fail(ne.getMessage());
				}
			});
		}

		Map<Class<? extends Entity>, BufferedWriter> cache = new HashMap<>();
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fake)) {
			func.setLocation(dir);
			
			func.create(); // Make the database.
	
			// Make sure that all the rsIds are mapped
			long[] sum = new long[] {0L};
			eqtls.stream()
				 .map(func::apply)
				 .map(e-> {
					 save(e, cache, dir, null);
					 return e;
				 })
				 .filter(e->e.getRsId()==null)
				 .forEach(e->sum[0]++);
			
			assertEquals(0, sum[0]);
		} finally {
			for (BufferedWriter writer : cache.values()) writer.close();
		}
		
		assertTrue(Files.exists(dir.resolve("EQTL-header.csv")));
		assertTrue(Files.exists(dir.resolve("EQTL.csv.gz")));
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(dir.resolve("EQTL.csv.gz")))))) {

			int lcount = 0;
			String line = null;
			while((line = reader.readLine())!=null) {
				if (line.matches("^r\\d+FAKE.*ENSG\\d+\\±EQTL$")) {
					lcount++;
				}
			}
			assertEquals(size, lcount);
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void urlInRequest() throws Exception {
		URL pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz").toURL();
		GTExEQTLReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(pairs));
		assertNotNull(reader);
	}
	
	/* 
	 * -----------------------------------------------------
	 * Ignored big files but useful for checking performance.
	 * -----------------------------------------------------
	 */
	@Ignore("Too big and local file!")
	@Test
	public void countEntitiesInAllFiles() throws Exception {
		File bigTar = new File("/Volumes/jax-data/data/variant-orthology/eQTL/v8/GTEx_Analysis_v8_eQTL.tar");
		StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(bigTar));
		assertEquals(72686455, reader.stream().count());
	}
	
	@Ignore("Too big and local file!")
	@Test
	public void countEntitiesNoPairs() throws Exception {
		File bigTar = new File("/Volumes/jax-data/data/variant-orthology/eQTL/v8/GTEx_Analysis_v8_eQTL.tar");
		
		StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(bigTar, FileFilters.PAIRS));
		assertEquals(71478479, reader.stream().count());
	}
	
	@Ignore("Too big and local file!")
	@Test
	public void countEntitiesNoEGenes() throws Exception {
		File bigTar = new File("/Volumes/jax-data/data/variant-orthology/eQTL/v8/GTEx_Analysis_v8_eQTL.tar");
		
		StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(bigTar, FileFilters.EGENES));
		assertEquals(1207976, reader.stream().count());
	}

}
