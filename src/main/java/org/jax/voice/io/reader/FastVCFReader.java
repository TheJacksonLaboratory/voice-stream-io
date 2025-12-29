package org.jax.voice.io.reader;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanMap;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.VariantCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VCF files read as a stream exist here:
 *      <dependency>
		    <groupId>com.github.samtools</groupId>
		    <artifactId>htsjdk</artifactId>
		    <version>2.24.1</version>
		</dependency>

  However it is quite a large dependency to make on a small package like this one.
  More importantly this simple reader basically is designed to do just what we need when biulding 
  the geneweaver graph. This means that long lines of individual information are not parsed 
  or split meaning the stream processing this file can go *fast*
		
 * @author gerrim
 *
 */
public class FastVCFReader<N extends Entity> extends LineIteratorReader<N> {
	
	private static Logger logger = LoggerFactory.getLogger(FastVCFReader.class);

	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public FastVCFReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\t"); // Must be a tab only
		return this;
	}

	private List<String> headerNames;

	/**
	 * Creates the.
	 *
	 * @param line the line
	 * @return the n
	 * @throws ReaderException the reader exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {

		if (headerNames==null) {
			if (header==null || header.isEmpty()) {
				throw new ReaderException("VCF files must have a header!");
			}
			String headLine = header.get(header.size()-1);
			String[] names = headLine.substring(1).split(getDelimiter());
			
			// Something like: CHROM, POS, ID, REF, ALT, QUAL, FILTER, INFO, FORMAT, HG00096, ... 
			headerNames = Arrays.asList(names);
		}
		VariantCall bean = new VariantCall();
		BeanMap d = new BeanMap(bean);
		
		// Splitting these long lines is slow and we do not need the 
		// individual values, therefore we do not split instead we
		// substring the line for each delimiter	
		String sline = line;
		for(int i=0;i<headerNames.size();i++) {
			String name = headerNames.get(i).toLowerCase();
			
			int loc = sline.indexOf(getDelimiter());
			if (loc<0) continue;
			Object value = sline.substring(0, loc);
			
			if (headerNames.get(i).equals("INFO")) {
				value = parseAttributes(value.toString());
			}
			
			sline = sline.substring(loc+1);
			
			// BeanMap autoboxes which is probably slowish and
			// could be speeded up.
			try {
				d.put(name, value);
			} catch (NumberFormatException ne) {
				logger.info("The property '"+name+"' cannot have value: "+value);
				continue;
			}
			
			// For speed reasons, we ignore everything after
			// format because the rest of the long line is not
			// needed when we extract the core parts of the file.
			if (name.equalsIgnoreCase("FORMAT")) break;
		}
		
		return (N)bean;
	}
	
	protected void addHeader(String line) {
		headerNames = null;
		super.addHeader(line);
	}

}
