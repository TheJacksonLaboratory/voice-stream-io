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
import org.jax.gweaver.domain.NamedEntity;
import org.jax.gweaver.domain.Region;
import org.jax.gweaver.domain.Track;
import org.jax.gweaver.domain.Tracked;
import org.neo4j.ogm.session.Session;

/**
 * 
 * Connects tracks with regions. This connector is *not* thread safe
 * and cannot be used parallel.
 * 
 * @author gerrim
 *
 */
public class TrackConnector implements Connector<NamedEntity, Entity>, Function<NamedEntity, Stream<Entity>> {
	
	
	private Track currentTrack;

	/**
	 * @param session, not required.
	 */
	@Override
	public Stream<Entity> stream(NamedEntity bean, Session session) {
		return apply(bean);
	}

	@Override
	public Stream<Entity> apply(NamedEntity bean) {
		String name = bean.getName();
		if (bean instanceof Track && (name==null || name.isEmpty())) {
			throw new ConnectorException("Tracks must have a name!");
		}
        if (bean instanceof Track) {
        	currentTrack = (Track)bean;
        	return Stream.of(bean);
        	
        } else if (bean instanceof Region) {
        	
        	if (currentTrack!=null) {
        		return Stream.of(bean, new Tracked((Region)bean, currentTrack));
        	} else {
        		return Stream.of(bean);
        	}
        }
        
        throw new ConnectorException(getClass().getSimpleName()+" may not be used with "+bean.getClass().getSimpleName());
	}
}
