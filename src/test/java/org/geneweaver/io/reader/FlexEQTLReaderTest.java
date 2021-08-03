package org.geneweaver.io.reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.geneweaver.domain.EQTL;
import org.junit.Test;

public class FlexEQTLReaderTest extends AbstractDataFileTest {

	
	@Test
	public void direct() throws Exception {
		
		Path dir = getPath("data/eQTL/hs");
		Files.list(dir).forEach( path-> {
			try {
				StreamReader<EQTL> reader = new FlexEQTLReader<>();
				reader.init(new ReaderRequest("Homo sapiens", path));
				
				List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
				assertTrue(eqtls.size()==499);
				check(eqtls, false);
			} catch (Exception ne) {
				fail(ne.getMessage());
			}
		});
		
	}

	@Test
	public void factory() throws Exception {
		
		Path dir = getPath("data/eQTL/hs");
		Files.list(dir).forEach( path-> {
			try {
				StreamReader<EQTL> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", path));
				
				List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
				assertTrue(eqtls.size()==499);
				check(eqtls, true);
				
			} catch (Exception ne) {
				fail(ne.getMessage());
			}
		});

	}
	
	private void check(List<EQTL> eqtls, boolean checkTissue) {
		eqtls.forEach(e-> {
			assertNotNull(e.getRsId()); // It might be "NA" though
			assertNotNull(e.getGeneId());
			if (checkTissue) assertNotNull(e.getTissueName());
		});		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badHeaderDirect() throws Exception {
		StreamReader<EQTL> reader = new FlexEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badHeader.csv"), ","));
		reader.stream().collect(Collectors.toList());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues1Direct() throws Exception {
		StreamReader<EQTL> reader = new FlexEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues1.csv"), ","));
		reader.stream().collect(Collectors.toList());
	}
	
	@Test(expected=AssertionError.class)
	public void badValues2Direct() throws Exception {
		StreamReader<EQTL> reader = new FlexEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues2.csv"), ","));
		List<EQTL> eqtls = reader.stream().collect(Collectors.toList());
		eqtls.forEach(e-> {
			assertNotNull(e.getRsId()); // Some are empty
		});	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void badValues3Direct() throws Exception {
		StreamReader<EQTL> reader = new FlexEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_badValues3.csv"), ","));
		reader.stream().collect(Collectors.toList());
	}

	@Test(expected=IllegalArgumentException.class)
	public void noHeaderDirect() throws Exception {
		StreamReader<EQTL> reader = new FlexEQTLReader<>();
		reader.init(new ReaderRequest("Mus musculus", getFile("data/eQTL/mm_bad/Mouse_eQTL_noHeader.csv"), ","));
		reader.stream().collect(Collectors.toList());
	}
	
}
