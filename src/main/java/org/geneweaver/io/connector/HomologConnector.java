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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Homolog;
import org.geneweaver.domain.HomologGene;
import org.geneweaver.io.IPrintStream;
import org.neo4j.ogm.session.Session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * It is designed to be used with parallel streams which is why a 
 * cache rather than a reference is used.
 * 
 * PLEASE USE HomologFunction to get the GeneID before using HomologConnector to make the connection between
 * mouse and human genes.
 * 
 * 
HomoloGene ID	Common Organism Name	NCBI Taxon ID	Symbol	EntrezGene ID	Mouse MGI ID	HGNC ID	OMIM Gene ID	Genetic Location	Genomic Coordinates (mouse: , human: )	Nucleotide RefSeq IDs	Protein RefSeq IDs	SWISS_PROT IDs
3	mouse, laboratory	10090	Acadm	11364	MGI:87867			Chr3 78.77 cM	Chr3:153922357-153944632(-)	NM_007382	NP_031408	P45952
3	human	9606	ACADM	34		HGNC:89	OMIM:607008	Chr1 p31.1	Chr1:75724347-75763679(+)	NM_001286043,NM_000016,NM_001127328,NM_001286042,NM_001286044	NP_001120800,NP_001272971,NP_001272972,NP_001272973,NP_000007	P11310
5	mouse, laboratory	10090	Acadvl	11370	MGI:895149			Chr11 42.96 cM	Chr11:70010183-70015411(-)	NM_017366	NP_059062	P50544
5	human	9606	ACADVL	37		HGNC:92	OMIM:609575	Chr17 p13.1	Chr17:7217125-7225267(+)	NM_000018,NM_001033859,NM_001270448,NM_001270447	NP_001029031,NP_001257377,XP_006721579,XP_011522131,XP_011522132,NP_000009,XP_024306509,NP_001257376	P49748
6	mouse, laboratory	10090	Acat1	110446	MGI:87870			Chr9 29.12 cM	Chr9:53580522-53610350(-)	NM_144784	NP_659033	Q8QZT1
6	human	9606	ACAT1	38		HGNC:93	OMIM:607809	Chr11 q22.3	Chr11:108121531-108148168(+)	NM_001386689,NM_001386688,NM_001386687,NM_001386686,NM_001386691,NM_001386678,NM_000019,NM_001386677,NM_001386685,NM_001386682,NM_001386679,NM_001386681,NM_001386690	NP_001373615,NP_001373617,NP_001373618,NP_001373619,NP_001373620,XP_016873171,XP_024304282,NP_001373607,NP_001373606,NP_000010,NP_001373608,NP_001373610,NP_001373611,NP_001373614,NP_001373616	P24752
7	mouse, laboratory	10090	Acvr1	11477	MGI:87911			Chr2 33.05 cM	Chr2:58446438-58566828(-)	NM_001355049,NM_001110205,NM_001355048,NM_007394,NM_001110204,XM_006497622	NP_001341978,NP_001103675,NP_001103674,NP_001341977,NP_031420,XP_006497685	P37172
7	human	9606	ACVR1	90		HGNC:171	OMIM:102576	Chr2 q24.1	Chr2:157736446-157875880(-)	NM_001347667,NM_001347666,NM_001347665,NM_001347664,NM_001347663,NM_001111067,NM_001105	NP_001334594,NP_001334595,NP_001334596,XP_006712888,XP_011510410,NP_001096,NP_001104537,NP_001334592,NP_001334593	Q04771
9	mouse, laboratory	10090	Sgca	20391	MGI:894698			Chr11 59.01 cM	Chr11:94962791-94976327(-)	XM_011248836,NM_009161	NP_033187,XP_011247138	P82350
9	human	9606	SGCA	6442		HGNC:10805	OMIM:600119	Chr17 q21.33	Chr17:50165517-50175932(+)	NM_001135697,NM_000023	XP_011523422,NP_001129169,NP_000014,XP_024306641,XP_011523426,XP_011523425,XP_011523424,XP_011523423	Q16586
12	mouse, laboratory	10090	Adsl	11564	MGI:103202			Chr15 37.95 cM	Chr15:80948490-80970946(+)	NM_009634	NP_033764	P54822
12	human	9606	ADSL	158		HGNC:291	OMIM:608222	Chr22 q13.1	Chr22:40346500-40387408(+)	NM_000026,NM_001363840,NM_001317923,NM_001123378	XP_016884128,XP_024307934,XP_016884129,XP_016884127,XP_016884126,XP_016884125,XP_011528282,XP_011528279,NP_001350769,NP_001304852,NP_001116850,NP_000017	P30566

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
	public Stream<E> stream(N entity, Session unused, IPrintStream log) {

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
					Homolog hom = new Homolog(hg.getHid(), hg.getGeneId(), hgene.getGeneId());
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
