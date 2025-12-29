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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.jax.voice.domain.Gene;
import org.jax.voice.domain.GeneticEntity;
import org.jax.voice.domain.Variant;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Simple test to check file parsing without neo4j involved.
 * Test just parses files into objects without mixing in neo4j.
 * This means it is fast to run and try different parsing options.
 * 
 * @author Matthew Gerring
 *
 */
public class VariantReaderTest extends AbstractDataFileTest {
	
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
	public void chunkSize() throws Exception {
		VariantReader<GeneticEntity> reader = new VariantReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf")));
		assertEquals(4096, reader.getChunkSize());
	}

	/**
	 * Simple variant read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead1() throws Exception {
		
		StreamReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf")));
		List<Variant> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
		checkVariantEffects(found, 25);
	}

	/**
	 * Simple variant read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead2() throws Exception {
		
		StreamReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf")));
		List<Variant> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
		checkVariantEffects(found, 0.2);
	}

	/**
	 * Simple variant read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead3() throws Exception {
		
		StreamReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz")));
		List<Variant> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
		checkVariantEffects(found, 25);
	}

	/**
	 * Simple variant read 4.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead4() throws Exception {
		
		try (InputStream in = new FileInputStream(getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz"))) {
		
			StreamReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", in, "consequences_2.gvf.gz"));
			List<Variant> found = reader.stream().collect(Collectors.toList());
			
			assertEquals(1000, found.size());
			assertEquals(1000, reader.linesProcessed());
			checkVariantEffects(found, 25);
		}
	}

	
	/**
	 * Check variant effects.
	 *
	 * @param found the found
	 * @param factor the factor
	 */
	private void checkVariantEffects(List<Variant> found, double factor) {
		List<Variant> withEffect = found.stream().filter(v->v.getVariantEffect()!=null).collect(Collectors.toList());
		List<Variant> noEffect = found.stream().filter(v->v.getVariantEffect()==null).collect(Collectors.toList());
		
		assertTrue("The variants with no variant effect were size "+noEffect.size()+". While those with were "+withEffect.size(), noEffect.size()*factor<withEffect.size()); // Few should have no effect
	}


	/**
	 * Parallel variant read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantRead1() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf")));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	/**
	 * Parallel variant read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf")));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
	}
	
	/**
	 * Simple repeat test 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleRepeatTest1() throws Exception {
		
		int rsize = 100000;
		StreamReader<GeneticEntity> reader = new RepeatedLineReader<>(new ReaderRequest("Mus musculus", rsize, Gene.class));
		long size = reader.stream().count();
		assertEquals(rsize, size);
		assertEquals(rsize, reader.linesProcessed());
	}

	/**
	 * Simple repeat test 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleRepeatTest2() throws Exception {
		
		int rsize = 100000;
		StreamReader<GeneticEntity> reader = new RepeatedLineReader<>(new ReaderRequest("Mus musculus", rsize, Variant.class));
		long size = reader.stream().count();
		assertEquals(rsize, size);
		assertEquals(rsize, reader.linesProcessed());
	}

	/**
	 * Variant G zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void variantGZipRead1() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_1.gvf.gz")));
		long count = reader.stream().count();
		assertEquals(872993, count);
		assertEquals(872993, reader.linesProcessed());
	}

	/**
	 * Variant G zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void variantGZipRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/gz/mus_musculus_incl_consequences_1.gvf.gz")));
		long count = reader.stream().count();
		assertEquals(1726211, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	
	/**
	 * Parallel variant zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantZipRead1() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip")));
		long count = reader.stream().parallel().count();
		assertEquals(872993, count);
		assertEquals(872993, reader.linesProcessed());
	}

	/**
	 * Parallel variant zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantZipRead2() throws Exception {
		
		StreamReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip")));
		long count = reader.stream().parallel().count();
		assertEquals(1726211, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	
	@Ignore
	@Test
	public void read38() throws Exception {
		
		VariantReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("/Volumes/Work/JAX/data/mus_musculus_incl_consequences.gvf.gz")));
		
		try (PrintWriter out = new PrintWriter("/Volumes/Work/JAX/data/mus_musculus_GRCm38_mt.csv")) {
			out.print("# rsId, chr, start, end");
			reader.stream().forEach(v->{
				if (v.getChr().toLowerCase().contains("m")) {
					out.print(v.getRsId());
					out.print(",");
					out.print(v.getChr());
					out.print(",");
					out.print(v.getStart());
					out.print(",");
					out.println(v.getEnd());
					out.flush();
				}
			});
		}
	}

}
