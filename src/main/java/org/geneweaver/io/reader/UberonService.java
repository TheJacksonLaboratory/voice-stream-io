package org.geneweaver.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This is a service which maps tissue information such as the file name
 * (terrible way to record metadata BTW) to the actual uberon type.
 */
class UberonService {

	private Map<String,Uberon> uberonMap;
	/**
	 * Create the service, reading the uberon mappings from a csv file.
	 * @throws IOException
	 */
	UberonService() {
		String path ="/UberonCLcodes.csv";
		InputStream in = getClass().getResourceAsStream(path);
		if (in == null) {
			try {
				String local = "src/main/resources"+path;
				in = Files.newInputStream(Paths.get(local));
			} catch (IOException ignored) {
				// Of the local path cannot be determined,
				// we ignore that we cannot do tissue lookups.
				this.uberonMap = null;
			}
		}
		
		this.uberonMap = read(in);
	}
	
	private Map<String, Uberon> read(InputStream in) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
		
			return reader.lines()
					.map(line->line.trim())
					.distinct()
					.filter(line -> !line.isBlank())
					.filter(line -> !line.startsWith("#"))
					.map(line -> line.split(","))
					.map(segs -> {
						Uberon key = new Uberon();
						key.setTerm(segs[1]);
						key.setUberon(segs[2]);
						return new Object[] {segs[0].toLowerCase(),key};
					})
					.collect(Collectors.toMap(
							oa -> (String)oa[0], 
							oa -> (Uberon)oa[1],
							(existingValue, newValue) -> existingValue));
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot read uberon mappings", e);
		}
	}
	
	/**
	 * Gets the best matching uberon term for the tissue name.
	 * This is done by returning the key with the most word matches
	 * from the tissue name.
	 * @param tissueName
	 * @return Best uberon code or null if not found.
	 */
	public String getUberonCode(String tissueName) {
		if (tissueName==null) return null;
		tissueName = tissueName.toLowerCase();
		
		if (uberonMap.containsKey(tissueName)) return uberonMap.get(tissueName).getUberon();
		
		return getBestMatchByWordContents(tissueName);
	}

	private String getBestMatchByWordContents(String tissueName) {
		
		String[] words = tissueName.split("\\s+|_|\\.");
		List<String> originalWords = Arrays.asList(words);
		TreeMap<Integer, String> matches = new TreeMap<Integer, String>(Collections.reverseOrder());
		for (String tKey : uberonMap.keySet()) {
			int count = 0;
			for (String word : words) {
				if (tKey.contains(word))
					count+=3;
			}
			
			// Every word not in the words reduces the match by 1.
			String[] tWords = tKey.split("\\s+|_|\\.");
			for (String tWord : tWords) {
				if (!originalWords.contains(tWord)) count-=1;
			}
			
			if (count>0) matches.put(count, tKey);
		}
		
		if (matches.isEmpty()) return null;
		String key = matches.firstEntry().getValue();
		if (key==null) return null;
		return this.uberonMap.get(key).getUberon();
	}

	public boolean isEmpty() {
		return this.uberonMap == null || this.uberonMap.isEmpty();
	}
}

class Uberon {
	private String uberon;
	private String term;
	/**
	 * @return the tissue
	 */
	public String getUberon() {
		return uberon;
	}
	/**
	 * @param tissue the tissue to set
	 */
	public void setUberon(String tissue) {
		this.uberon = tissue;
	}
	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}
	/**
	 * @param term the term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}
	@Override
	public int hashCode() {
		return Objects.hash(term, uberon);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Uberon))
			return false;
		Uberon other = (Uberon) obj;
		return Objects.equals(term, other.term) && Objects.equals(uberon, other.uberon);
	}
}
