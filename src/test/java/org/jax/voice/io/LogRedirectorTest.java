package org.jax.voice.io;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.jax.voice.io.LogRedirector;
import org.junit.Test;

public class LogRedirectorTest {
	
	@Test
	public void noRollOver10() throws Exception {
		LogRedirector rd = new LogRedirector(Paths.get("tmp/test.log"), 100, 10);
		writeLines(rd, 10);
		checkLines(rd.getLog(), 10, "Line 0", "Line 9");
	}
	
	@Test
	public void noRollOver99() throws Exception {
		LogRedirector rd = new LogRedirector(Paths.get("tmp/test.log"), 100, 10);
		writeLines(rd, 99);
		checkLines(rd.getLog(), 99, "Line 0", "Line 98");
	}
	
	@Test
	public void noRollOver100() throws Exception {
		LogRedirector rd = new LogRedirector(Paths.get("tmp/test.log"), 100, 10);
		writeLines(rd, 100);
		checkLines(rd.getLog(), 100, "Line 0", "Line 99");
	}

	@Test
	public void noRollOver101() throws Exception {
		LogRedirector rd = new LogRedirector(Paths.get("tmp/test.log"), 100, 10);
		writeLines(rd, 101);
		checkLines(rd.getLog(), 91, "Line 10", "Line 100");
	}

	@Test
	public void noRollOverFullZipped() throws Exception {
		LogRedirector rd = new LogRedirector(Paths.get("tmp/test.log.gz"));
		writeLines(rd, 100001);
		checkLines(rd.getLog(), 99001, "Line 1000", "Line 100000");
	}

	private void checkLines(Path log, int size, String firstLine, String lastLine) throws IOException {
		try (BufferedReader br = LogRedirector.createBufferedReader(log)) {
			long found = br.lines().count();
			assertEquals(found, size);
		}
		try (BufferedReader br = LogRedirector.createBufferedReader(log)) {
			String first = br.readLine();
			assertEquals(firstLine, first);
		}
		try (BufferedReader br = LogRedirector.createBufferedReader(log)) {
			String last = br.lines().skip(size-1).findFirst().get();
			assertEquals(lastLine, last);
		}

	}

	private void writeLines(LogRedirector rd, int i) {
		IntStream.range(0, i)
				.forEach(j -> {
					rd.println("Line " + j);
				});
	}
}
