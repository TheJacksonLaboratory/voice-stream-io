package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
		
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snps.csv"));
		req.setReaderHint("Map");
		StreamReader<Map<String,String>> reader = ReaderFactory.getReader(req);
		
		check(reader);
	}
	
	@Test
	public void csvHeader() throws Exception {
		
		AbstractCSVReader<Map<String,String>> reader = new MapCSVReader();
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snps.csv"));
		reader.init(req);
		
		List<String> headers = reader.headers();
		assertNotNull(headers);
		assertEquals(254, headers.size());
		
		List<String> firstFew = Arrays.asList("chr", "bp38", "rs", "observed", "dbsnp142annot", "requested", "129P2/OlaHsd", "129S1/SvImJ", "129S2/SvHsd", "129S4/SvJaeJ", "129S6/SvEvTac");
		assertTrue(headers.containsAll(firstFew));
	}
	
	@Test
	public void csvWithComments() throws Exception {
		
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snp_ucla_bp38_ordered.csv.gz"));
		req.setReaderHint("Map");
		AbstractCSVReader<Map<String,String>> reader = ReaderFactory.getReader(req);
		
		List<String> headers = reader.headers();
		assertNotNull(headers);
		assertEquals(3, headers.size());
		
		assertEquals(132277, reader.stream().count());
		assertEquals(131908, reader.stream().filter(row->row.get("rs")!=null).count());
	}

	
	@Test
	public void csvHeaderTrailingDelimiter() throws Exception {
		
		ReaderRequest req = new ReaderRequest(getFile("data/csv/snp_UCLA_100.csv"));
		req.setReaderHint("Map");
		AbstractCSVReader<Map<String,String>> reader = ReaderFactory.getReader(req);

		List<String> headers = reader.headers();
		assertNotNull(headers);
		assertEquals(254, headers.size());
		
		List<String> firstFew = Arrays.asList("chr", "bp38", "rs", "observed", "dbsnp142annot", "requested", "129P2/OlaHsd", "129S1/SvImJ", "129S2/SvHsd", "129S4/SvJaeJ", "129S6/SvEvTac");
		assertTrue(headers.containsAll(firstFew));
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
