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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Homolog;
import org.geneweaver.domain.HomologGene;
import org.geneweaver.domain.NamedEntity;
import org.junit.Ignore;
import org.junit.Test;

public class HomologGeneReaderTest extends AbstractDataFileTest {

	
	@Test
	public void chunkSize() throws Exception {
		
		HomologGeneReader<NamedEntity> reader = new HomologGeneReader<>();
		reader.init(new ReaderRequest(getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		assertEquals(4096, reader.getChunkSize());
	}
	
	@Test
	public void chunkSizeFromFactory() throws Exception {
		
		LineIteratorReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		assertEquals(4096, reader.getChunkSize());
	}
	
	@Ignore
	@Test
	public void checkUrl() throws Exception {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(new URL("http://www.informatics.jax.org/downloads/reports/HOM_MouseHumanSequence.rpt")));
		assertEquals(43117, reader.stream().count());
	}

	
	@Ignore
	@Test
	public void checkUrlAsPath() throws Exception {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Test", Paths.get("http://www.informatics.jax.org/downloads/reports/HOM_MouseHumanSequence.rpt")));
		assertEquals(43117, reader.stream().count());
	}

	@Test
	public void count1() throws Exception {
		
		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest(getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		assertEquals(43123, reader.stream().count());
		assertEquals(20600, reader.stream().filter(h->h.getOrganismName().toLowerCase().startsWith("mouse")).count());
		assertEquals(22523, reader.stream().filter(h->h.getOrganismName().toLowerCase().startsWith("human")).count());
	}

	@Test
	public void findHomologs() throws Exception {
		
		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest(getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		List<Entity> homols = reader.stream()
									.flatMap(h->connector.apply(h))
									.filter(e->e instanceof Homolog)
									.collect(Collectors.toList());
		assertEquals(22522, homols.size());
	}

	@Test
	public void checkValues() throws Exception {
		
		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest("Homologene", getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Map<Long,List<HomologGene>> mhomols = new HashMap<>();
		Map<Long,List<HomologGene>> hhomols = new HashMap<>();
		reader.stream().limit(1000).forEach(h->{
			
			if (h.getOrganismName().toLowerCase().startsWith("mouse")) {
				List<HomologGene> mc = mhomols.get(h.getHid());
				if (mc==null) {
					mc = new LinkedList<>();
					mhomols.put(h.getHid(), mc);
				}
				mc.add(h);
				
			} else if (h.getOrganismName().toLowerCase().startsWith("human")) {
				List<HomologGene> hc = hhomols.get(h.getHid());
				if (hc==null) {
					hc = new LinkedList<>();
					hhomols.put(h.getHid(), hc);
				}
				hc.add(h);
			}			
		});
		
		//38711027	mouse, laboratory	10090	Mapk8	26419	MGI:1346861			Chr14 20.22 cM	Chr14:33099855-33169115(-)	NM_001310452,XM_036158602,NM_016700,NM_001310454,XM_030247820,XM_006519032,NM_001310453,XM_030247821	NP_001297383,XP_030103681,XP_030103680,XP_036014495,XP_006519095,NP_057909,NP_001297382,NP_001297381	Q91Y86
		//38711027	human	9606	MAPK8	5599		HGNC:6881	OMIM:601158	Chr10 q11.22	Chr10:48306639-48439360(+)	NM_139049,NM_001323321,NM_001323320,NM_001323302,NM_001278548,NM_001323322,NM_001323323,NM_001323324,NM_001323325,NM_001323326,NM_001278547,NM_001323327,NM_001323328,NM_001323329,NM_001323330,NM_001323331,NM_002750,NM_139046	NP_001310231,NP_001310249,NP_001310250,NP_001310251,NP_001310256,NP_001310257,NP_001310258,NP_001310259,NP_001310260,NP_620634,NP_001310253,NP_001310254,NP_620637,XP_024303847,XP_024303848,NP_001265476,NP_001265477,NP_001310252,NP_001310255	P45983
		equals(mhomols.get(38711027L).iterator().next(), 38711027L, "mouse, laboratory", 10090L, "Mapk8");
		equals(hhomols.get(38711027L).iterator().next(), 38711027L, "human", 9606L, "MAPK8");
		notEquals(hhomols.get(38711028L).iterator().next(), 38711027L, "humany", 10090L, "ACADM");
		
		//38711047	mouse, laboratory	10090	Pou5f1	18999	MGI:101893			Chr17 18.69 cM	Chr17:35816929-35821674(+)	NM_001252452,NM_013633	NP_001239381,NP_038661	P20263
		//38711047	human	9606	POU5F1	5460		HGNC:9221	OMIM:164177	Chr6 p21.33	Chr6:31164337-31170693(-)	NM_002701,NM_001285986,NM_001173531,NM_001285987,NM_203289	NP_001272915,NP_001272916,NP_002692,NP_976034,NP_001167002	Q01860
		//38711047	human	9606	POU5F1B	5462		HGNC:9223	OMIM:615739	Chr8 q24.21	Chr8:127415612-127417210(+)	NM_001159542	NP_001153014	Q06416
		equals(mhomols.get(38711047L).iterator().next(), 38711047L, "mouse, laboratory", 10090L, "Pou5f1");
		equals(hhomols.get(38711047L).get(0), 38711047L, "human", 9606L, "POU5F1");
		equals(hhomols.get(38711047L).get(1), 38711047L, "human", 9606L, "POU5F1B");
		notEquals(hhomols.get(38711048L).iterator().next(), 38711047L, "humany", 10090L, "POU5F1");
		
		//38711143	mouse, laboratory	10090	Olfr1251	259145	MGI:3031085			Chr2 50.1 cM	Chr2:89497272-89498228(-)	NM_001011529	NP_001011529	
		//38711143	human	9606	OR4A16	81327		HGNC:15153		Chr11 q11	Chr11:55343201-55344187(+)	NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274,NM_001005274	NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274,NP_001005274	Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70,Q8NH70
		//38711143	human	9606	OR4A5	81318		HGNC:15162		Chr11 q11	Chr11:54706832-54707902(+)	NM_001005272,NM_001005272,NM_001005272	NP_001005272,NP_001005272,NP_001005272	Q8NH83,Q8NH83,Q8NH83
		//38711143	human	9606	OR4A8	81315		HGNC:15165		Chr11 q11	Chr11:54682876-54683820(+)			P0C604,P0C604,P0C604,P0C604,P0C604,P0C604
		equals(mhomols.get(38711143L).iterator().next(), 38711143L, "mouse, laboratory", 10090L, "Olfr1251");
		List<HomologGene> hgenes = hhomols.get(38711143L);
		assertEquals(3, hgenes.size());
		equals(hgenes.get(0), 38711143L, "human", 9606L, "OR4A16");
		equals(hgenes.get(1), 38711143L, "human", 9606L, "OR4A5");
		equals(hgenes.get(2), 38711143L, "human", 9606L, "OR4A8");
		notEquals(hhomols.get(38711144L).iterator().next(), 38711143L, "humany", 10090L, "SMN2");

	}
	
	private int geneIndex = 0;
	
	@Test
	public void checkHomologs1() throws Exception {

		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest("Homologene", getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		geneIndex = 0;
		Supplier<String> s = ()->"FAKE000000"+(geneIndex++);
		
		List<Entity> tnt = reader.stream()
									.limit(1000)
									.filter(h->h.getHid()==38711143L)
									.map(h->h.setGeneId(s.get()))
									.flatMap(h->connector.apply(h))
									.collect(Collectors.toList());
		assertEquals(7, tnt.size());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000001", tnt.get(1).toString());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000002", tnt.get(3).toString());
		
		assertEquals("FAKE0000000|38711143|Homologene|FAKE0000001|HOMOLOG", tnt.get(1).toCsv());
		assertEquals("FAKE0000000|38711143|Homologene|FAKE0000002|HOMOLOG", tnt.get(3).toCsv());
	}
	
	@Test
	public void checkHomologs2() throws Exception {

		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest("Homologene", getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		geneIndex = 0;
		Supplier<String> s = ()->"FAKE000000"+(geneIndex++);

		List<Entity> tnt = reader.stream()
									.limit(1000)
									.filter(h->h.getHid()==38711324L)
									.map(h->h.setGeneId(s.get()))
									.flatMap(h->connector.apply(h))
									.collect(Collectors.toList());
		assertEquals(3, tnt.size());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000001", tnt.get(1).toString());
		
		// Providing delimiter is set to |
		assertEquals("FAKE0000000|38711324|Homologene|FAKE0000001|HOMOLOG", tnt.get(1).toCsv());
	}
	
	
	@Test
	public void checkHomologs3() throws Exception {

		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest("Homologene", getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		geneIndex = 0;
		Supplier<String> s = ()->"FAKE000000"+(geneIndex++);

		List<Entity> tnt = reader.stream()
									.limit(1000)
									.filter(h->h.getHid()==38711324L)
									.map(h->h.setGeneId(s.get()))
									.flatMap(h->connector.apply(h))
									.filter(h->h instanceof Homolog)
									.collect(Collectors.toList());
		assertEquals(1, tnt.size());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000001", tnt.get(0).toString());
		
		// Providing delimiter is set to |
		assertEquals("FAKE0000000|38711324|Homologene|FAKE0000001|HOMOLOG", tnt.get(0).toCsv());
	}

	private void equals(HomologGene g, Long i, String name, Long l, String gene) {
		assertEquals(i, g.getHid());
		assertEquals(name, g.getOrganismName());
		assertEquals(l, g.getTaxonId());
		assertEquals(gene, g.getSymbol());
	}
	
	private void notEquals(HomologGene g, Long i, String name, Long l, String gene) {
		assertNotEquals(i, g.getHid());
		assertNotEquals(name, g.getOrganismName());
		assertNotEquals(l, g.getTaxonId());
		assertNotEquals(gene, g.getSymbol());
	}

}
