package org.geneweaver.ccsi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.geneweaver.io.reader.AbstractDataFileTest;
import org.geneweaver.io.reader.MapCSVReader;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.junit.Test;

/**
 * Sorts CCSI Data and performs a test of streaming.
 * @author gerrim
 *
 */
public class CCSISorter extends AbstractDataFileTest {
	
	private static Map<String,String> prefixes;
	static {
		prefixes = new HashMap<>();
		prefixes.put("chia-pet", 	"chia");
		prefixes.put("situhi-c", 	"hic");
		prefixes.put("chi-c", 		"hic");
		prefixes.put("hi-c", 		"hic");
	}

	/**
	 * This is actually not a test. I am using a test like a script here.
	 * 
	 * 1. Run prod/ccsi/download.sh to get the ccsi data. We actually sort and store this data
	 * in case the ccsi website is taken down.
	 * 
	 * 2. Run this sorter to put it into the mouse and human directories. 
	 * 
	 * @throws Exception
	 */
	@Test
	public void sortCCSI() throws Exception {
		
		Path info = getPath("prod/ccsi/CCSI_annotation.csv");
		assertTrue(Files.exists(info));
		
		Path dir = info.getParent();
		dir.resolve("mm").toFile().mkdirs();
		dir.resolve("hs").toFile().mkdirs();
		
		ReaderRequest req = new ReaderRequest(info.toFile());
		req.setReaderHint("MapCSVReader");
		StreamReader<Map<String,String>> reader = ReaderFactory.getReader(req);
		reader.stream()
			  .map(MapCSVReader::toAscii)
			  .map(line->sortLine(dir, line))
			  .count();
			  
	}

	private Map<String,String> sortLine(Path dir, Map<String, String> line) {
		
		try {
			String fnum = line.get("filename_num");
			String meth = line.get("method");
			int num = Integer.parseInt(fnum.toString());
			meth = meth.toLowerCase().replace(" ", "");
			
			String fileName = meth+"-"+num+".step.gz";
			Path file = dir.resolve(fileName);
			if (!Files.exists(file)) {
				meth = prefixes.get(meth);
				assertNotNull("No mapping for: "+line.get("method"), meth);
				fileName = meth+"-"+num+".step.gz";
			}
			file = dir.resolve(fileName);
			assertTrue(fileName+" does not exist", Files.exists(file));
			
			prepend(file, line);
			
			// We now have a file for this line. So we can sort the step
			// file in to mm or hs.
			String spec = line.get("species");
			if (spec.equalsIgnoreCase("mouse")) {
				Files.copy(file, dir.resolve("mm").resolve(file.getFileName()));
			} else if (spec.equalsIgnoreCase("human")) {
				Files.copy(file, dir.resolve("hs").resolve(file.getFileName()));
			}
			Files.delete(file);
			
		} catch (Exception ne) {
			fail(ne.getMessage());
		}
		
		return line;
	}

	private Map<String,String> prepend(Path file, Map<String, String> line) throws IOException {
		
		Path tmp = Files.createTempFile(file.getParent(), "tmp", ".gz");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(file))));
			 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(tmp))))) {
			
			for (Map.Entry<String, String> entry : line.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue();
				writer.write("# ");
				writer.write(key);
				writer.write(" = ");
				writer.write(val);
				writer.newLine();
			}
			
			reader.lines().forEach(ln->{
				try {
					writer.write(ln);
					writer.newLine();
				} catch (IOException e) {
					fail(e.getMessage());
				}
			});
		}
		Files.delete(file);
		Files.copy(tmp, file);
		Files.delete(tmp);
		return line;
	}
}
