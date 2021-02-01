package org.jax.gweaver.io.connector;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.Gene;
import org.jax.gweaver.domain.GeneticEntity;
import org.jax.gweaver.domain.Variant;
import org.jax.gweaver.io.reader.AbstractDataFileTest;
import org.jax.gweaver.io.reader.AbstractReader;
import org.jax.gweaver.io.reader.GeneReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeneConnectorTest extends AbstractDataFileTest {

	
	private GeneConnector<GeneticEntity, Entity> connector;
	
	@Before
	public void before() throws Exception {
		connector = new GeneConnector<>();
	}
	
	@After
	public void after() throws Exception {
		connector = null;
	}
	
	@Test(expected=NullPointerException.class)
	public void nullGeneException() {
		connector.stream(null);
	}
	
	@Test(expected=ConnectorException.class)
	public void noGeneIdException() {
		connector.stream(new Gene());
	}
	
	@Test(expected=ConnectorException.class)
	public void itsAVariantException() {
		connector.stream(new Variant());
	}
	
	@Test(expected=ConnectorException.class)
	public void twoGenesSameException() {
		Gene gene = new Gene();
		gene.setGeneId("repeat!");
		connector.stream(gene);
		connector.stream(gene);
	}

	@Test
	public void simpleGeneRead1() throws Exception {
		
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", getFile("data/1000/hs_gtf/hg38_2.gtf"));
		List<Entity> found = reader.stream()
								   .flatMap(n->connector.apply(n))
								   .collect(Collectors.toList());
		
		assertEquals(401, found.size());
		assertEquals(1059, reader.linesProcessed());
	}

	/**
	 * Simple gene read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleGeneRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", getFile("data/1000/mm_gtf/mm10_2.gtf"));
		List<Entity> found = reader.stream()
				   .flatMap(n->connector.stream(n))
				   .collect(Collectors.toList());
		
		assertEquals(282, found.size());
		assertEquals(1152, reader.linesProcessed());
	}
	
	/**
	 * Simple gene read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleGeneRead3() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", getFile("data/gz/hg38_2.gtf.gz"));
		List<Entity> found = reader.stream()
				   .flatMap(n->connector.stream(n, null))
				   .collect(Collectors.toList());
		
		assertEquals(401, found.size());
		assertEquals(1059, reader.linesProcessed());
	}

	/**
	 * Simple gene read 3.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void simpleGeneReadWithDefConnector() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", getFile("data/gz/hg38_2.gtf.gz"));
		Function<GeneticEntity, Stream<Entity>> def = reader.getDefaultConnector();
		List<Entity> found = reader.stream()
				   .flatMap(n->def.apply(n))
				   .collect(Collectors.toList());
		
		assertEquals(401, found.size());
		assertEquals(1059, reader.linesProcessed());
	}
	/**
	 * Parallel gene read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=ConnectorException.class)
	public void parallelGeneRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", getFile("data/1000/hs_gtf/hg38_2.gtf"));
		List<Entity> found = reader.stream()
								   .parallel()
								   .flatMap(n->connector.apply(n))
								   .collect(Collectors.toList());
		
		assertEquals(401, found.size());
		assertEquals(1059, reader.linesProcessed());
	}

	/**
	 * Parallel gene read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=ConnectorException.class)
	public void parallelGeneRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", getFile("data/1000/mm_gtf/mm10_2.gtf"));
		List<Entity> found = reader.stream()
								   .parallel()
								   .flatMap(n->connector.apply(n))
								   .collect(Collectors.toList());
		
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
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", getFile("data/zip/hs_gtf/hg38_1.gtf.zip"));
		long count = reader.stream().flatMap(n->connector.apply(n)).count();
		assertEquals(204678, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	/**
	 * Gene zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void geneZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", getFile("data/zip/mm_gtf/mm10_1.gtf.zip"));
		long count = reader.stream().flatMap(n->connector.apply(n)).count();
		assertEquals(164914, count);
		assertEquals(899084, reader.linesProcessed());
	}
	/**
	 * Parallel gene zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=ConnectorException.class)
	public void parallelGeneZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", getFile("data/zip/hs_gtf/hg38_1.gtf.zip"));
		long count = reader.stream().parallel().flatMap(n->connector.apply(n)).count();
		assertEquals(115709, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	/**
	 * Parallel gene zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=ConnectorException.class)
	public void parallelGeneZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", getFile("data/zip/mm_gtf/mm10_1.gtf.zip"));
		long count = reader.stream().parallel().flatMap(n->connector.apply(n)).count();
		assertEquals(95996, count);
		assertEquals(899084, reader.linesProcessed());
	}

}
