package org.geneweaver.io.reader;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
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
class JaxEQTLReader<N extends Entity> extends LineIteratorReader<N> {
	
	private static Logger logger = LoggerFactory.getLogger(JaxEQTLReader.class);
	private static UberonService uberonService = new UberonService();

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
		bean.setTissueFileName(request.name());
		//bean.setStudyId(createFudgedStudyId(request.name()));
		BeanMap d = new BeanMap(bean);
		
		String[] values = line.split(getDelimiter());
		
		if (values.length!=headerNames.size()) {
			throw new ReaderException("There are a different number of headers and values!");
		}
		
		for(int i=0;i<headerNames.size();i++) {
			String name = headerNames.get(i);
			
			// BeanMap autoboxes which is probably slowish and
			// could be speeded up.
			Object value = values[i];
			if (value==null) continue;
			value = values[i].trim();
			
			Class<?> type = d.getType(name);
			if ("NA".equalsIgnoreCase(value.toString()) || "null".equalsIgnoreCase(value.toString())) {
				if (Number.class.isAssignableFrom(type)) {
					value = "0";
				}
			}
			if (Integer.class.equals(type)) {
				value = Double.valueOf(value.toString()).intValue();
			} else if (Double.class.equals(type)) {
				value = Double.valueOf(value.toString());
			}
			
			try {
				if (value.toString().length()<1) continue;
				d.put(name, value);
			} catch (NumberFormatException ne) {
				logger.info("The property '"+name+"' cannot have value: "+values[i]);
				continue;
			} catch (IllegalArgumentException ie) {
				throw new ReaderException("Field "+name+" has type "+type+" which has not been parsed from "+value);
			}
		}
		
		headerValues.forEach((k,v)->{
			d.put(k,v);
		});
		
