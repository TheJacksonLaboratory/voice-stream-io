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

import org.geneweaver.domain.Fantom5Link;
import org.junit.Test;

public class Fantom5EnsemblMapReaderTest extends AbstractDataFileTest {

	
	@Test
	public void chunkSize() throws Exception {
		
		Fantom5EnsemblMapReader<Fantom5Link> reader = new Fantom5EnsemblMapReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(4096, reader.getChunkSize());
	}

	@Test
	public void parseSimpleTestFile1() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(48700-5, reader.stream().count());
	}

	@Test
	public void countGeneReferences() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(5084105, reader.stream().mapToInt(f->f.getEnsemblIds().size()).sum());
	}

	@Test
	public void countObjectsWithSpanGt5000() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(48592, reader.stream().mapToInt(f->f.span()).filter(i->i>5000).count());
	}

	@Test
	public void countObjectsWithSpanGt100000() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(36332, reader.stream().mapToInt(f->f.span()).filter(i->i>100000).count());
	}

	@Test
	public void countObjectsWithSpanGt1000000() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(43, reader.stream().mapToInt(f->f.span()).filter(i->i>1000000).count());
	}

}
