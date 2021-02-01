/*-
 * 
 * Copyright 2018, 2020  The Jackson Laboratory Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Matthew Gerring
 */
package org.jax.gweaver.io.reader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


// TODO: Auto-generated Javadoc
/**
 * Simple table which parses the file into a
 * Map of ids such that the grpah created from
 * the main code can be checked.
 * 
 * @author gerrim
 *
 */
public class GeneTable {

	/** The path. */
	private File path;
	
	/** The assignment char. */
	private String assignmentChar;

	/**
	 * Instantiates a new gene table.
	 *
	 * @param path the path
	 * @param assignmentChar the assignment char
	 */
	public GeneTable(File path, String assignmentChar) {
		this.path = path;
		this.assignmentChar = assignmentChar;
	}
	
	/**
	 * Parses the.
	 *
	 * @return the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Map<String, Set<String>> parse() throws IOException {
		
		Map<String, Set<String>> ret = new HashMap<>();
		try (Scanner scanner = new Scanner(path)) {
	
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.startsWith("#")) continue;
				String[] rec = line.split("\t");
				
				String type = rec[2];
				
				// Genes and Transcripts
				Map<String,Object> attributes = parseAttributes(rec[8]);
				String geneId = attributes.get("gene_id").toString().split(":")[0];

				if ("gene".equalsIgnoreCase(type)) {
					ret.put(geneId, new HashSet<>());
				} else if ("transcript".equalsIgnoreCase(type)) {
					String transId = attributes.get("transcript_id").toString();
					ret.get(geneId).add(transId);
				}
			}
			
		}
		return ret;
	}
	
	/**
	 * Parses the attributes.
	 *
	 * @param rec8 the rec 8
	 * @return the map
	 */
	protected Map<String, Object> parseAttributes(String rec8) {
		// Split attributes in rec[8]
        // str: gene_id "ENSMUSG00000102693"; gene_version "1"; gene_name "4933401J01Rik"; gene_source "havana"; gene_biotype "TEC"; havana_gene "OTTMUSG00000049935"; havana_gene_version "1";
        String [] attr = rec8.split(";");
        Map<String,Object> attributes = new HashMap<>();
        for (int i = 0; i < attr.length; i++) {
        	String line = attr[i].trim().replace("\"", "");
			String[] kv = line.split(assignmentChar);
			if (kv.length==2) attributes.put(kv[0], kv[1].trim());
		}
        return attributes;
    }

}
