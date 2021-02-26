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
	
	@Test
	public void checkUrl() throws Exception {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest(new URL("http://www.informatics.jax.org/downloads/reports/HOM_MouseHumanSequence.rpt")));
		assertEquals(40015, reader.stream().count());
	}

	
	@Test
	public void checkUrlAsPath() throws Exception {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Test", Paths.get("http://www.informatics.jax.org/downloads/reports/HOM_MouseHumanSequence.rpt")));
		assertEquals(40015, reader.stream().count());
	}

	@Test
	public void count1() throws Exception {
		
		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest(getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		assertEquals(40015, reader.stream().count());
		assertEquals(20891, reader.stream().filter(h->h.getOrganismName().toLowerCase().startsWith("mouse")).count());
		assertEquals(19124, reader.stream().filter(h->h.getOrganismName().toLowerCase().startsWith("human")).count());
	}

	@Test
	public void findHomologs() throws Exception {
		
		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest(getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		List<Entity> homols = reader.stream()
									.flatMap(h->connector.apply(h))
									.filter(e->e instanceof Homolog)
									.collect(Collectors.toList());
		assertEquals(17316, homols.size());
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
		
		//3	mouse, laboratory	10090	Acadm	11364	MGI:87867			Chr3 78.77 cM	Chr3:153922357-153944632(-)	NM_007382	NP_031408	P45952
		//3	human	9606	ACADM	34		HGNC:89	OMIM:607008	Chr1 p31.1	Chr1:75724347-75763679(+)	NM_001286043,NM_000016,NM_001127328,NM_001286042,NM_001286044	NP_001120800,NP_001272971,NP_001272972,NP_001272973,NP_000007	P11310
		equals(mhomols.get(3L).iterator().next(), 3L, "mouse, laboratory", 10090L, "Acadm");
		equals(hhomols.get(3L).iterator().next(), 3L, "human", 9606L, "ACADM");
		notEquals(hhomols.get(5L).iterator().next(), 3L, "humany", 10090L, "ACADM");
		
		//35	mouse, laboratory	10090	Atp7a	11977	MGI:99400			ChrX 47.36 cM	ChrX:106027276-106124926(+)	NM_001109757,NM_009726	NP_033856,NP_001103227	Q64430
		//35	human	9606	ATP7A	538		HGNC:869	OMIM:300011	ChrX q21.1	ChrX:77910656-78050395(+)	NM_001282224,NM_000052	NP_001269153,NP_000043	Q04656
		equals(mhomols.get(35L).iterator().next(), 35L, "mouse, laboratory", 10090L, "Atp7a");
		equals(hhomols.get(35L).iterator().next(), 35L, "human", 9606L, "ATP7A");
		notEquals(hhomols.get(39L).iterator().next(), 35L, "humany", 10090L, "ATP7A");
		
		//292	mouse, laboratory	10090	Smn1	20595	MGI:109257			Chr13 52.99 cM	Chr13:100124852-100137690(+)	NM_011420,NM_001252629,XM_011244637	XP_011242939,NP_001239558,NP_035550	P97801
		//292	human	9606	SMN1	6606		HGNC:11117	OMIM:600354	Chr5 q13.2	Chr5:70924941-70953015(+)	NM_000344,NM_022874,NM_001297715	NP_075012,NP_001284644,NP_000335,XP_011541898,XP_016865275,XP_011541899,XP_011541900	Q16637
		//292	human	9606	SMN2	6607		HGNC:11118	OMIM:601627	Chr5 q13.2	Chr5:70049523-70077595(+)	NM_022876,NM_017411,NM_022875,NM_022877	XP_016865276,XP_011541905,NP_059107,NP_075013,NP_075014,NP_075015,XP_011541901,XP_011541902,XP_011541903,XP_011541904	Q16637
		equals(mhomols.get(292L).iterator().next(), 292L, "mouse, laboratory", 10090L, "Smn1");
		List<HomologGene> hgenes = hhomols.get(292L);
		assertEquals(2, hgenes.size());
		equals(hgenes.get(0), 292L, "human", 9606L, "SMN1");
		equals(hgenes.get(1), 292L, "human", 9606L, "SMN2");
		notEquals(hhomols.get(293L).iterator().next(), 292L, "humany", 10090L, "SMN2");

		//660	mouse, laboratory	10090	Gstp1	14870	MGI:95865			Chr19 3.75 cM	Chr19:4035411-4037912(-)	NM_013541	NP_038569	P19157
		//660	mouse, laboratory	10090	Gstp2	14869	MGI:95864			Chr19 3.75 cM	Chr19:4040288-4042221(-)	NM_181796	NP_861461	P46425
		//660	mouse, laboratory	10090	Gstp-ps	100042625	MGI:3782108			Chr1 97.2 cM	Chr1:192073820-192074450(-)	XM_036155425	XP_036011318	
		//660	human	9606	GSTP1	2950		HGNC:4638	OMIM:134660	Chr11 q13.2	Chr11:67583595-67586653(+)	NM_000852	NP_000843	P09211
		List<HomologGene> mgenes = mhomols.get(660L);
		assertEquals(3, mgenes.size());
		equals(mgenes.get(0), 660L, "mouse, laboratory", 10090L, "Gstp1");
		equals(mgenes.get(1), 660L, "mouse, laboratory", 10090L, "Gstp2");
		equals(mgenes.get(2), 660L, "mouse, laboratory", 10090L, "Gstp-ps");
		equals(hhomols.get(660L).iterator().next(), 660L, "human", 9606L, "GSTP1");
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
									.filter(h->h.getHid()==292L)
									.map(h->h.setGeneId(s.get()))
									.flatMap(h->connector.apply(h))
									.collect(Collectors.toList());
		assertEquals(5, tnt.size());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000001", tnt.get(1).toString());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000002", tnt.get(3).toString());
		
		assertEquals("FAKE0000000±292±Homologene±FAKE0000001±HOMOLOG", tnt.get(1).toCsv());
		assertEquals("FAKE0000000±292±Homologene±FAKE0000002±HOMOLOG", tnt.get(3).toCsv());
	}
	
	@Test
	public void checkHomologs2() throws Exception {

		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest("Homologene", getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		geneIndex = 0;
		Supplier<String> s = ()->"FAKE000000"+(geneIndex++);

		List<Entity> tnt = reader.stream()
									.limit(1000)
									.filter(h->h.getHid()==660L)
									.map(h->h.setGeneId(s.get()))
									.flatMap(h->connector.apply(h))
									.collect(Collectors.toList());
		assertEquals(7, tnt.size());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000003", tnt.get(3).toString());
		assertEquals("FAKE0000001-[HOMOLOG]->FAKE0000003", tnt.get(4).toString());
		assertEquals("FAKE0000002-[HOMOLOG]->FAKE0000003", tnt.get(5).toString());
		
		// Providing delimiter is set to ±
		assertEquals("FAKE0000000±660±Homologene±FAKE0000003±HOMOLOG", tnt.get(3).toCsv());
		assertEquals("FAKE0000001±660±Homologene±FAKE0000003±HOMOLOG", tnt.get(4).toCsv());
		assertEquals("FAKE0000002±660±Homologene±FAKE0000003±HOMOLOG", tnt.get(5).toCsv());
	}
	
	
	@Test
	public void checkHomologs3() throws Exception {

		LineIteratorReader<HomologGene> reader = ReaderFactory.getReader(new ReaderRequest("Homologene", getFile("data/homol/HOM_MouseHumanSequence.rpt.gz")));
		
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		
		geneIndex = 0;
		Supplier<String> s = ()->"FAKE000000"+(geneIndex++);

		List<Entity> tnt = reader.stream()
									.limit(1000)
									.filter(h->h.getHid()==660L)
									.map(h->h.setGeneId(s.get()))
									.flatMap(h->connector.apply(h))
									.filter(h->h instanceof Homolog)
									.collect(Collectors.toList());
		assertEquals(3, tnt.size());
		assertEquals("FAKE0000000-[HOMOLOG]->FAKE0000003", tnt.get(0).toString());
		assertEquals("FAKE0000001-[HOMOLOG]->FAKE0000003", tnt.get(1).toString());
		assertEquals("FAKE0000002-[HOMOLOG]->FAKE0000003", tnt.get(2).toString());
		
		// Providing delimiter is set to ±
		assertEquals("FAKE0000000±660±Homologene±FAKE0000003±HOMOLOG", tnt.get(0).toCsv());
		assertEquals("FAKE0000001±660±Homologene±FAKE0000003±HOMOLOG", tnt.get(1).toCsv());
		assertEquals("FAKE0000002±660±Homologene±FAKE0000003±HOMOLOG", tnt.get(2).toCsv());
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
