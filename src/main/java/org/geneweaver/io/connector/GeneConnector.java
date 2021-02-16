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
package org.geneweaver.io.connector;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Gene;
import org.geneweaver.domain.GeneticEntity;
import org.geneweaver.domain.Produces;
import org.geneweaver.domain.Transcript;
import org.neo4j.ogm.session.Session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A flatMap function designed to return the original bean and its connections, if any.
 * 
 * For example each time a gene is added other entities which link to it will be parsed. 
 * This class attempts to create link objects between entities (for example Transcript and
 * Genes) by returning an additional object in the stream (use flatMap(...) when using a 
 * connector function) to represent the connection. These objects can be used to build up
 * a graph in neo4j or write bulk import files of different classes.
 * 
 * @author gerrim
 *
 */
public class GeneConnector<N extends GeneticEntity, E extends Entity> implements Connector<N, E>, Function<N, Stream<E>> {
	
	/**
	 * We store recently created Genes by id. We look in this pool for the 
	 * Gene corresponding to the current transcript so that we can make a link.
	 * Since the Gene file is quite large, we do not keep all the keys in memory
	 * all the time. Instead we use Guava to create a local temporary cache.
	 * We Cannot just save the last Gene and use it because we want to use multiple
	 * threads to consume lines from the file sometimes.
	 */
	private Cache<String, Gene> recentGenes = createCache();


	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> apply(GeneticEntity bean) {
		String geneId = bean.getGeneId();
		if (geneId==null || geneId.isEmpty()) {
			throw new ConnectorException("Genes and Transcripts must have a geneId!");
		}
        if (bean instanceof Gene) {
        	if (recentGenes.getIfPresent(geneId)!=null) {
        		throw new ConnectorException("The gene id "+geneId+" appears twice!");
        	}
        	Gene gene = (Gene)bean;
        	recentGenes.put(geneId, gene);
        	
        	return (Stream<E>) Stream.of(gene);
        	
        } else if (bean instanceof Transcript) {
        	
        	Gene gene = recentGenes.getIfPresent(geneId);
        	Transcript transcript = (Transcript)bean;
        	if (gene == null) {
            	throw new ConnectorException("The gene id "+geneId+" for Transcript '"+transcript.getTranscriptId()+" has not been found!");
        	} 
        	
        	Produces produces = new Produces(gene, transcript);
        	return (Stream<E>) Stream.of(transcript, produces);
        }
        
        throw new ConnectorException(getClass().getSimpleName()+" may not be used with "+bean.getClass().getSimpleName());
	}

	/**
	 * @param session - not required.
	 */
	@Override
	public Stream<E> stream(GeneticEntity bean, Session session) {
		return apply(bean);
	}
	
	private <T> Cache<String, T> createCache() {
		return CacheBuilder.newBuilder()
					    .maximumSize(1000) 
					    .expireAfterWrite(2, TimeUnit.MINUTES)
					    .build();
	}



}
