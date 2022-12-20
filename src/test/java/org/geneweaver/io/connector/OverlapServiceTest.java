package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.geneweaver.domain.Overlap;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OverlapServiceTest extends AbstractDataFileTest  {
	
	private OverlapService iservice;

	@Before
	public void create() {
		this.iservice = new OverlapService();
		OverlapService.clearCache();
	}
	
	@After
	public void dispose() {
		OverlapService.clearCache();
	}

	@Test
	public void enclosedVariant1() throws Exception {
		assertNotNull(intersection(10, 10, 7, 12));
	}

	@Test
	public void enclosedVariant2() throws Exception {
		assertNotNull(intersection(10, 11, 7, 12));
	}

	@Test
	public void enclosedVariant3() throws Exception {
		assertNotNull(intersection(10, 15, 8, 16));
	}
	
	@Test
	public void outside1() throws Exception {
		assertNull(intersection(10, 10, 11, 11));
	}
	
	@Test
	public void outside2() throws Exception {
		assertNull(intersection(10, 10, 11, 12));
	}

	@Test
	public void outside3() throws Exception {
		assertNull(intersection(10, 10, 8, 8));
	}

	@Test
	public void outside4() throws Exception {
		assertNull(intersection(10, 10, 8, 9));
	}

	@Test
	public void enclosedPeak0() throws Exception {
		assertNotNull(intersection(10, 10, 10, 10));
	}

	@Test
	public void enclosedPeak1() throws Exception {
		assertNotNull(intersection(10, 20, 10, 10));
	}

	@Test
	public void enclosedPeak2() throws Exception {
		assertNotNull(intersection(10, 20, 11, 11));
	}

	@Test
	public void enclosedPeak3() throws Exception {
		assertNotNull(intersection(10, 20, 11, 20));
	}

	@Test
	public void enclosedPeak4() throws Exception {
		assertNotNull(intersection(10, 20, 20, 20));
	}
	
	@Test
	public void peakSmaller1() throws Exception {
		assertNotNull(intersection(10, 20, 10, 10));
	}
	
	@Test
	public void peakSmaller2() throws Exception {
		assertNotNull(intersection(10, 20, 5, 10));
	}
	
	@Test
	public void peakGreater1() throws Exception {
		assertNotNull(intersection(10, 20, 19, 22));
	}
	
	@Test
	public void peakGreater2() throws Exception {
		assertNotNull(intersection(10, 20, 20, 20));
	}
	
	@Test
	public void peakGreater3() throws Exception {
		assertNotNull(intersection(10, 20, 20, 22));
	}

	private Overlap intersection(int vs, int ve, int ps, int pe) {
		Variant v = new Variant();
		v.setStart(vs);
		v.setEnd(ve);
		
		Peak p = new Peak();
		p.setStart(ps);
		p.setEnd(pe);
		return iservice.intersection(v, p);
	}
	
	@Test
	public void checkGoodChromosomes() throws Exception {
		assertEquals("chr1",  OverlapService.getChromosome("chr1"));
		assertEquals("chr22", OverlapService.getChromosome("chr22"));
		assertEquals("chrX",  OverlapService.getChromosome("chrX"));
		assertEquals("chrMT", OverlapService.getChromosome("chrMT"));
		assertEquals("chr22", OverlapService.getChromosome("chr22_KI270731v1_random"));
		assertEquals("chr5",  OverlapService.getChromosome("chr5_GL000208v1_random"));
	}
	
	@Test
	public void checkBadChromosomes() throws Exception {
		assertNull(OverlapService.getChromosome("chr"));
		assertNull(OverlapService.getChromosome("CHR2"));
		assertNull(OverlapService.getChromosome("fred"));
		assertNull(OverlapService.getChromosome("chr111"));
		assertNull(OverlapService.getChromosome("chrUn_KI270418v1"));
		assertNull(OverlapService.getChromosome("chrUn"));
	}

	@Test
	public void checkGoodChromosomesStrict() throws Exception {
		try {
			System.setProperty("strict", "true");
			assertEquals("chr1",  OverlapService.getChromosome("chr1"));
			assertEquals("chr22", OverlapService.getChromosome("chr22"));
			assertEquals("chrX",  OverlapService.getChromosome("chrX"));
			assertEquals("chrMT", OverlapService.getChromosome("chrMT"));
			assertEquals(null, OverlapService.getChromosome("chr22_KI270731v1_random"));
			assertEquals(null,  OverlapService.getChromosome("chr5_GL000208v1_random"));
		} finally {
			System.setProperty("strict", "false");
		}
	}

	@Test
	public void checkBadChromosomesStrict() throws Exception {
		try {
			System.setProperty("strict", "true");
			assertNull(OverlapService.getChromosome("chr22_KI270731v1_random"));
			assertNull(OverlapService.getChromosome("chr5_GL000208v1_random"));
			assertNull(OverlapService.getChromosome("chr"));
			assertNull(OverlapService.getChromosome("CHR2"));
			assertNull(OverlapService.getChromosome("fred"));
			assertNull(OverlapService.getChromosome("chr111"));
			assertNull(OverlapService.getChromosome("chrUn_KI270418v1"));
			assertNull(OverlapService.getChromosome("chrUn"));
		} finally {
			System.setProperty("strict", "false");
		}
	}

}
