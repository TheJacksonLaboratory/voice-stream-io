package org.jax.voice.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.jax.voice.domain.EQTL;
import org.jax.voice.io.reader.JaxIntervalEQTLReader;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;
import org.junit.Test;

public class JaxIntervalEQTLReaderTest extends AbstractDataFileTest {


	@Test
	public void directUsingViveksFile() throws Exception {
		
		Path path = getPath("data/eQTL/interval/Chr8_1p5LODinterval_GenomeMuster_rqtl.csv");
		ReaderRequest req = new ReaderRequest("Mus musculus", path.toFile());
		req.setReaderHint("JaxIntervalEQTLReader");
	    StreamReader<EQTL> reader = ReaderFactory.getReader(req);
	    
	    assertTrue(reader instanceof JaxIntervalEQTLReader);
	    
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
	 
		// There should not be 1000 here because repeats e.g. in the 
		// test data line 15, 39, 84 etc.
		assertNotEquals(1000, eqtls.size());
		assertEquals(1019, eqtls.size());
		check(eqtls);
		
		String header = eqtls.get(0).getHeader();
		String line	  = eqtls.get(0).toCsv();
		assertEquals(header.split("\\|").length, line.split("\\|").length);
	}
	
	@Test
	public void directUsingViveksFileZipped() throws Exception {
		
		Path path = getPath("prod/eQTL-interval/mm/Chr8_1p5LODinterval_GenomeMuster_rqtl.csv.gz");
		ReaderRequest req = new ReaderRequest("Mus musculus", path.toFile());
		req.setReaderHint("JaxIntervalEQTLReader");
	    StreamReader<EQTL> reader = ReaderFactory.getReader(req);
	    
	    assertTrue(reader instanceof JaxIntervalEQTLReader);
	    
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
	 
		// There should not be 13010 here because repeats e.g. in the 
		// test data line 15, 39, 84 etc.
		assertNotEquals(13010, eqtls.size());
		assertEquals(13177, eqtls.size());
		check(eqtls);
		
		String header = eqtls.get(0).getHeader();
		String line	  = eqtls.get(0).toCsv();
		assertEquals(header.split("\\|").length, line.split("\\|").length);
	}
	
	private void check(List<EQTL> eqtls) {
		eqtls.forEach(e-> {
			assertNotNull(e.getRsId()); // It might be "NA" though
			//assertTrue("The rsId is: "+e.getRsId(), e.getRsId().startsWith("rs"));
			assertTrue(e.getRsId().indexOf(' ')<0);
			assertNotNull(e.getGeneId());
			assertNotNull(e.getPopulation());
			assertNotNull(e.getTissueName());
			assertNotNull(e.getLod());
			assertNotNull(e.getStudyId());
			assertNotNull(e.getBp());
			assertEquals(EQTL.Type.INTERVAL, e.getType());
		});		
		
		// Line 15: rs265569038 rs213947216
		checkPropertiesEqual(eqtls, "rs265569038", "rs213947216");
		// Line 39: rs229925787 rs250584385
		checkPropertiesEqual(eqtls, "rs229925787", "rs250584385");
		// Line 84: rs49951440 rs238952826
		checkPropertiesEqual(eqtls, "rs49951440", "rs238952826");
	}
	
	private void checkPropertiesEqual(List<EQTL> eqtls, String r1, String r2) {
		
		EQTL e1 = eqtls.stream().filter(e->e.getRsId().equals(r1)).findFirst().orElse(null);
		EQTL e2 = eqtls.stream().filter(e->e.getRsId().equals(r2)).findFirst().orElse(null);
		
		// They should have all same values apart from rsId
		e1.setRsId("test");
		e2.setRsId("test");
		assertEquals(e1, e2);
	}

	@Test(expected=IllegalArgumentException.class)
	public void badHeaderDirect() throws Exception {
		JaxIntervalEQTLReader reader = new JaxIntervalEQTLReader();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badHeader.csv")));
		reader.stream().collect(Collectors.toList());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues1Direct() throws Exception {
		JaxIntervalEQTLReader reader = new JaxIntervalEQTLReader();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues1.csv")));
		reader.stream().collect(Collectors.toList());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues2Direct() throws Exception {
		JaxIntervalEQTLReader reader = new JaxIntervalEQTLReader();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues2.csv")));
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		eqtls.forEach(e-> {
			assertNotNull(e.getRsId()); // Some are empty
		});	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues3Direct() throws Exception {
		JaxIntervalEQTLReader reader = new JaxIntervalEQTLReader();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues3.csv")));
		reader.stream().collect(Collectors.toList());
	}

	@Test(expected=IllegalArgumentException.class)
	public void noHeaderDirect() throws Exception {
		JaxIntervalEQTLReader reader = new JaxIntervalEQTLReader();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_noHeader.csv")));
		reader.stream().collect(Collectors.toList());
	}
	
}
