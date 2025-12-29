package org.jax.voice.io.reader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.beanutils.BeanMap;
import org.jax.voice.domain.EQTL;
import org.jax.voice.domain.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the eQTL files in as flexible way as possible.
 * For instance files from  https://www.biorxiv.org/content/10.1101/655670v1
 * And these files: https://zenodo.org/record/3408356#.YQljwlNKii6
 * @author gerrim
 *
 */
public class FlexEQTLReader<N extends Entity> extends LineIteratorReader<N> {
	
	private static Logger logger = LoggerFactory.getLogger(FlexEQTLReader.class);

	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public FlexEQTLReader<N> init(ReaderRequest request) throws ReaderException {
		setDelimiter(request.getDelimiter()==null?"\\t":request.getDelimiter()); 
		super.setup(request);
		return this;
	}

	private List<String> columnNames;
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

		if (line==null || line.isBlank()) return null;
		
		if (columnNames==null || headerValues==null) {
			boolean lineIsHeader = parseHeaders(line);
			if (lineIsHeader) return null;
		}
		
		EQTL bean = new EQTL();
		bean.setSpecies(getSpecies());
		BeanMap d = new BeanMap(bean);
		
		String[] values = line.split(getDelimiter());
		
		if (values.length!=columnNames.size()) {
			throw new ReaderException("There are a different number of headers and values!");
		}
		
		for(int i=0;i<columnNames.size();i++) {
			
			String name = columnNames.get(i);
			if (!d.containsKey(name)) continue;
			
			// BeanMap autoboxes which is probably slowish and
			// could be speeded up.
			try {
				String value = values[i];
				if (value==null) continue;
				value = value.trim();
				if (value.isEmpty()) continue;
				
				// Split the . from the id and record the original in 'fullGeneId'
				if ("geneId".equals(name) && value.indexOf(".")>-1) {
					String geneId = value.substring(0, value.indexOf("."));
					d.put("fullGeneId", value);
					value = geneId;
				}
				
				d.put(name, value);
			} catch (NumberFormatException ne) {
				logger.info("The property '"+name+"' cannot have value: "+values[i]);
				continue;
			}
		}
		
		headerValues.forEach((k,v)->{
			d.put(k,v);
		});
		
		// No header should be numeric
		for (String header : columnNames) {
			try {
				Double.parseDouble(header);
				throw new IllegalArgumentException("No header name should be numeric!");
			} catch (NumberFormatException required) {
				continue;
			}
		}
		
		return (N)bean;
	}
	
	private DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	
	private boolean parseHeaders(String line) throws ReaderException {
		
		boolean lineIsHeader = false;
		// They might have header lines with # at the start
		// or they might have a #-less line of headers.
		if (header==null || header.isEmpty()) {
			// If the line is non-null we attempt to get headers from it.
			if (line!=null) {
				addHeader(line);
				lineIsHeader = true;
			}
		}
		
		// Header names
		String headLine = header.get(header.size()-1);
		String[] names = headLine.trim().startsWith(getComment()) 
				       ? headLine.substring(1).split(getDelimiter())
				       : headLine.split(getDelimiter());
		
		// Something like: marker,chr,bp_mm10,rs_id,gene_id
		// Or GeneName	Strand	GencodeLevel	GeneType	GeneID	ChrPheno	StartPheno	EndPheno	BestExonID	NumExons	NumVariantCis	DistanceWithBest	SNPid
		columnNames = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			name = name.replace("_", "");
			name = name.toLowerCase();
			
			// Map between header names. The header will
			// be the field in the eQTL object.
			if (name.equals("rsid")) name = "rsId";
			if (name.equals("snpid")) name = "rsId";
			if (name.equals("geneid")) name = "geneId";
			if (name.equals("snpchr")) name = "chr";			
			if (name.equals("chrsnp")) name = "chr";			
			columnNames.add(name);
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
			if (name.equalsIgnoreCase("ensembl.version")) name = "version";
			if (name.equalsIgnoreCase("tissue")) {
				name = "tissueName";
				value = value.toString().toLowerCase();
			}
			if (name.equalsIgnoreCase("species")) continue; // Repeated information
			if (name.equalsIgnoreCase("url")) name = "source";
			if (name.equalsIgnoreCase("date")) {
				try {
					value = format.parse(value.toString());
				} catch (ParseException e) {
					throw new ReaderException("Cannot parse date: "+value);
				}
				continue;// We do not repeat date
			}
			headerValues.put(name, value);
		}
		
		// Some Header values can come from the file name for some formats (yipee!!!)
		Matcher matcher = request.getMatcher();
		if (matcher!=null) {
			// First match is tissue
			String name = "tissueName";
			String value = matcher.group(1);
			headerValues.put(name, value);
		}
		
		if (!headerValues.containsKey("source") && request.getSource()!=null) {
			headerValues.put("source", request.getSource());
		}
		return lineIsHeader;
	}

	protected void addHeader(String line) {
		columnNames = null;
		headerValues = null;
		super.addHeader(line);
	}

}
