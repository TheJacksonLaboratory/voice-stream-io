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
 */
package org.jax.gweaver.io.connector;

import java.util.function.Function;
import java.util.stream.Stream;

import org.jax.gweaver.domain.Entity;
import org.neo4j.ogm.session.Session;

/**
 * A function which runs to expand entities as they are being read.
 * Examples of doing this:
 * 1. Create a relationship between Transcript and Gene.
 * 2. Create a VariantEffect back to an existing transcript.
 * 3. 
 * 
 * @author gerrim
 * @param <T> Output of connection e.g. Produces or VariantEffect
 * @param<I> Input e.g. Transcript, Gene, Variant
 * 
 * Function<? super T, ? extends Stream<? extends R>>
 */
public interface Connector<I extends Entity, T extends Entity> extends Function<I, Stream<T>>  {

	/**
	 * Simply passes to stream method by default.
	 */
	default Stream<T> apply(I entity) {
		return stream(entity, null);
	}

	
	/**
	 * Create a stream from the entity. Note that if threads and
	 * multiple sessions are used the session should not be cached
	 * between calls of this method, or anything.
	 * @param entity From which we will create a stream of connections
	 * @param session The current session, NOTE multiple sessions may be active.
	 * @return Stream of connections including the original entity.
	 */
	default Stream<T> stream(I entity) {
		return stream(entity, null);
	}

	/**
	 * Create a stream from the entity. Note that if threads and
	 * multiple sessions are used the session should not be cached
	 * between calls of this method, or anything.
	 * @param entity From which we will create a stream of connections
	 * @param session The current session, NOTE multiple sessions may be active.
	 * @return Stream of connections including the original entity.
	 */
	Stream<T> stream(I entity, Session session);
	
}
