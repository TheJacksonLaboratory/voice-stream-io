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
package org.jax.voice.io.connector;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jax.voice.domain.Entity;
import org.jax.voice.domain.GeneticEntity;
import org.jax.voice.domain.Transcript;
import org.jax.voice.domain.Variant;
import org.jax.voice.domain.VariantEffect;
import org.jax.voice.io.IPrintStream;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.session.Session;

/**
 * A connector function which makes sure that variant effects linking to variants are extracts as the file 
 * is parsed. This is desirable because it makes parsing a large varient file or files fast.
 * 
 * @author gerrim
 * @param <N> type of entity in the file
 * @param <E> type of entity after mapping using the connector.
 */
public class VariantConnector<N extends GeneticEntity, E extends Entity> implements Connector<N, E>, Function<N, Stream<E>>  {

	/**
	 * Soft reference cache to reduce memory leaks. Garbage collector will nullify them as needed.
	 */
	private Map<String, SoftReference<Transcript>> cache = new HashMap<>();
	private boolean useSessions;

	/**
	 * Connect variants to effects without looking up things in an active Neo4j session
	 */
	public VariantConnector() {
		this(false);
	}

	/**
	 * Connect variants to effects, looking up things in an active Neo4j session if useSessions is true.
	 * @param useSessions - Use with caution as a session is required and there may be more than one
	 * session if the objects are being parsed in parallel. If you do not know how to use this param,
	 * leave it as false.
	 */
	public VariantConnector(boolean useSessions) {
		this.useSessions = useSessions;
	}


	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> apply(GeneticEntity ge) {

		Variant v = (Variant)ge;
		try {
			if (v.getVariantEffect()==null || v.getVariantEffect().isEmpty()) {
				return (Stream<E>) Stream.of(v);
			}
			Collection<VariantEffect> ve = v.getVariantEffect()
					.stream()
					.filter(e->e!=null)
					.filter(e->e.getFeatureId()!=null)
					.filter(e->!e.getFeatureId().trim().isBlank())
					.map(e->{e.setVariant(v); return e;})
					.collect(Collectors.toSet());

			Collection<Entity> ret = new LinkedList<>();
			ret.add(v);
			ret.addAll(ve);
			return (Stream<E>) ret.stream();

		} finally {
			// We never actually save the relationships inside the Variant.
			v.clearEffects();
		}
	}

	/**
	 * @param session - not required if useSessions is false.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> stream(N ge, Session session, IPrintStream log) {

		if (!useSessions) {
			return apply(ge);
			
		} else {
			Variant v = (Variant)ge;
			try {
				if (v.getVariantEffect()==null || v.getVariantEffect().isEmpty()) {
					return (Stream<E>) Stream.of(v);
				}
				
				Set<String> transIds = v.getVariantEffect().stream()
						.filter(e->e!=null)
						.filter(e->e.getFeatureId()!=null)
						.filter(e->!e.getFeatureId().trim().isBlank())
						.map(e->e.getFeatureId())
						.collect(Collectors.toSet());

				if (transIds.isEmpty()) {
					return (Stream<E>) Stream.of(v); // It gets cleared on finally.
				}

				Map<String, Transcript> allTranscripts = getCachedFilters(transIds, session);
				Collection<VariantEffect> ve = v.getVariantEffect()
						.stream()
						.map(e->registerTranscript(v, e, allTranscripts))
						.filter(t->t!=null)
						.collect(Collectors.toSet());

				Collection<Entity> ret = new LinkedList<>();
				ret.add(v);
				ret.addAll(ve);
				return (Stream<E>) ret.stream();

			} finally {
				// We never actually save the relationships inside the Variant.
				v.clearEffects();

			}
		}
	}


	/**
	 * The logic of this is a little hard to understand. The following points help:
	 * 1. We do not want to do more filters than we have to, they are slow.
	 * 2. If the cache filters, we do not want to cache so many that we use all the memory
	 * 3. If a Transcript is not there, we want to save this as a null in our cache to
	 * save doing many negative filters.
	 * 
	 * @param transIds
	 * @param session
	 * @return
	 */
	private Map<String, Transcript> getCachedFilters(Set<String> transIds, Session session) {

		// This seems clumsy and non-functional but the Filter object is limited.
		Map<String, Transcript> allTranscripts = new HashMap<>();
		Filters filters = new Filters();

		// 1. Travese the ids and either get the cached Transcript
		// or find out it has been purposely set to null or should
		// result in a new filter.
		Iterator<String> it = transIds.iterator();
		while(it.hasNext()) {
			// Get it from cache
			String tid = it.next();
			KEY_TEST: if (cache.containsKey(tid)) {
				SoftReference<Transcript> ref = cache.get(tid);
				if (ref!=null) { // If its null it is marked as not having a mapping.
					Transcript t = ref.get();
					if (t != null) {
						allTranscripts.put(tid, t);
						continue; // We cached it!
					} else {
						cache.remove(tid); // Will need to read it as a filter again.
						break KEY_TEST; // Add a filter, the cache value got garbage collected.
					}
				} else {
					continue; // If set to explicitly null, do not filter it.
				}
			}

			// If not get it from filter.
			filters = filters.or(new Filter("transcriptId", ComparisonOperator.EQUALS, tid));
		}

		// Try not to do filtering unless we have to, it's slow.
		if (!filters.isEmpty()) {
			Collection<Transcript> transcripts = session.loadAll(Transcript.class, filters);
			Map<String, Transcript> tmap = transcripts.stream().collect(Collectors.toMap(t->t.getTranscriptId(), t->t));

			// Add the filtered Transcripts to the cache with a soft reference.
			it = tmap.keySet().iterator();
			while(it.hasNext()) {
				String tid = it.next();
				cache.put(tid, new SoftReference<>(tmap.get(tid)));
			}
			allTranscripts.putAll(tmap);
		}

		// We put all the nulls in the cache
		// Anything we did not find is a permanent null
		transIds.removeAll(allTranscripts.keySet());
		for (String tid : transIds) {
			cache.put(tid, null); // Set to null and stays null
		}

		return allTranscripts;
	}

	private VariantEffect registerTranscript(Variant v, VariantEffect e, Map<String, Transcript> tmap) {
		Transcript t = tmap.get(e.getFeatureId());
		if (t==null) return null; // We cannot link this one, no relationship will be made.
		t.setSpecies(v.getSpecies());
		e.setTranscript(t);
		e.setVariant(v);
		e.setSpecies(v.getSpecies());
		return e;
	}

}