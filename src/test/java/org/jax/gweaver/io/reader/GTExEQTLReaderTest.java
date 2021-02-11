package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.jax.gweaver.domain.NamedEntity;
import org.junit.Ignore;
import org.junit.Test;

public class GTExEQTLReaderTest extends AbstractDataFileTest {

	
	@Test
	public void lookup() throws Exception {
		File lookup = getFile("data/eQTL/GTExLookup-frag.lookup_table.txt");
		assertTrue(ReaderFactory.isSupported(new ReaderRequest(lookup)));
	}

	@Test
	public void egenes() throws Exception {
		File egenes = getFile("data/eQTL/Brain_Substantia_nigra.v8.egenes.txt.gz");
		assertTrue(ReaderFactory.isSupported(new ReaderRequest(egenes)));
	}
	
	@Test
	public void genePairs() throws Exception {
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		assertTrue(ReaderFactory.isSupported(new ReaderRequest(pairs)));
	}
	
	@Test
	public void nullMap() throws Exception {
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		GTExEQTLReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(pairs));
		assertNull(reader.getMapping());
	}
	
	@Test
	public void testMap() throws Exception {
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		File fmap = getFile("data/eQTL/GTExLookup-frag.lookup_table.txt");
		GTExEQTLReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(pairs, fmap));
		
		Map<String,String> map = reader.getMapping();
		assertNotNull(map);
		assertEquals(999, map.size());
	}

	@Ignore
	@Test
	public void timeMassiveMapURL() throws Exception {
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		URL umap = new URL("https://storage.googleapis.com/gtex_analysis_v8/reference/GTEx_Analysis_2017-06-05_v8_WholeGenomeSeq_838Indiv_Analysis_Freeze.lookup_table.txt.gz");
		GTExEQTLReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(pairs, umap));
		
		Map<String,String> map = reader.getMapping();
		assertNotNull(map);
		assertEquals(999, map.size());
	}
	
	@Ignore
	@Test
	public void timeMassiveMapFile() throws Exception {
		File pairs = getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
		File fmap = new File("/Volumes/jax-data/data/variant-orthology/eQTL/v8/GTEx_Analysis_2017-06-05_v8_WholeGenomeSeq_838Indiv_Analysis_Freeze.lookup_table.txt.gz");
		GTExEQTLReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(pairs, fmap));
		
		Map<String,String> map = reader.getMapping();
		assertNotNull(map);
		assertEquals(999, map.size());
	}

}
