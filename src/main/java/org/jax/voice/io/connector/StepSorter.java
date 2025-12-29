package org.jax.voice.io.connector;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jax.voice.io.reader.MapCSVReader;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;

/**
 * Sorts CCSI Data into mouse and human using the annotation list.
 * 
 * This action is needed once after downloading the CCSI data. It is not needed
 * every rebuild of the data because we store the sorted data in directories and
 * we annotate that data with the metadata required.
 * 
 * @author gerrim
 *
 */
public class StepSorter {
	
	private static Map<String,String> prefixes;
	static {
		prefixes = new HashMap<>();
		prefixes.put("chia-pet", 	"chia");
		prefixes.put("situhi-c", 	"hic");
		prefixes.put("chi-c", 		"hic");
		prefixes.put("hi-c", 		"hic");
	}

	/**
	 * 1. Run prod/ccsi/download.sh to get the ccsi data. We actually sort and store this data
	 * in case the ccsi website is taken down.
	 * 
	 * 2. Run this sorter to put it into the mouse and human directories. 
	 * 
	 * @param ccsiAnot e.g. "prod/ccsi/CCSI_annotation.csv" in test data.
	 * @throws Exception
	 */
	public void sortCCSI(Path ccsiAnot) throws Exception {
		
		assert (Files.exists(ccsiAnot));
		
		Path dir = ccsiAnot.getParent();
		dir.resolve("mm").toFile().mkdirs();
		dir.resolve("hs").toFile().mkdirs();
		
		ReaderRequest req = new ReaderRequest(ccsiAnot.toFile());
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
				if (meth == null) throw new NullPointerException("No mapping for: "+line.get("method"));
				fileName = meth+"-"+num+".step.gz";
			}
			file = dir.resolve(fileName);
			if (!Files.exists(file)) throw new IllegalArgumentException(fileName+" does not exist");
			
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
			// Used in lambda so we wrap in a runtime.
			throw new RuntimeException(ne.getMessage());
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
					// Used in lambda so we wrap in a runtime.
					throw new RuntimeException(e.getMessage());
				}
			});
		}
		Files.delete(file);
		Files.copy(tmp, file);
		Files.delete(tmp);
		return line;
	}
	
	/**
	 * entry point for running the sorting or can just use a unit
	 * test to run this class. The action of running the sorter is
	 * not needed regularly.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length!=1) throw new IllegalArgumentException("One argument which is the CCSI_annotation CSV file please.");
		Path annot = Paths.get(args[0]);
		StepSorter sorter = new StepSorter();
		sorter.sortCCSI(annot);
	}
}
