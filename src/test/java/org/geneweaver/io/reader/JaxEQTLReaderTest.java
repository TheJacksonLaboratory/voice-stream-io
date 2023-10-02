package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.geneweaver.domain.EQTL;
import org.junit.Test;

public class JaxEQTLReaderTest extends AbstractDataFileTest {

	
	@Test
	public void directAgingBone() throws Exception {
		
		JaxEQTLReader<EQTL> reader = new JaxEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO.csv")));
		
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		assertEquals(46177, eqtls.size());
		
		check(eqtls);
	}


	@Test
	public void factory() throws Exception {
		StreamReader<EQTL> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm/Aging_Bone_DO.csv")));
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		assertEquals(46177, eqtls.size());
		
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
			assertNotNull(e.getStrain());
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