		return (N)bean;
	}
	
	/*
	      "Aging_Bone_DO.Rds",	"https://churchilllab.jax.org/qtlviewer/DO/bone/dl?fileName=dataset.mrna.DO_bone.v2.Rds",
		  "Aging_Heart_DO.Rds", "https://churchilllab.jax.org/qtlviewer/JAC/DOHeart/dl?fileName=dataset.mrna.JAC_DO_heart.v10.Rds",
		  "Aging_Kidney_DO.Rds", "https://churchilllab.jax.org/qtlviewer/JAC/DOKidney/dl?fileName=dataset.mrna.JAC_DO_kidney.v6.Rds",
		  "Skelly_mESC_DO.Rds", "https://churchilllab.jax.org/qtlviewer/DO_mESC/dl?fileName=dataset.mrna.DO_mESC.v3.Rds",
		  "Svenson_HFD_DO.Rds", "https://churchilllab.jax.org/qtlviewer/svenson/DOHFD/dl?fileName=dataset.mrna.Svenson_DO_HFD.v12.Rds",
		  "Chesler_Striatum_DO.Rds",	"https://churchilllab.jax.org/qtlviewer/DO/DrugNaiveStriatum/dl?fileName=dataset.CSNA_DO_Striatum.v3.Rds",
		  "Hippocampus_DO.Rds",	"https://churchilllab.jax.org/qtlviewer/DO/hippocampus/dl?fileName=dataset.DO_Hippocampus.v2.Rds",
		  "AttieIsletSecretion_v13_DO.Rdata","http://bhchurchilllab01.jax.org:18005/dl?fileName=AttieIsletSecretion_v13.RData",
		  "Adipose.RDS",	"http://bhchurchilllab01.jax.org:18045/dl?fileName=Adipose.RDS",
		  "Heart.RDS",	"http://bhchurchilllab01.jax.org:18045/dl?fileName=Heart.RDS",
		  "Islet.RDS",	"http://bhchurchilllab01.jax.org:18045/dl?fileName=Islet.RDS",
		  "Liver.RDS",	"http://bhchurchilllab01.jax.org:18045/dl?fileName=Liver.RDS",
		  "SkeletalMuscle.RDS",	"http://bhchurchilllab01.jax.org:18045/dl?fileName=SkeletalMuscle.RDS");
	 */
	
	private static Map<String,String> studyIdMap = createStudyIdMap();
	private static Map<String,String> createStudyIdMap() {
		Map<String,String> ret = new HashMap<>();
		ret.put("Aging_Bone_DO.csv.gz".toLowerCase(),"Project999901");
		ret.put("Aging_Heart_DO.csv.gz".toLowerCase(),"Project999902");
		ret.put("Aging_Kidney_DO.csv.gz".toLowerCase(),"Project999903");
		ret.put("Chesler_Hippocampus_DO.csv.gz".toLowerCase(),"Chesler999901");
		ret.put("Chesler_Striatum_DO.csv.gz".toLowerCase(),"Chesler999902");
		ret.put("DO.Cube.Adipose.csv.gz".toLowerCase(),"Cube999901");
		ret.put("DO.Cube.Heart.csv.gz".toLowerCase(),"Cube999902");
		ret.put("DO.Cube.Islet.csv.gz".toLowerCase(),"Cube999903");
		ret.put("DO.Cube.Liver.csv.gz".toLowerCase(),"Cube999904");
		ret.put("DO.Cube.SkeletalMuscle.csv.gz".toLowerCase(),"Cube999905");
		ret.put("Skelly_mESC_DO.csv.gz".toLowerCase(),"Skelly999901");
		ret.put("Svenson_HFD_DO.csv.gz".toLowerCase(),"Svenson999901");
		return ret;
	}
		
	private String createFudgedStudyId(String name) throws ReaderException {
		if (name == null) return null;
		try {
			// We generate a fake project StudyId.
			// Project1234 would be a private id
			// Chelser7 would be a public id
			if (studyIdMap.containsKey(name.toLowerCase())) return studyIdMap.get(name.toLowerCase());
			return "Project:"+Base64.getEncoder().encodeToString(name.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new ReaderException(e);
		}
	}

	private DateFormat format1 = new SimpleDateFormat("MM/dd/yyyy");
	private DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	protected Map<String,Object> parseHeaders() throws ReaderException {
		
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
			if (name.equals("bpmm10")) name = "bp";
			if (name.equals("bpm39"))  name = "bp";
			if (name.equals("strain")) name = "population";
			// TODO lod
			headerNames.add(name);
		}
		
		// Header values
		headerValues = new HashMap<>();
		// Something like: strain, tissue, ensembl.version, species, url, date
		for (int i = 0; i < header.size()-1; i++) {
			String hline = header.get(i).substring(1).trim();
			String[] kvs = hline.split(":");
			if (kvs.length!=2) {
				logger.debug("Ignored invalid header line: "+hline);
				continue;
			}
			
			String name = kvs[0].toLowerCase();
			
			// Keys with spaces are other comments in the eQTL header.
			if (name.indexOf(' ')>0) continue; 
			
			Object value = kvs[1].trim();
			
			// Make all eQTLs have same field names, even if from human data or mouse data.
			if (name.equals("strain")) name = "population";
			if (name.equals("ensembl.version")) name = "version";
			if (name.equals("tissue")) {
				name = "tissueName";
				value = value.toString().toLowerCase();
			}
			if (name.equals("species")) continue; // Repeated information
			if (name.equals("url")) name = "source";
			if (name.equals("date")) {
				try {
					value = format1.parse(value.toString());
				} catch (ParseException e) {
					try {
						value = format2.parse(value.toString());
					} catch (ParseException eOther) {
						throw new ReaderException("Cannot parse date: "+value);
					}
				}
				continue;// We do not repeat date
			}
			headerValues.put(name, value);
		}
		
		// If the headerValues contains a tissueName, we can set the uberon.
		if (headerValues.containsKey("tissueName")) {
            String tName = headerValues.get("tissueName").toString();
            headerValues.put("uberon", uberonService.getUberonCode(tName));
		}
		
		return headerValues;
	}

	protected void addHeader(String line) {
		headerNames = null;
		headerValues = null;
		super.addHeader(line);
	}

}
