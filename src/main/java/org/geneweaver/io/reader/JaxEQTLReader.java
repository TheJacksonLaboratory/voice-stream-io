package org.geneweaver.io.reader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.EQTL;
import org.geneweaver.domain.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the eQTL files for mouse from the work by Hao He to
 * produce these files. The processing of the eQTL rdata files
 * produces an EQTL csv file which we read with this reader in 
 * order to generate eQTL links in the graph.
		
 * @author gerrim
 *
 */
public class JaxEQTLReader<N extends Entity> extends LineIteratorReader<N> {
	
	private static Logger logger = LoggerFactory.getLogger(JaxEQTLReader.class);

	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JaxEQTLReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter(","); // Must be a , only
		return this;
	}

	private List<String> headerNames;
	private Map<String,Object> headerValues;

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

		if (headerNames==null || headerValues==null) {
			parseHeaders();
		}
		
		EQTL bean = new EQTL();
		BeanMap d = new BeanMap(bean);
		
		String[] values = line.split(getDelimiter());
		
		if (values.length!=headerNames.size()) {
			throw new ReaderException("There are a different number of headers and values!");
		}
		
		for(int i=0;i<headerNames.size();i++) {
			String name = headerNames.get(i);
			
			if (name.equals("bpmm10")) continue; // TODO

			// BeanMap autoboxes which is probably slowish and
			// could be speeded up.
			try {
				if (values[i].trim().length()<1) continue;
				d.put(name, values[i]);
			} catch (NumberFormatException ne) {
				logger.info("The property '"+name+"' cannot have value: "+values[i]);
				continue;
			}
		}
		
		headerValues.forEach((k,v)->{
			d.put(k,v);
		});
		
		return (N)bean;
	}
	
	private DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	
	private void parseHeaders() throws ReaderException {
		
		if (header==null || header.isEmpty()) {
			throw new ReaderException("JAX eQTL files must have a header!");
		}
		
		// Header names
		String headLine = header.get(header.size()-1);
		String[] names = headLine.substring(1).split(getDelimiter());
		
		// Something like: marker,chr,bp_mm10,rs_id,gene_id
		headerNames = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			name = name.replace("_", "");
			if (name.equals("rsid")) name = "rsId";
			if (name.equals("geneid")) name = "geneId";
			headerNames.add(name);
		}
		
		// Header values
		headerValues = new HashMap<>();
		// Something like: strain, tissue, ensembl.version, species, url, date
		for (int i = 0; i < header.size()-1; i++) {
			String hline = header.get(i).substring(1);
			String[] kvs = hline.split(":");
			
			String name = kvs[0].toLowerCase();
			Object value = kvs[1].trim();
			
			// Make all eQTLs have same field names, even if from human data or mouse data.
			if (name.equals("ensembl.version")) name = "version";
			if (name.equals("tissue")) {
				name = "tissueName";
				value = value.toString().toLowerCase();
			}
			if (name.equals("species")) continue; // Repeated information
			if (name.equals("url")) name = "source";
			if (name.equals("date")) {
				try {
					value = format.parse(value.toString());
				} catch (ParseException e) {
					throw new ReaderException("Cannot parse date: "+value);
				}
				continue;// We do not repeat date
			}
			headerValues.put(name, value);
		}
	}

	protected void addHeader(String line) {
		headerNames = null;
		headerValues = null;
		super.addHeader(line);
	}

}
