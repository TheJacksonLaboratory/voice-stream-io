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
package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jax.voice.domain.Entity;
import org.jax.voice.domain.GeneticEntity;
import org.jax.voice.domain.Variant;
import org.jax.voice.io.connector.Connector;
import org.jax.voice.io.connector.VariantConnector;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.jax.voice.io.reader.LineIteratorReader;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.RepeatedLineReader;
import org.jax.voice.io.reader.StreamReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VariantConnectorTest extends AbstractDataFileTest {
	
	private Connector<GeneticEntity, Entity> connector;
	
	@Before
	public void before() throws Exception {
		connector = new VariantConnector<>();
	}
	
	@After
	public void after() throws Exception {
		connector = null;
	}

	@Test
	public void simpleVariantRead1() throws Exception {
		
		LineIteratorReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf")));
		List<Entity> found = reader.stream().flatMap(v->connector.stream(v)).collect(Collectors.toList());
		
		assertEquals(6860, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	/**
	 * Simple variant read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead2() throws Exception {
		
		LineIteratorReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf")));
		List<Entity> found = reader.stream().flatMap(v->connector.stream(v)).collect(Collectors.toList());
		
		assertEquals(1286, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	/**
	 * Simple variant read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead3() throws Exception {
		
		LineIteratorReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz")));
		List<Entity> found = reader.stream().flatMap(v->connector.stream(v, null)).collect(Collectors.toList());
		
		assertEquals(6860, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	/**
	 * Simple variant read 4.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantRead4() throws Exception {
		
		try (InputStream in = new FileInputStream(getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz"))) {
		
			LineIteratorReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", in, "consequences_2.gvf.gz"));
			List<Entity> found = reader.stream().flatMap(v->connector.stream(v)).collect(Collectors.toList());
			
			assertEquals(6860, found.size());
			assertEquals(1000, reader.linesProcessed());
		}
	}

	/**
	 * Simple variant read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleVariantReadWithDefConnector() throws Exception {
		
		LineIteratorReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz")));
		Function<Variant, Stream<Entity>> def = reader.getDefaultConnector();
		List<Entity> found = reader.stream().flatMap(v->def.apply(v)).collect(Collectors.toList());
		
		assertEquals(6860, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	/**
	 * Simple variant read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=NullPointerException.class)
	public void simpleVariantReadBadSession() throws Exception {
		
		VariantConnector<Variant, ?> sconn = new VariantConnector<>(true);
		LineIteratorReader<Variant> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz")));
		reader.stream().flatMap(v->sconn.stream(v, null)).collect(Collectors.toList());
	}



	/**
	 * Parallel variant read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantRead1() throws Exception {
		
		LineIteratorReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf")));
		List<Entity> found = reader.stream().parallel().flatMap(v->connector.stream(v)).collect(Collectors.toList());
		
		assertEquals(6860, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	/**
	 * Parallel variant read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantRead2() throws Exception {
		
		LineIteratorReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf")));
		List<Entity> found = reader.stream().parallel().flatMap(v->connector.stream(v)).collect(Collectors.toList());
		
		assertEquals(1286, found.size());
		assertEquals(1000, reader.linesProcessed());
	}
	
	/**
	 * Simple repeat test 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleRepeatTest2() throws Exception {
		
		int rsize = 100000;
		LineIteratorReader<GeneticEntity> reader = new RepeatedLineReader<>(new ReaderRequest("Mus musculus", rsize, Variant.class));
		long size = reader.stream().flatMap(v->connector.stream(v)).count();
		assertEquals(rsize*2, size);
		assertEquals(rsize, reader.linesProcessed());
	}

	
	/**
	 * Variant G zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void variantGZipRead1() throws Exception {
		
		LineIteratorReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_1.gvf.gz")));
		long count = reader.stream().flatMap(v->connector.stream(v)).count();
		assertEquals(7367093, count);
		assertEquals(872993, reader.linesProcessed());
	}

	/**
	 * Variant G zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void variantGZipRead2() throws Exception {
		
		LineIteratorReader<GeneticEntity> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", getFile("data/gz/mus_musculus_incl_consequences_1.gvf.gz")));
		long count = reader.stream().flatMap(v->connector.stream(v)).count();
		assertEquals(6648629, count);
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
		long count = reader.stream().parallel().flatMap(v->connector.stream(v)).count();
		assertEquals(7367093, count);
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
		long count = reader.stream().parallel().flatMap(v->connector.stream(v)).count();
		assertEquals(6648629, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	

}
