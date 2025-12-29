package org.jax.voice.io.connector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.EQTLOverlap;
import org.jax.voice.domain.Gene;
import org.jax.voice.domain.Peak;
import org.jax.voice.domain.Variant;
import org.jax.voice.io.connector.ChromosomeService;
import org.jax.voice.io.connector.EQTLOverlapConnector;
import org.jax.voice.io.connector.OverlapService;
import org.jax.voice.io.connector.PeakOverlapConnector;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OverlapServiceTest extends AbstractDataFileTest  {
	
	private OverlapService iservice;

	@Before
	public void create() {
		this.iservice = new OverlapService();
		ChromosomeService.clearCache();
	}
	
	@After
	public void dispose() {
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
			assertNotNull(intersection(10, 10, 7, 12));
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
		assertNotNull(intersection(10, 10, 10, 10));
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

	private <T extends AbstractEntity> T intersection(int vs, int ve, int ps, int pe) {
		Variant v = new Variant();
		v.setStart(vs);
		v.setEnd(ve);
		
		Peak p = new Peak();
		p.setStart(ps);
		p.setEnd(pe);
		return (T)iservice.intersection(v, p, new PeakOverlapConnector<>(), Collections.emptyMap());
	}
	
	@Test
	public void eqtl1() throws Exception {
		
		Map<String,Object> meta = createTestMeta();
		EQTLOverlap eo = intersection(10, 15, 8, 16, meta);
		assertNotNull(eo);
		
		BeanMap map = new BeanMap(eo);
		assertTrue(meta.keySet().stream().allMatch(key->map.get(key)!=null));
	}
	
	@Test
	public void eqtlOutside() throws Exception {
		Map<String,Object> meta = createTestMeta();
		assertNull(intersection(10, 10, 8, 8, meta));
	}
	
	private Map<String,Object> createTestMeta() {
		Map<String, Object> meta = new HashMap<>();
		meta.put("chr",  "1");
		meta.put("bp",   123456);
		meta.put("lod",  0.1d);
		meta.put("tissueFileName","striatum file");
		meta.put("tissueGroup","brain");
		meta.put("tissueName","striatum");
		meta.put("uberon","???");
		meta.put("studyId","JAX123");
		return meta;
	}

	private <T extends AbstractEntity> T intersection(int vs, int ve, int gs, int ge, Map<String, Object> meta) {
		Variant v = new Variant();
		v.setStart(vs);
		v.setEnd(ve);
		
		Gene g = new Gene();
		g.setStart(gs);
		g.setEnd(ge);
		return (T)iservice.intersection(v, g, new EQTLOverlapConnector<>(), meta);
	}

}