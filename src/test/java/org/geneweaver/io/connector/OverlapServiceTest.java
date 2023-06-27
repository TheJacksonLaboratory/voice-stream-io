package org.geneweaver.io.connector;

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
	private ChromosomeService cservice;

	@Before
	public void create() {
		this.iservice = new OverlapService();
		cservice = ChromosomeService.getInstance();
		ChromosomeService.clearCache();
	}
	
	@After
	public void dispose() {
		cservice = ChromosomeService.getInstance();
		ChromosomeService.clearCache();
	}

	@Test
	public void enclosedVariant1() throws Exception {
		assertNotNull(intersection(10, 10, 7, 12));
	}

	@Test
	public void enclosedVariant1OverlapGt1() throws Exception {
		try {
			iservice.minOverlap = 2;
			assertNull(intersection(10, 10, 7, 12));
		} finally {
			iservice.minOverlap = 1;
		}
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
		assertNull(intersection(10, 10, 10, 10));
	}

	@Test
	public void enclosedPeak1() throws Exception {
		assertNotNull(intersection(10, 20, 10, 11));
	}

	@Test
	public void enclosedPeak2() throws Exception {
		assertNotNull(intersection(10, 20, 11, 12));
	}

	@Test
	public void enclosedPeak3() throws Exception {
		assertNotNull(intersection(10, 20, 11, 20));
	}

	@Test
	public void enclosedPeak4() throws Exception {
		assertNotNull(intersection(10, 21, 20, 21));
	}
	
	@Test
	public void peakSmaller1() throws Exception {
		assertNotNull(intersection(10, 20, 10, 11));
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
		assertNotNull(intersection(10, 21, 20, 21));
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
}