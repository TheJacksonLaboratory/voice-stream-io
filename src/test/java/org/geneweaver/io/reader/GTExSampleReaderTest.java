package org.geneweaver.io.reader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geneweaver.domain.EQTL;
import org.geneweaver.domain.Sample;
import org.geneweaver.io.connector.TissueKey;
import org.junit.Test;

public class GTExSampleReaderTest extends AbstractDataFileTest {

	@Test
	public void checkAllText() throws Exception {
		
		StreamReader<Sample> samples = ReaderFactory.getReader(new ReaderRequest(getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz")));
		assertNotNull(samples);
		
		Set<Sample> entries = new TreeSet<>();
		samples.stream().forEach(entries::add);
		
		// Check that the tissue is not a number which might be one of the other columns.
		entries.stream().forEach(s->{
			assertFalse("Group is "+s.getTissueGroup(), s.getTissueGroup().matches("\\d+"));
			assertFalse("Name is "+s.getTissueGroup(), s.getTissueName().matches("\\d+"));
		});
	}
	
	/**
	 * Test that we match every single GTEx file name using a map to a sample.
	 * @throws Exception
	 */
	@Test
	public void matchingFileNames() throws Exception {
		
		// Make the tissue rough keys
		StreamReader<Sample> samples = ReaderFactory.getReader(new ReaderRequest(getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz")));
		Map<TissueKey, Sample> roughMap = new HashMap<>();
		samples.stream().forEach(s->roughMap.put(new TissueKey(s), s));

		// These are all the names we want to match.
		List<String> fnames = Files.readAllLines(getPath("data/eQTL/names.txt"));

		for (String name : fnames) {
			EQTL eqtl = new EQTL();
			GTExEQTLReader.setTissueInfo(eqtl, name);
			
			Sample sample = roughMap.get(new TissueKey(eqtl.getTissueName()));
			assertNotNull("Not matched: "+eqtl.getTissueName(), sample);
		}
	}
}
