package org.geneweaver.io.reader;

import java.util.Map;

import org.apache.commons.csv.CSVRecord;

class MapCSVReader extends AbstractCSVReader<Map<String, String>> {
	@Override
	protected Map<String, String> create(CSVRecord row) throws ReaderException {
		return row.toMap();
	}
}
