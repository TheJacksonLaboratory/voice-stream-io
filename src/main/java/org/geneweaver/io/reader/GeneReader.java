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
import org.geneweaver.domain.Gene;
import org.geneweaver.domain.GeneticEntity;
import org.geneweaver.domain.Transcript;
import org.geneweaver.io.connector.GeneConnector;

// TODO: Auto-generated Javadoc
/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 *
 */
class GeneReader<N extends GeneticEntity> extends LineIteratorReader<N>{


	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@Override
	public GeneReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\t+"); // Must be a tab only
		setWindStopType("gene");
		setChunkSize(10000); // We need quite a few lines because active objects in the data are sparse.
		return this;
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
		GeneticEntity bean = null;
        String type = rec[2].trim();
        if ("gene".equalsIgnoreCase(type)) {
        	bean = new Gene();
        } else if ("transcript".equalsIgnoreCase(type)) {
        	bean = new Transcript();
        } else if ("exon".equalsIgnoreCase(type)) {
        	return null; // TODO
        } else {
        	// TODO Should we throw exceptions or ignore these cases. Examples cds, start_codon
        	return null;
        }
		
        try {
			BeanMap d = new BeanMap(bean);
			populate(d, rec);
	        
	        d.put("phase", rec[7]);
	        
	        Map<String,String> attributes = parseAttributes(rec[8]);
	        String geneId = attributes.get("gene_id").toString().split(":")[0];
	        d.put("geneId", geneId);
	        d.put("geneName", attributes.get("gene_name"));
	        d.put("geneVersion", attributes.get("gene_version"));
	        d.put("geneBiotype", attributes.get("gene_biotype"));
 	        transfer("transcript_id", attributes, "transcriptId", d);
	        transfer("transcript_biotype", attributes, "transcriptBiotype", d);
	        transfer("transcript_name", attributes, "transcriptName", d);
	        
        } catch (IllegalArgumentException ne) {
        	throw new ReaderException("The line "+line+" of bean type "+bean.getClass().getSimpleName()+" cannot be parsed ", ne);
        }
        
        return (N)bean;
	}

	@Override
	public <U extends Entity> Function<N, Stream<U>> getDefaultConnector() {
		return new GeneConnector<>();
	}

}