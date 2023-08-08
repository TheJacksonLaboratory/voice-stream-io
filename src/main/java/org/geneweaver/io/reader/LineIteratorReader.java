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

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.Entity;

// TODO: Auto-generated Javadoc
/**
 * Class for readers of lines to types.
 * 
 * This class is a bit hard to understand because it is a Stream. The slight complexity
 * here is worth it to enable a Stream to be used with any file. Parallel streams might
 * be faster for processing large gene files.
 * 
 * @author Matthew Gerring
 * @param <T> The type of thing this reader will read.
 */
public abstract class LineIteratorReader<T extends Entity> extends AbstractStreamReader<T> implements Spliterator<T> {
	
	/** The Constant build. */
	private static final long build = generateBuildNumber(); // Once per vm execution

	/**
	 * The scanner for all the file(s) which we will parse. 
	 */
	protected Iterator<String> iterator;

	/** The count. */
	protected volatile int count;
	
	/**
	 * The type when winding to stop for. 
	 * This is used to get whole gene increments and make multi-threading work in the tests.
	 */
	private String windStopType;
	
	/**
	 * It is possible to reject a line after it has been read.
	 * The rejected line is then returned from the next nextLine() call.
	 */
	private String rejectedLine; 
	
	/**
	 * delimiter used to split strings. By default any whitespace.
	 */
	private String delimiter = System.getProperty("org.geneweaver.io.delimiter", "\\s+");
	
	/**
	 * Comment character
	 */
	private String comment = "#";
	
	/**
	 * Header lines, if any
	 */
	protected List<String> header;

	/**
	 * Instantiates a new abstract reader.
	 *
	 * @param species the species
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void setup(ReaderRequest request) throws ReaderException {
		
		if (request.getDelimiter()!=null) {
			setDelimiter(request.getDelimiter());
		}
		try {
			super.init(request);
			
			if (request.isNoInputStream()) {
				iterator = null;
			} else {
				// Iterate the file with a stream
				this.iterator = StreamUtil.createStream(request);
			}
			this.count = 0;
			
		} catch (IOException ne) {
			throw new ReaderException(ne);
		}
	}
	
	/**
	 * Testing only.
	 */
	protected LineIteratorReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * This stream must not be used in parallel stream programming.
	 * Use forkJoinStream() instead.
	 *
	 * @return the stream
	 */
	public Stream<T> stream() {
		
		header = null;
		if (isEmpty() && isDataSource()) {
			try {
				this.iterator = StreamUtil.createStream(request.getFile(), request.isCloseInputStream());
			} catch (IOException e) {
				throw new IllegalArgumentException("The scanner iterator cannot be recreated from "+request.getFile(), e);
			}
		}
		
		// Will throw an exception if called when empty
		return StreamSupport.stream(this, false);
	}
	
