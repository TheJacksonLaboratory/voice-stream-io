package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geneweaver.domain.EQTL;
import org.junit.Test;

public class JaxEQTLReaderTest extends AbstractDataFileTest {

	
	@Test
	public void directAgingBone() throws Exception {
		
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO.csv")));
		
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		assertEquals(151514, eqtls.size());
		
		check(eqtls);
	}

	@Test
	public void directAgingBoneAsMapReader() throws Exception {
		
		ReaderRequest request = new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO.csv"));
		request.setReaderHint("MapCSVReader");
		StreamReader<Map<String,String>> reader = ReaderFactory.getReader(request);
		assertTrue(reader instanceof MapCSVReader);
		Map<String,String> firstWrong = reader.stream().findAny().orElse(null);
		// It gets the wrong headers.
		assertTrue(firstWrong.get("17_9239543")!=null);
	}

	@Test
	public void directAgingBoneSetHeaderOverride() throws Exception {
		
		ReaderRequest request = new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO_pr69k.csv"));
		request.setReaderHint("MapCSVReader");
		AbstractCSVReader<Map<String,String>> reader = ReaderFactory.getReader(request);
		reader.setHeaderOverride(Arrays.asList("marker","chr","bp_mm10","gene_id","rs_id","lod"));
		assertTrue(reader instanceof MapCSVReader);
		Map<String,String> firstWrong = reader.stream().findAny().orElse(null);
		// It gets the right headers because we override them.
		assertEquals("9_29539511", firstWrong.get("marker"));
		assertEquals("29539511", firstWrong.get("bp_mm10"));
	}

	@Test
	public void directAgingBoneHeadersFromLastCommentLine() throws Exception {
		
		ReaderRequest request = new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO_pr69k.csv"));
		request.setReaderHint("MapCSVReader");
		AbstractCSVReader<Map<String,String>> reader = ReaderFactory.getReader(request);
		reader.readHeadersFromLastCommentLine(); // This will close the stream so cannot be used in streaming mode.
		assertTrue(reader instanceof MapCSVReader);
		Map<String,String> first = reader.stream().findAny().orElse(null);
		// It gets the right headers because we override them.
		assertEquals("3_107201964", first.get("marker"));
		assertEquals("107201964", first.get("bp_mm10"));
	}

	@Test(expected = ReaderException.class)
	public void directAgingBoneHeadersFromLastCommentLineStream() throws Exception {
		
		InputStream in = Files.newInputStream(getPath("data/eQTL/mm/Aging_Bone_DO_pr69k.csv"));
		ReaderRequest request = new ReaderRequest(in, "Mus musculus");
		request.setReaderHint("MapCSVReader");
		AbstractCSVReader<Map<String,String>> reader = ReaderFactory.getReader(request);
		reader.readHeadersFromLastCommentLine(); // This will close the stream so cannot be used in streaming mode.
	}

	@Test
	public void factory() throws Exception {
		StreamReader<EQTL> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO.csv")));
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		assertEquals(151514, eqtls.size());
		
		check(eqtls);
		
		eqtls.stream().allMatch(e->e.getStudyId().equals("Project999901"));
	}
	

	@Test
	public void folder() throws Exception {
		
		Path dir = getPath("data/eQTL/mm/");
		Files.list(dir).forEach(path-> {
			try {
				StreamReader<EQTL> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", path));
				long start = System.currentTimeMillis();
				List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
				long end = System.currentTimeMillis();
				check(eqtls);
				System.out.println(String.format("Read %s EQTLs from %s in %s ms", eqtls.size(), path.getFileName(), end-start));
			} catch (Exception ne) {
				fail(ne.getMessage());
			}
		});
		
	}

	private void check(List<EQTL> eqtls) {
		eqtls.forEach(e-> {
			assertNotNull(e.getRsId()); // It might be "NA" though
			assertNotNull(e.getGeneId());
			assertNotNull(e.getPopulation());
			assertNotNull(e.getTissueName());
			assertNotNull(e.getBp());
		});		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badHeaderDirect() throws Exception {
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badHeader.csv")));
		reader.stream().collect(Collectors.toList());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues1Direct() throws Exception {
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues1.csv")));
		reader.stream().collect(Collectors.toList());
	}
	
	@Test(expected=AssertionError.class)
	public void badValues2Direct() throws Exception {
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues2.csv")));
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		eqtls.forEach(e-> {
			assertNotNull(e.getRsId()); // Some are empty
		});	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues3Direct() throws Exception {
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues3.csv")));
		reader.stream().collect(Collectors.toList());
	}

	@Test(expected=IllegalArgumentException.class)
	public void noHeaderDirect() throws Exception {
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_noHeader.csv")));
		reader.stream().collect(Collectors.toList());
	}
	
}
