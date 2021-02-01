package org.jax.gweaver.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.GeneticEntity;
import org.jax.gweaver.domain.Variant;
import org.jax.gweaver.io.reader.AbstractDataFileTest;
import org.jax.gweaver.io.reader.AbstractReader;
import org.jax.gweaver.io.reader.GeneReader;
import org.jax.gweaver.io.reader.RepeatedLineReader;
import org.jax.gweaver.io.reader.VariantReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VariantConnectorTest extends AbstractDataFileTest {
	
	private VariantConnector connector;
	
	@Before
	public void before() throws Exception {
		connector = new VariantConnector();
	}
	
	@After
	public void after() throws Exception {
		connector = null;
	}

	@Test
	public void simpleVariantRead1() throws Exception {
		
		AbstractReader<Variant> reader = new VariantReader<>("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf"));
		List<Entity> found = reader.stream().flatMap(v->connector.apply(v)).collect(Collectors.toList());
		
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
		
		AbstractReader<Variant> reader = new VariantReader<>("Homo sapiens", getFile("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf"));
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
		
		AbstractReader<Variant> reader = new VariantReader<>("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz"));
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
		
		try (InputStream in = new GZIPInputStream(new FileInputStream(getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz")))) {
		
			AbstractReader<Variant> reader = new VariantReader<>("Homo sapiens", in);
			List<Entity> found = reader.stream().flatMap(v->connector.apply(v)).collect(Collectors.toList());
			
			assertEquals(6860, found.size());
			assertEquals(1000, reader.linesProcessed());
		}
	}

	/**
	 * Simple variant read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=NullPointerException.class)
	public void simpleVariantReadBadSession() throws Exception {
		
		VariantConnector sconn = new VariantConnector(true);
		AbstractReader<Variant> reader = new VariantReader<>("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_2.gvf.gz"));
		reader.stream().flatMap(v->sconn.stream(v, null)).collect(Collectors.toList());
	}



	/**
	 * Parallel variant read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parallelVariantRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", getFile("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf"));
		List<Entity> found = reader.stream().parallel().flatMap(v->connector.apply(v)).collect(Collectors.toList());
		
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
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", getFile("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf"));
		List<Entity> found = reader.stream().parallel().flatMap(v->connector.apply(v)).collect(Collectors.toList());
		
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
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Mus musculus", rsize, VariantReader.class);
		long size = reader.stream().flatMap(v->connector.apply(v)).count();
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
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", getFile("data/gz/homo_sapiens_incl_consequences_1.gvf.gz"));
		long count = reader.stream().flatMap(v->connector.apply(v)).count();
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
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", getFile("data/gz/mus_musculus_incl_consequences_1.gvf.gz"));
		long count = reader.stream().flatMap(v->connector.apply(v)).count();
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
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", getFile("data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip"));
		long count = reader.stream().parallel().flatMap(v->connector.apply(v)).count();
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
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", getFile("data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip"));
		long count = reader.stream().parallel().flatMap(v->connector.apply(v)).count();
		assertEquals(6648629, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	

}
