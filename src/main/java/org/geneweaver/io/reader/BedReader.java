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
package org.geneweaver.io.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.NamedEntity;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.Peak.Strand;
import org.geneweaver.domain.Species;
import org.geneweaver.domain.Track;
import org.geneweaver.io.connector.BedConnector;
import org.geneweaver.io.connector.ChromosomeService;

/**
 * Bed file format @see https://m.ensembl.org/info/website/upload/bed.html
 * @see https://en.wikipedia.org/wiki/BED_(file_format)#:~:text=is%20widely%20used.-,Description,coordinates%20of%20the%20sequences%20considered.
 * @author gerrim
 *
 * @param <N>
 */
public class BedReader<N extends NamedEntity> extends LineIteratorReader<N> {
	

	private ChromosomeService cservice = ChromosomeService.getInstance();
	
	private static AtomicLong peakIdCounter = null;
	
	public static void clearCounting() {
		peakIdCounter = null;
	}
	
	/**
	 * Create the reader by setting its data
	 * 
	 * @param request
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BedReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\\s+");
		
		// The id counter starts from a base value. In this way
		// external commands can control the value from which the
		// counter starts and we can ingest human and mouse at the
		// same time for instance.
		if (peakIdCounter==null) {
			Long start = this.request!=null && this.request.getStartIndex()!=null 
					   ? this.request.getStartIndex() 
					   : 0L;
			peakIdCounter = new AtomicLong(start);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
		
		N ret;
		if (line.startsWith("track ")) {
        	String ln = line.substring(5); // Remove track
			Map<String,String> attr = parseQuotedAttributes(ln);
			
			Track track = new Track();
			BeanMap d = new BeanMap(track);
			d.put("name", attr.get("name"));
			d.put("type", attr.get("type"));
			d.put("graphType", attr.get("graphType"));
			d.put("description", attr.get("description"));
			if (attr.containsKey("priority")) d.put("priority", attr.get("priority"));
			if (attr.containsKey("color")) {
				track.setColor(getIntArray(attr.get("color"), 3));
			}
			if (attr.containsKey("useScore")) d.put("useScore", attr.get("useScore"));
			if (attr.containsKey("itemRgb")) {
				String val = attr.get("itemRgb");
				track.setItemRgb("on".equals(val));
			}

			ret = (N)track;
			
		} else {
			String[] rec = line.split(getDelimiter());
			Peak peak = new Peak();
			
			BeanMap d = new BeanMap(peak);
			
			// At one time we allowed the bad chromosomes to
			// come in through the peaks but now we do not.
			String chrom = cservice.getChromosome(rec[0]);
			if (chrom==null) return null;
			d.put("chr", chrom);
			d.put("start", rec[1]);
			d.put("end",   rec[2]);
			if (rec.length>3) d.put("name",  rec[3]);
			if (rec.length>4) d.put("score", rec[4]);
			if (rec.length>5) d.put("strand", Strand.from(rec[5]));
			if (rec.length>6) d.put("thickStart",  rec[6]);
			if (rec.length>7) d.put("thickEnd",    rec[7]);
			if (rec.length>8) {
				peak.setItemRgb(getIntArray(rec[8], 3));
			}
			if (rec.length>9) d.put("blockCount",  rec[9]);
			if (rec.length>10) d.put("blockSizes",  getIntArray(rec[10], 1));
			if (rec.length>11) d.put("blockStarts", getIntArray(rec[11], 1));
			
			parseName(d);
			
			String epi  = peak.getEpigenome();
			String feat = peak.getFeatureType();
			if (epi==null && feat!=null) return null;
			createPeakId(peak);

			ret = (N)peak;
		}
		
		ret.setSpecies(getSpecies());
		return ret;
	}

	public Stream<N> stream() {
		return super.stream();
	}
	
	private Peak createPeakId(Peak peak) {
		
		int start = peak.getStart();
		int end = peak.getEnd();
		Long peakId = createPeakId(peak.getFeatureType(), peak.getChr(), start, end, peak.getTissueDescription());
		peak.setPeakId(peakId);
		return peak;
	}

	
	/**
	 * Try to make a repeatable unique peakId from the properties
	 * of the peak.
	 * 
	 * @param featureName
	 * @param chr
	 * @param path
	 * @param start
	 * @param end
	 * @param removeSpecialChars
	 * @return the peak id as a string.
	 */
	public Long createPeakId(String featType, String chr, int start, int end, String tissue) {
		
		if (peakIdCounter==null) {
			synchronized(this) {
				if (peakIdCounter==null) {
					peakIdCounter = new AtomicLong(this.request.getStartIndex());
				}
			}
		}
		return peakIdCounter.getAndIncrement();
	}

	// Parse name in Ensembl format e.g. 
	// BCL3_A549__Enriched_Site
	// H3K4me1_embryonic_facial_prominence_embryonic_10_5_days__Enriched_Site
	private static final Pattern pattern = Pattern.compile("([a-zA-Z0-9]+)_([a-zA-Z0-9_]+)__Enriched_Site");
	
