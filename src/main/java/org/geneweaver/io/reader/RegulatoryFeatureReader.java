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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.GeneticEntity;
import org.geneweaver.domain.RegulatoryFeature;
import org.geneweaver.io.connector.RegulatoryFeatureOverlapConnector;

// TODO: Auto-generated Javadoc
/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * Reads General Feature Format (GFF) files example:
 * 
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 *
 */
class RegulatoryFeatureReader<N extends GeneticEntity> extends LineIteratorReader<N>{


	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public RegulatoryFeatureReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\t+");
		setAssignmentChar("=");
		setChunkSize(1000); 
		return this;
	}
	
	@Override
	public <U extends Entity> Function<N, Stream<U>> getDefaultConnector() {
		Function<N, Stream<U>> func = new RegulatoryFeatureOverlapConnector<N, U>();
		return func;
	}


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
        
		String[] rec = line.split(getDelimiter());
		GeneticEntity bean = new RegulatoryFeature();
        try {
			BeanMap d = new BeanMap(bean);
			populateStandardColumns(d, rec);
	        
			String attrColumn = rec[8];
	        Map<String,String> attributes = parseAttributes(attrColumn);
	        d.put("featureId", attributes.get("regulatory_feature_stable_id"));
	        d.put("activity", attributes.get("activity"));
	        d.put("boundEnd", Integer.parseInt(attributes.get("bound_end")));
	        d.put("boundStart", Integer.parseInt(attributes.get("bound_start")));
	        d.put("description", attributes.get("description"));
	        d.put("epigenome", attributes.get("epigenome"));
	        d.put("featureType", attributes.get("feature_type"));
	        
	        String fileName = request.getName();
	        if (fileName!=null) {
	        	String[] dedot = fileName.split("\\.");
	        	
	        	// Assuming metadata is in name we can 
	        	// get more information.
	        	
	        	if (dedot.length>=7) {
	        	// 0. species
		        	d.put("species", getFixedSpecies(dedot[0]));
		        	// 1. assembly version,
		        	d.put("assemblyVersion", dedot[1]);
		        	// 2. cell type (if applicable),
		        	d.put("cellType", dedot[2]);
		        	// 3. feature type (if applicable),
		        	//d.put("featureType", dedot[3]);
		        	// 4. analysis name,
		        	d.put("analysisName", dedot[4]);
		        	// 5. results type,
		        	d.put("resultsType", dedot[5]);
		        	// 6. data freeze date and
		        	d.put("date", dedot[6]);
		        	// 7. file format.
		        	//d.put("date", dedot[7]);
	        	}
	        }
	        
        } catch (IllegalArgumentException ne) {
        	throw new ReaderException("The line "+line+" of bean type "+bean.getClass().getSimpleName()+" cannot be parsed ", ne);
        }
        
        return (N)bean;
	}

	private String getFixedSpecies(String species) {
		species = species.replaceAll("_", " ").trim();
		String first = String.valueOf(species.charAt(0)).toLowerCase();
		String rest = species.substring(1);
		return first+rest;
	}

}