	/**
	 * Generate build number.
	 *
	 * @return the long
	 */
	private static long generateBuildNumber() {
		LocalDateTime now = LocalDateTime.now();
		String format = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));
		return Long.parseLong(format);
	}
       
	/**
	 * Parse the line to type T.
	 *
	 * @param line the line
	 * @return the t
	 * @throws ReaderException the reader exception
	 */
	protected abstract T create(String line) throws ReaderException;
		 
	/**
	 * The variants are encoded with delimiter space(' ') or '#'.
	 *
	 * @return the assignment char
	 */
	protected String getAssignmentChar() {
		return " ";
	}

	/**
	 * Transfer.
	 *
	 * @param propName the prop name
	 * @param from the from
	 * @param attrName the attr name
	 * @param to the to
	 * @return previous value in to or null if name is not in from or was not previously set in to.
	 */
	protected Object transfer(String propName, Map<String,String> from, String attrName, Map<Object,Object> to) {
		if (from.containsKey(propName)) {
			Set<Object> fields = to.keySet();
			if (fields.contains(attrName)) {
				return to.put(attrName, from.get(propName));
			}
		}
		return null;
	}

	/**
	 * Gets the builds the.
	 *
	 * @return the build
	 */
	public long getBuild() {
		return build;
	}

	/**
	 * Try advance.
	 *
	 * @param action the action
	 * @return true, if successful
	 */
	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		
		String line = nextLine();
		if (line == null) return false;
		T made = null;
		try {
			while ((made = create(line)) == null) {
				line = nextLine();
				if (line == null) return false;
			}
		} catch (NullPointerException | ReaderException e) {
			throw new IllegalArgumentException(e);
		}
		action.accept(made);
		return true;
	}
	
	/**
	 * Watch out for threading issues when calling .
	 *
	 * @param line the line
	 */
	private synchronized void rejectLine(String line) {
		rejectedLine = line;
	}
	
	/**
	 * This method is synchronized and only allows one thread at a time
	 * to hit the file and get the next line. When the reader is used with
	 * writing nodes to Neo4j the line reading is such shorter than the
	 * node save and also intermittent transaction.commit() slow the rate.
	 * This means that synchronizing each file read line is not thought a
	 * bottleneck. It would be possible to read more than one line per thread's
	 * request using a lock. This may be faster. To try this change the wind()
	 * method to grab a lock on the whole ScannerIterator and read a chunk
	 * exclusively.
	 * 
	 * @return next line, thread safe.
	 */
	protected synchronized String nextLine() {
		
		if (rejectedLine!=null) {
			String ret = rejectedLine;
			rejectedLine = null;
			return ret;
		}
		
		String line = null;
		try { 
			if (!iterator.hasNext()) {
				return line;
			}

			line = iterator.next();
			if (line==null) return line;
			
			// For speed reasons, we only support
			// comment characters at the start of a trimmed line.
			// We do not look for them in the body of a line.
			line = line.trim();
			if (!line.startsWith(comment)) ++count;
			while((line.isEmpty() || line.startsWith(comment)) && iterator.hasNext()) {
				
				if (line.startsWith(comment)) {
					addHeader(line);
				}
				
				line = iterator.next();
				if (line==null) return line;
				line = line.trim();
				if (! line.startsWith(comment)) ++count;
			}
			if (line.isEmpty())  line = null;
			return line;
			
		} catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException i) {
			return null;
		
		} finally {
	 		if (line == null) {
	 			if (iterator instanceof Closeable) {
	 				try {
	 					if (request.isCloseInputStream()) {
	 						((Closeable)iterator).close();
	 					}
					} catch (IOException e) {
						throw new IllegalArgumentException("The scanner closeable cannot close!", e);
					}
	 			}
	 		}
		}

	}

	protected void addHeader(String line) {
		if (header==null) header = new LinkedList<>();
		header.add(line);
	}

	/**
	 * Try split.
	 *
	 * @return the spliterator
	 */
	@Override
	public Spliterator<T> trySplit() {
		try {
			if (!iterator.hasNext()) {
				return null;
			}
		} catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException i) {
			return null;
		}
		Spliterator<String> split = wind(chunkSize).spliterator();
		Spliterator<T> wrapper = new LineWrapper(split);
		return wrapper;
	}
	
	/**
	 * This method winds forward in the file a given amount
	 * If the stopType is set (for instance if we require whole gene increments)
	 * then the exact amount wound on may be larger because it will not
	 * stop until the gene is reached.
	 *
	 * @return the list
	 * @throws ReaderException the reader exception
	 */
	public List<T> wind() throws ReaderException {
		List<String> lines = wind(getChunkSize());
		List<T> 	 items = new LinkedList<>(); // linked list is fast to add/iterate
		for (String line : lines) {
			T bean = create(line);
			if (bean!=null) {
				items.add(bean);
			}			
		}
		return items;
	}
	
	/**
	 * Wind.
	 *
	 * @param amount the amount
	 * @return the list
	 */
	private synchronized List<String> wind(int amount) {
		
		List<String> ls = new LinkedList<>(); // linked list is fast to add/iterate
		String line=null;
		for (int i = 0; i < amount; i++) {
			line = nextLine();
			if (line==null) break; // First null line is always the end
			ls.add(line);
		}
		
		if (line!=null && getWindStopType()!=null) { // We wind forward until the next section
			while((line = nextLine())!=null) {
				String[] rec = line.split("\t");
				String type = rec[2];
				if (!getWindStopType().equals(type.toLowerCase())) {
					ls.add(line);
				} else {
					rejectLine(line);
					break;
				}
			}
		}
		
		return ls;
	}
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return !iterator.hasNext();
	}

	/**
	 * Estimate size.
	 *
	 * @return the long
	 */
	@Override
	public long estimateSize() {
		if (request.getFile()==null) return 10000;
		String typical = "1	havana	transcript	11869	14409	.	+	.	gene_id \"ENSG00000223972\"; gene_version \"5\"; transcript_id \"ENST00000456328\"; transcript_version "
				+ "\"2\"; gene_name \"DDX11L1\"; gene_source \"havana\"; gene_biotype \"transcribed_unprocessed_pseudogene\"; transcript_name \"DDX11L1-202\"; transcript_source \""
				+ "havana\"; transcript_biotype \"processed_transcript\"; tag \"basic\"; transcript_support_level \"1\"; cannot be parsed ";
		int bytesPerLine = typical.getBytes().length;
		return request.getFile().length()/bytesPerLine;
	}

	/**
	 * Characteristics.
	 *
	 * @return the int
	 */
	@Override
	public int characteristics() {
		return Spliterator.IMMUTABLE | Spliterator.ORDERED;
	}

	/**
	 * The Class LineWrapper.
	 */
	private class LineWrapper implements Spliterator<T> {
		
		/** The lines. */
		private Spliterator<String> lines;

		/**
		 * Instantiates a new line wrapper.
		 *
		 * @param lines the lines
		 */
		LineWrapper(Spliterator<String> lines) {
			this.lines = lines;
		}

		/**
		 * Try advance.
		 *
		 * @param action the action
		 * @return true, if successful
		 */
		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			return lines.tryAdvance(line->{
				try {
					T bean = create(line);
					if (bean!=null) {
						action.accept(bean);
					}
				} catch (ReaderException e) {
					throw new RuntimeException(e);
				}
			});
		}
		
		/**
		 * Try split.
		 *
		 * @return the spliterator
		 */
		@Override
		public Spliterator<T> trySplit() {
			Spliterator<String> split = lines.trySplit();
			if (split==null) return null;
			return new LineWrapper(split);
		}
		
		/**
		 * Estimate size.
		 *
		 * @return the long
		 */
		@Override
		public long estimateSize() {
			return lines.estimateSize();
		}

		/**
		 * Characteristics.
		 *
		 * @return the int
		 */
		@Override
		public int characteristics() {
			return lines.characteristics();
		}
		
	}

	/**
	 * All lines processed including those ignored.
	 *
	 * @return the int
	 */
	public int linesProcessed() {
		return count;
	}

	/**
	 * Populate.
	 *
	 * @param d the d
	 * @param rec the rec
	 */
	protected void populate(BeanMap d, String[] rec) {
        d.put("sequenceId", rec[0]);
        
        String chr = rec[0];
        if (!chr.startsWith("chr")) chr = "chr"+chr;
        d.put("chr", chr);
        d.put("source", rec[1]);
        d.put("type", rec[2]);
        d.put("start", rec[3]);
        d.put("end", rec[4]);
        d.put("score", rec[5]);
        d.put("strand", rec[6]);
        if (rec[6].length() > 8) {
        	d.put("strand", rec[6].substring(0, 8));
        }
        
        // Do not repeat information, there will be millions of nodes.
        // d.put("attributes", rec[8]);
        d.put("active", Boolean.TRUE);
        d.put("build", getBuild());
        d.put("species", getSpecies());
	}

	/**
	 * Parses the attributes.
	 *
	 * @param rec8 the rec 8
	 * @return the map
	 */
	protected Map<String, String> parseAttributes(String rec8) {
		// Split attributes in rec[8]
        // str: gene_id "ENSMUSG00000102693"; gene_version "1"; gene_name "4933401J01Rik"; gene_source "havana"; gene_biotype "TEC"; havana_gene "OTTMUSG00000049935"; havana_gene_version "1";
        String [] attr = rec8.split(";");
        Map<String,String> attributes = new HashMap<>();
        for (int i = 0; i < attr.length; i++) {
        	String line = attr[i].trim().replace("\"", "");
			String[] kv = line.split(getAssignmentChar());
			if (kv.length==2) attributes.put(kv[0], kv[1].trim());
		}
        return attributes;
    }
	
	private Pattern quotedRegex;
	/**
	 * Parses the attributes.
	 *
	 * @param rec8 the rec 8
	 * @return the map
	 */
	protected Map<String, String> parseQuotedAttributes(String ln) {
		// Split attributes in ln
        // name="ItemRGBDemo" description="Item RGB demonstration" itemRgb="On" 
		if (quotedRegex==null) {
			String regex = "([a-zA-Z0-9_\\-\\.]+)"+getAssignmentChar()+"\"([a-zA-Z0-9_\\-\\. ]+)"+"\"";
			quotedRegex = Pattern.compile(regex);
		}
        Matcher matcher = quotedRegex.matcher(ln);
      	Map<String,String> attributes = new HashMap<>();
        while(matcher.find()) {
        	String key = matcher.group(1);
        	String val = matcher.group(2);
        	attributes.put(key, val.trim());
        }
        return attributes;
    }
	
	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException {
		request.close();
	}

	/**
	 * Gets the wind stop type.
	 *
	 * @return the windStopType
	 */
	private String getWindStopType() {
		return windStopType;
	}

	/**
	 * Sets the wind stop type.
	 *
	 * @param windStopType the windStopType to set
	 */
	protected void setWindStopType(String windStopType) {
		this.windStopType = windStopType;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		if (request==null) return null;
		return request.getSource();
	}

	/**
	 * @return the dataSource
	 */
	public boolean isDataSource() {
		return request.isFileRequest();
	}

	protected String getComment() {
		return comment;
	}

	protected void setComment(String comment) {
		this.comment = comment;
	}

	protected Map<String, Object> parseHeaders() throws ReaderException {
		if (header==null || header.isEmpty()) return Collections.emptyMap();
		Map<String, Object> ret = new HashMap<>();
		for (String line : header) {
			line = line.trim();
			if (line.startsWith("#")) line = line.substring(1);
			String[] sa = null;
			if (line.contains(":")) sa = line.split(":");
			if (line.contains("=")) sa = line.split("=");
			ret.put(sa[0].trim(), sa[1].trim());
		}
		return ret;
	}
}
