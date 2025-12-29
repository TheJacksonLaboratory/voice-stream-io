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
package org.jax.voice.io.reader;

import static org.junit.Assert.assertEquals;

import org.jax.voice.domain.Anchor;
import org.jax.voice.domain.ChromatinInteraction;
import org.jax.voice.domain.ExperimentMetadata;
import org.jax.voice.io.reader.AbstractXlsReader;
import org.jax.voice.io.reader.ChiapetReader;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.junit.Test;

public class ChiapetReaderTest extends AbstractDataFileTest {

	@Test
	public void countSheet1() throws Exception {
		ChiapetReader<ChromatinInteraction> reader = new ChiapetReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(1);
		reader.setConcreteClass(Anchor.class);
		assertEquals(11162-3, reader.stream().count());	
	}

	@Test
	public void countSheet2() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(2);
		reader.setConcreteClass(Anchor.class);
		assertEquals(14607-3, reader.stream().count());	
	}

	@Test
	public void countSheet3() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(3);
		reader.setConcreteClass(Anchor.class);
		assertEquals(28977-3, reader.stream().count());	
	}

	@Test
	public void countSheet4() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(4);
		reader.setConcreteClass(Anchor.class);
		assertEquals(30588-3, reader.stream().count());	
	}

	@Test
	public void countSheet5() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(5);
		assertEquals(13285-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet6() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(6);
		assertEquals(19860-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet7() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(7);
		assertEquals(65000-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet8() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(8);
		assertEquals(61894-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet9() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(9);
		assertEquals(50060-4, reader.stream().count());	
	}
	
	@Test
	public void checkMetaReferenceInObjects() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ExperimentMetadata> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(9);
		reader.setMeta(new ExperimentMetadata("MCF7", "breast", "Polr2a", "ChIA-PET", "PMID:22265404"));

		assertEquals(50060-4, reader.stream().filter(ci->ci.getMeta()!=null).count());	
	}

	@Test
	public void filterFdr() throws Exception {
		
		AbstractXlsReader<ChromatinInteraction, ExperimentMetadata> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(7);
		reader.setMeta(new ExperimentMetadata("MCF7", "breast", "Polr2a", "ChIA-PET", "PMID:22265404"));
		
		// All less than 0.05
		assertEquals(65000-4, reader.stream().filter(c->c.getFdr()<0.05).count());	
		
		// Only a few greater than 0.00000085
		assertEquals(800, reader.stream().filter(c->c.getFdr()>0.00000085).count());
		
		// Only a few more greater than 0.00000000032
		assertEquals(9746, reader.stream().filter(c->c.getFdr()>0.00000000032).count());	

	}
	
	@Test
	public void filterBaseSpan() throws Exception {
		
		AbstractXlsReader<ChromatinInteraction, ExperimentMetadata> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(7);
		reader.setMeta(new ExperimentMetadata("MCF7", "breast", "Polr2a", "ChIA-PET", "PMID:22265404"));
		
		// Which span more than 5000
		assertEquals(13800, reader.stream().filter(c->c.getLeft().span()>5000 && c.getRight().span()>5000).count());	
	}

}
