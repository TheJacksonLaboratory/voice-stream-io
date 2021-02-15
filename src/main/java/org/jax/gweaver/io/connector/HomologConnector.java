package org.jax.gweaver.io.connector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.Homolog;
import org.jax.gweaver.domain.HomologGene;
import org.neo4j.ogm.session.Session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * It is designed to be used with parallel streams which is why a 
 * cache rather than a reference is used.
 * 
 * @author gerrim
 *
 * @param <N>
 * @param <E>
 */
public class HomologConnector<N extends Entity, E extends Entity> implements Connector<N, E>, Function<N, Stream<E>>  {

	
	/**
	 * We store recently created Genes by id. We look in this pool for the 
	 * Gene corresponding to the current transcript so that we can make a link.
	 * Since the Gene file is quite large, we do not keep all the keys in memory
	 * all the time. Instead we use Guava to create a local temporary cache.
	 * We Cannot just save the last Gene and use it because we want to use multiple
	 * threads to consume lines from the file sometimes.
	 */
	private Cache<Long, Collection<HomologGene>> mouseCache = createCache();

	@SuppressWarnings("unchecked")
	@Override
	public Stream<E> stream(N entity, Session unused) {

		HomologGene hgene = (HomologGene)entity;
		if (hgene.getOrganismName().toLowerCase().startsWith("mouse")) {
			
			Collection<HomologGene> mhols = mouseCache.getIfPresent(hgene.getHid());
			if (mhols==null) {
				mhols = new LinkedList<>();
				mouseCache.put(hgene.getHid(), mhols);
			}
			
			mhols.add(hgene);
			return (Stream<E>)Stream.of(hgene);
			
		} else {
			
			Long hid = hgene.getHid();
			Collection<HomologGene> mouse = mouseCache.getIfPresent(hid);
			if (mouse !=null) {
				
				Collection<E> ret = new LinkedList<>();
				for (HomologGene hg : mouse) {
					Homolog hom = new Homolog(hg.getHid(), hg.getTaxonId(), hg.getSymbol(), hgene.getTaxonId(), hgene.getSymbol());
					hom.setSource(hgene.getSource());
					ret.add((E)hom);
				}
				ret.add((E)hgene);
				return ret.stream();
			} else {
				return (Stream<E>)Stream.of(hgene);
			}
		}
		
	}

	
	private <T> Cache<Long, T> createCache() {
		return CacheBuilder.newBuilder()
					    .maximumSize(1000) 
					    .expireAfterWrite(1, TimeUnit.MINUTES)
					    .build();
	}


	@Override
	public Stream<E> apply(N t) {
		return stream(t);
	}

	

}