	/**
	 * The name encodes the featureType and o
	 * @param d
	 * @throws ReaderException 
	 */
	private void parseName(BeanMap d) throws ReaderException {
		
		Object name = d.get("name");
		if (name==null) return;
		Matcher matcher = pattern.matcher(name.toString());
		if (!matcher.matches()) return;
		
		d.put("featureType", matcher.group(1));
		
		String egen = matcher.group(2);
		d.put("epigenome", egen);
		
		Map<String,String> des = getEpigenomeDescriptions(getSpecies());
		if (des!=null) {
			String ekey = getKey(egen);
			String descr = des.get(ekey);
			if (descr!=null) {
				d.put("tissueDescription", descr);
			}
		}
	}
	
	Peak testParseName(String name) throws ReaderException {
		Peak peak = new Peak(name);
		parseName(new BeanMap(peak));
		return peak;
	}
 
	private int[] getIntArray(String string, int min) {
		String[] col = string.split(",");
		Collection<Integer> ret = new LinkedList<>();
		for (String c : col) {
			ret.add(Integer.parseInt(c));
		}
		if (ret.size()<min) {
			for (int i = ret.size(); i < min; i++) {
				ret.add(0);
			}
		}
		return toArray(ret);
	}

	private int[] toArray(Collection<Integer> ret) {
		int[] ia = new int[ret.size()];
		Iterator<Integer> it = ret.iterator();
		for (int i = 0; i < ia.length; i++) {
			ia[i] = it.next();
		}
		return ia;
	}

	@Override
	protected String getAssignmentChar() {
		return "=";
	}

	@Override
	public <U extends Entity> Function<N, Stream<U>> getDefaultConnector() {
		Function<N, Stream<U>> func = new BedConnector<N, U>();
		return func;
	}

	/**
	 * This is static data residing in classpath data. We keep it in memory
	 * once loaded to reduce parsing if there are a lot of BedReaders created.
	 * This can be the case (1000's at least) when parsing all the files to build
	 * the graph.
	 */
	private static final Map<String,Map<String,String>> descriptions = new HashMap<>();
	
	Map<String,String> getEpigenomeDescriptions(Integer speciesCode) throws ReaderException {
		
		String species = Species.getSpeciesName(speciesCode);
		if (species==null) return Collections.emptyMap();
		if (descriptions.get(species)!=null) return descriptions.get(species);
		
		String path ="/epigenome_description/"+species.replace(" ", "_")+".tsv";
		InputStream in = getClass().getResourceAsStream(path);
		if (in == null) {
			try {
				String local = "src/main/resources"+path;
				in = Files.newInputStream(Paths.get(local));
			} catch (IOException ignored) {
				// Of the local path cannot be determined,
				// we ignore that we cannot do tissue lookups.
				return null;
			}
		}
		
		ReaderRequest req = new ReaderRequest(species, in, path);
		req.setReaderHint("MapCSVReader");
		req.setDelimiter("\t");
		StreamReader<Map<String,String>> lines = ReaderFactory.getReader(req);
		
		final Map<String,String> fdescr = new HashMap<>();
		lines.stream().forEach(m->{
			String ekey = getKey(m.get("Epigenome"));
			String des = m.get("Description");
			fdescr.put(ekey, des);
		});
		
		descriptions.put(species, fdescr);
		return fdescr;
	}

	private static Pattern postPattern = Pattern.compile("([a-z0-9]+?)(postnatal(\\d+)days)");
	private static Pattern embPattern = Pattern.compile("([a-z0-9]+?)(embryonic(\\d+)days)");
	private static Pattern daysPattern = Pattern.compile("([a-z0-9]+?)\\d+days");
	private static Pattern weeksPattern = Pattern.compile("([a-z0-9]+?)\\d+weeks");
	
	public static String getKey(final String origName) {
		String keyName = origName.toLowerCase();
		keyName = keyName.replaceAll("[^a-z0-9]+", "");
		
		// P0 in one key is postnatal_0_days in the other.
		Matcher post = postPattern.matcher(keyName);
		if (post.matches()) {
			keyName = post.group(1)+"p"+post.group(3);
		}

		// E10.5 in one key is embryonic_10_5_days in the other.
		Matcher emb = embPattern.matcher(keyName);
		if (emb.matches()) {
			keyName = emb.group(1)+"e"+emb.group(3);
		}
		
		// Remove (8weeks) from end.
		Matcher days = daysPattern.matcher(keyName);
		if (days.matches()) {
			keyName = days.group(1);
		}

		// Remove (8weeks) from end.
		Matcher weeks = weeksPattern.matcher(keyName);
		if (weeks.matches()) {
			keyName = weeks.group(1);
		}
		
		return keyName;
	}
}
