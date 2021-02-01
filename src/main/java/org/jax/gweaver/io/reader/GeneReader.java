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
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanMap;
import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.Gene;
import org.jax.gweaver.domain.GeneticEntity;
import org.jax.gweaver.domain.Transcript;
import org.jax.gweaver.io.connector.GeneConnector;

// TODO: Auto-generated Javadoc
/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 *
 */
public class GeneReader<N extends GeneticEntity> extends AbstractReader<N>{

	/**
	 * Instantiates a new gene reader.
	 *
	 * @param species the species
	 */
	// Used in RepeatedLineReader, do not delete.
	protected GeneReader(String species) {
		super(species);
		init();
	}
	
	/**
	 * Instantiates a new gene reader.
	 *
	 * @param species the species
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GeneReader(String species, File file) throws IOException {
		super(species, file); // Genes are not that dense maybe one gene / 10 lines
		init();
	}
	
	/**
	 * Instantiates a new gene reader.
	 *
	 * @param species the species
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GeneReader(String species, InputStream in) throws IOException {
		super(species, in); // Genes are not that dense maybe one gene / 10 lines
		init();
	}

	/**
	 * Inits the.
	 */
	private void init() {
		setWindStopType("gene");
		setChunkSize(10000); // We need quite a few lines because active objects in the data are sparse.
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