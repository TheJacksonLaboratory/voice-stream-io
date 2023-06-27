package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.geneweaver.io.reader.AbstractDataFileTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChromosomeServiceTest extends AbstractDataFileTest  {
	
	private ChromosomeService cservice;

	@Before
	public void create() {
		cservice = ChromosomeService.getInstance();
		ChromosomeService.clearCache();
	}
	
	@After
	public void dispose() {
		cservice = ChromosomeService.getInstance();
		ChromosomeService.clearCache();
	}
	
	@Test
	public void checkGoodChromosomes() throws Exception {
		assertEquals("X",  cservice.getChromosome("chrX"));
		assertEquals("1",  cservice.getChromosome("1"));
		assertEquals("22", cservice.getChromosome("22"));
		assertEquals("1",  cservice.getChromosome("chr1"));
		assertEquals("22", cservice.getChromosome("chr22"));
		assertEquals("22", cservice.getChromosome("CHR22"));
		assertEquals("22", cservice.getChromosome("CHr22"));
		assertEquals("M",  cservice.getChromosome("chrMT"));
		assertEquals("22", cservice.getChromosome("chr22_KI270731v1_random"));
		assertEquals("5",  cservice.getChromosome("chr5_GL000208v1_random"));
	}
	
	@Test
	public void checkBadChromosomes() throws Exception {
		assertNull(cservice.getChromosome("chr"));
		assertNull(cservice.getChromosome("fred"));
		assertNull(cservice.getChromosome("chr111"));
		assertNull(cservice.getChromosome("chrUn_KI270418v1"));
		assertNull(cservice.getChromosome("chrUn"));
	}

	@Test
	public void checkGoodChromosomesStrict() throws Exception {
		try {
			System.setProperty("strict", "true");
			assertEquals("1",  cservice.getChromosome("chr1"));
			assertEquals("22", cservice.getChromosome("chr22"));
			assertEquals("X",  cservice.getChromosome("chrX"));
			assertEquals("M", cservice.getChromosome("chrMT"));
			assertEquals("M", cservice.getChromosome("chrM"));
			assertEquals("2", cservice.getChromosome("CHR2"));
			assertEquals(null, cservice.getChromosome("chr22_KI270731v1_random"));
			assertEquals(null,  cservice.getChromosome("chr5_GL000208v1_random"));
		} finally {
			System.setProperty("strict", "false");
		}
	}

	@Test
	public void checkBadChromosomesStrict() throws Exception {
		try {
			System.setProperty("strict", "true");
			assertNull(cservice.getChromosome("chr22_KI270731v1_random"));
			assertNull(cservice.getChromosome("chr5_GL000208v1_random"));
			assertNull(cservice.getChromosome("chr"));
			assertNull(cservice.getChromosome("fred"));
			assertNull(cservice.getChromosome("chr111"));
			assertNull(cservice.getChromosome("chrUn_KI270418v1"));
			assertNull(cservice.getChromosome("chrUn"));
		} finally {
			System.setProperty("strict", "false");
		}
	}

}
