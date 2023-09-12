package org.geneweaver.io.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class MapCSVReader extends AbstractCSVReader<Map<String, String>> {
	@Override
	protected Map<String, String> create(CSVRecord row) throws ReaderException {
		return row.toMap();
	}
	
	// Can be used to fix lines read in with funny invisible 
	// whitespace characters in UTF-16 space.
	public static Map<String,String> toAscii(Map<String,String> line) {
		Map<String,String> ret = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : line.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			key = key.replaceAll("[^\\x00-\\x7F]", "");
			ret.put(key, val);
		}
		return ret;
	}
}
