package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geneweaver.io.JsonConverter;
import org.geneweaver.io.writer.JsonToCSVParser;
import org.junit.Test;

public class JsonToCSVParserTest extends AbstractDataFileTest {

	private JsonToCSVParser parser = new JsonToCSVParser();
	
	@Test
	public void tinyFile() throws Exception {
		
		Path in  = getPath("data/json/test_25.json");
		Path out = Paths.get("tmp/json/test_25.csv");
		parser.convert(in, out);
		
		assertTrue(Files.exists(out));
		assertEquals(26, Files.readAllLines(out).size());
	}
	
	@Test
	public void tinyFileViaMain() throws Exception {
		
		Path in  = getPath("data/json/test_25.json");
		Path out = Paths.get("tmp/json/test_25_main.csv");
		JsonConverter.main(in.toAbsolutePath().toString(), out.toAbsolutePath().toString());
		
		assertTrue(Files.exists(out));
		assertEquals(26, Files.readAllLines(out).size());
	}

	@Test
	public void tinyFileViaMainWithTab() throws Exception {
		
		Path in  = getPath("data/json/test_25.json");
		Path out = Paths.get("tmp/json/test_25_main_tab.csv");
		JsonConverter.main(in.toAbsolutePath().toString(), out.toAbsolutePath().toString(), "TAB");
		
		assertTrue(Files.exists(out));
		assertEquals(26, Files.readAllLines(out).size());
	}
	

	@Test
	public void mainWrongArgs1() throws Exception {
		
		JsonConverter.main();
	}

	@Test
	public void mainWrongArgs2() throws Exception {
		
		JsonConverter.main("Hello");
	}

	@Test
	public void mainWrongArgs3() throws Exception {
		
		JsonConverter.main("Hello", "WORLD");
	}

	@Test
	public void mainWrongArgs4() throws Exception {
		
		JsonConverter.main("Hello", "WORLD", ":");
	}

	@Test
	public void mainWrongArgs5() throws Exception {
		Path in  = getPath("data/json/test_25.json");
		Path out = Paths.get("tmp/json/test_25_main_tab.csv");
		JsonConverter.main(in.toAbsolutePath().toString(), out.toAbsolutePath().toString(), ":", "Too Many!");
	}

	@Test
	public void smallFile() throws Exception {
		
		Path in  = getPath("data/json/test_1000.json");
		Path out = Paths.get("tmp/json/test_1000.csv");
		parser.convert(in, out);
		
		assertTrue(Files.exists(out));
		assertEquals(1001, Files.readAllLines(out).size());
	}
	
	@Test
	public void mediumFile() throws Exception {
		
		Path in  = getPath("data/json/test_250k.json.gz");
		Path out = Paths.get("tmp/json/test_250k.csv.gz");
		parser.convert(in, out);
		
		assertTrue(Files.exists(out));
	}

}
