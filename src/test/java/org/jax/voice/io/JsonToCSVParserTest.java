package org.jax.voice.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.MissingOptionException;
import org.jax.voice.io.CLI;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.jax.voice.io.writer.JsonToCSVParser;
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
		
		CLI.testRun(line(in.toAbsolutePath().toString(), out.toAbsolutePath().toString()));
		
		assertTrue(Files.exists(out));
		assertEquals(26, Files.readAllLines(out).size());
	}

	private String[] line(String... args) {
		
		if (args.length<1) return null;
		
		String[] ret = new String[(args.length*2) + 1];
		ret[0] = "-convert";
		ret[1] = "-i";
		ret[2] = args[0];
		
		if (args.length>1) {
			ret[3] = "-o";
			ret[4] = args[1];
		}
		
		if (args.length>2) {
			ret[5] = "-d";
			ret[6] = args[2];
		}
		return ret;
	}

	@Test
	public void tinyFileViaMainWithTab() throws Exception {
		
		Path in  = getPath("data/json/test_25.json");
		Path out = Paths.get("tmp/json/test_25_main_tab.csv");
		CLI.testRun(line(in.toAbsolutePath().toString(), out.toAbsolutePath().toString(), "TAB"));
		
		assertTrue(Files.exists(out));
		assertEquals(26, Files.readAllLines(out).size());
	}
	

	@Test(expected=MissingOptionException.class)
	public void mainWrongArgs1() throws Exception {
		
		CLI.testRun(line());
	}

	@Test(expected=MissingOptionException.class)
	public void mainWrongArgs2() throws Exception {
		
		CLI.testRun(line("Hello"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void mainWrongArgs3() throws Exception {
		
		CLI.testRun(line("Hello", "WORLD"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void mainWrongArgs4() throws Exception {
		
		CLI.testRun(line("Hello", "WORLD", ":"));
	}

	@Test(expected=NullPointerException.class)
	public void mainWrongArgs5() throws Exception {
		Path in  = getPath("data/json/test_25.json");
		Path out = Paths.get("tmp/json/test_25_main_tab.csv");
		CLI.testRun(line(in.toAbsolutePath().toString(), out.toAbsolutePath().toString(), ":", "Too Many!"));
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
