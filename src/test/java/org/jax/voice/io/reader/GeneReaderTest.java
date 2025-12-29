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
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jax.voice.domain.GeneticEntity;
import org.jax.voice.io.reader.GeneReader;
import org.jax.voice.io.reader.LineIteratorReader;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;
import org.junit.Test;

/**
 * A test of gene reading as a stream.
 * 
 * @author gerrim
 *
 */
public class GeneReaderTest extends AbstractDataFileTest {
	/**
	 * TODO
	 * 1. Test more than one file in a directory
	 * 2. Test directory of zip and unzipped files.
	 * 3. Test more than one zip in a directory.
	 * 4. Test more than one file in a zip
	 * 5. Test directories in a zip (may fail)
	 * 6. Test links are made between nodes as required.
	 *
	 * @throws Exception the exception
	 */
	
	
	@Test
	public void simpleGeneRead1() throws Exception {
		GeneReader<GeneticEntity> reader = new GeneReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gtf/hg38_2.gtf")));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(230, found.size());
		assertEquals(1059, reader.linesProcessed());
	}
	
	@Test
	public void simpleGeneRead1Wind() throws Exception {
		GeneReader<GeneticEntity> reader = new GeneReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gtf/hg38_2.gtf")));
		List<GeneticEntity> found = new LinkedList<>();
		while(!reader.isEmpty()) {
			found.addAll(reader.wind());
		}
		
		assertEquals(230, found.size());
		assertEquals(1059, reader.linesProcessed());
	}

	/**
	 * Simple gene read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleGeneRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/1000/mm_gtf/mm10_2.gtf")));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(168, found.size());
		assertEquals(1152, reader.linesProcessed());
	}
	
	/**
	 * Simple gene read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleGeneRead3() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/hg38_2.gtf.gz")));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(230, found.size());
		assertEquals(1059, reader.linesProcessed());
	}
	
	/**
	 * Simple gene read 4.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleGeneRead4() throws Exception {
		
		InputStream in = new FileInputStream(getFile("data/gz/hg38_2.gtf.gz"));
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", in, "hg38_2.gtf.gz"));
		try {
			List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
			
			assertEquals(230, found.size());
			assertEquals(1059, reader.linesProcessed());
		} finally {
			reader.close();
		}
		
		try {
			in.read();
		} catch(IOException expected) {
			assertEquals("Stream closed".toLowerCase(), expected.getMessage().toLowerCase());
			return;
		}
		throw new Exception("The stream was not closed!");
	}
	
	@Test
	public void chunkSize() throws Exception {
		
		LineIteratorReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/hg38_2.gtf.gz")));
		assertEquals(10000, reader.getChunkSize());
	}

	@Test
	public void singleWind() throws Exception {
		
		LineIteratorReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/hg38_2.gtf.gz")));
		List<GeneticEntity> chunk = reader.wind();
		assertEquals(230, chunk.size());
	}

	@Test
	public void isEmpty() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/hg38_2.gtf.gz")));
		reader.stream().count();
		assertTrue(reader.isEmpty());
	}

	/**
	 * Parallel gene read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelGeneRead1() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gtf/hg38_2.gtf")));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(230, found.size());
		assertEquals(1059, reader.linesProcessed());
	}

	/**
	 * Parallel gene read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelGeneRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/1000/mm_gtf/mm10_2.gtf")));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(168, found.size());
		assertEquals(1152, reader.linesProcessed());
	}
	
	/**
	 * Gene zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void geneZipRead1() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/zip/hs_gtf/hg38_1.gtf.zip")));
		long count = reader.stream().count();
		assertEquals(115709, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	/**
	 * Gene zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void geneZipRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/zip/mm_gtf/mm10_1.gtf.zip")));
		long count = reader.stream().count();
		assertEquals(95996, count);
		assertEquals(899084, reader.linesProcessed());
	}
	/**
	 * Parallel gene zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelGeneZipRead1() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/zip/hs_gtf/hg38_1.gtf.zip")));
		long count = reader.stream().parallel().count();
		assertEquals(115709, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	/**
	 * Parallel gene zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelGeneZipRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/zip/mm_gtf/mm10_1.gtf.zip")));
		long count = reader.stream().parallel().count();
		assertEquals(95996, count);
		assertEquals(899084, reader.linesProcessed());
	}

}
