package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

public class MapCSVReaderTest extends AbstractDataFileTest {

	
	@Test
	public void csv() throws Exception {
		
		StreamReader<Map<String,String>> reader = new MapCSVReader();
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snps.csv"));
		reader.init(req);
		
		check(reader);
	}
	
	@Test
	public void rs() throws Exception {
		
		StreamReader<Map<String,String>> reader = new MapCSVReader();
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snps.csv"));
		reader.init(req);
		
		List<String> rsIds = reader.stream()
							      .map(map->map.get("rs"))
							      .collect(Collectors.toList());
		
		assertTrue(rsIds.containsAll(Arrays.asList("", "rs33038652", "rs33485892", "rs33264419", "rs33719054", "rs33261861", "rs6281391", "rs29532926", "rs6209993", "rs6210677")));
	}

	
	@Test
	public void tsv() throws Exception {
		
		StreamReader<Map<String,String>> reader = new MapCSVReader();
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snps.tsv"));
		req.setDelimiter("\t");
		reader.init(req);
		
		check(reader);
	}
	
	@Test(expected=AssertionError.class)
	public void wrongDelim() throws Exception {
		
		StreamReader<Map<String,String>> reader = new MapCSVReader();
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snps.tsv"));
		req.setDelimiter("|");
		reader.init(req);
		
		check(reader);
	}

	private void check(StreamReader<Map<String, String>> reader) throws ReaderException {
		
		reader.stream().forEach(map->{
			if (map.size()<26) fail("Incorrect headers to values map");
		});
		assertEquals(100, reader.stream().count());
	}

}
