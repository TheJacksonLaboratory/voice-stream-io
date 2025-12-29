package org.jax.voice.io.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * This converter is designed to convert large json files
 * from using the neo4j service with curl into large csv tables.
 * 
 * @author gerrim
 *
 */
public class JsonToCSVParser {

	private String delimiter = ",";
	
	public JsonToCSVParser() {
		
	}
	
	public void convert(Path jsonFrom, Path csvTo) throws JsonParseException, IOException {
		
		csvTo.getParent().toFile().mkdirs();
		
		JsonFactory factory = new JsonFactory();
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(createInput(jsonFrom)));
			 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(createOutput(csvTo)))) {
			
			JsonParser parser = factory.createParser(in);
			while (parser.nextToken() != null) {
				
				if (!parser.isExpectedStartArrayToken()) continue;
				if ("columns".equals(parser.currentName())) {
					writeLine(parser, out);
				}
				if ("row".equals(parser.currentName())) {
					writeLine(parser, out);
				}
			}

		}
	}

	private void writeLine(JsonParser parser, BufferedWriter out) throws IOException {
		JsonToken token = parser.nextToken();
		while(token!=JsonToken.END_ARRAY) {
			out.write(parser.getValueAsString());
			token = parser.nextToken();
			if (token!=JsonToken.END_ARRAY) {
				out.write(getDelimiter());
			}
		}
		out.newLine();		
	}

	private InputStream createInput(Path path) throws IOException {
		InputStream in = Files.newInputStream(path);
		if (path.toString().endsWith(".gz")) {
			return new GZIPInputStream(in);
		}
		return in;
	}
	private OutputStream createOutput(Path path) throws IOException {
		OutputStream out = Files.newOutputStream(path);
		if (path.toString().endsWith(".gz")) {
			return new GZIPOutputStream(out);
		}
		return out;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}
