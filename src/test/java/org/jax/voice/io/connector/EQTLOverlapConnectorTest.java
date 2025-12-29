package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jax.voice.domain.EQTLOverlap;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Variant;
import org.jax.voice.io.IPrintStream;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;
import org.junit.Test;

public class EQTLOverlapConnectorTest extends AbstractDataFileTest{
	
	
	@Test
	public void simpleEQTLOverlapCreationMouse() throws Exception {

		Path gdir = Paths.get("tmp/simpleEQTLOverlapCreation/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
	
		Path input =  getPath("data/eQTL/mm/lo/Chesler_Striatum_DO_pr69k_lo.csv");
		try (AbstractOverlapConnector<Variant, Entity> conn = new EQTLOverlapConnector<>("Mus musculus", "eqtloverlaps")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long neqtl = conn.create(null, IPrintStream.of(System.out)); 
			assertEquals(27444, neqtl);
			
			// First line in Chesler_Striatum_DO_pr69k_lo.csv
			// 1. 108233733,3,ENSMUSG00000000001,10.9194777703955,3_108233733,NA,chr3,108141049
			Variant search = createVariant("test", "3", 108141040, 108141050);
			List<Entity> ents = conn.stream(search).toList();
			assertEquals(2, ents.size());
			
			// Middle of file
			// 2. 25972921,10,ENSMUSG00000039089,11.0811397106921,10_25972921,NA,chr10,25848819
			search = createVariant("test", "10", 25848810, 25848820);
			ents = conn.stream(search).toList();
			assertEquals(2, ents.size());
			
			// Near of file
			// 3. 77434114,8,ENSMUSG00000110258,54.0145969225715,8_77434114,NA,chr8,78160743
			search = createVariant("test", "8", 78160742, 78160743);
			ents = conn.stream(search).toList();
			assertEquals(4, ents.size());

		}

	}
	
	@Test
	public void intersects() {
		
		int a = 40, b=50, A=49, B=49;
		boolean inter = (a <= B) && (A <= b);
		assertTrue(inter);
		
		a = 40; b=41; A=49; B=49;
		inter = (a <= B) && (A <= b);
		assertFalse(inter);

		a = 49; b=50; A=49; B=49;
		inter = (a <= B) && (A <= b);
		assertTrue(inter);

		a = 50; b=50; A=49; B=49;
		inter = (a <= B) && (A <= b);
		assertFalse(inter);
		
		a = 50; b=50; A=50; B=50;
		inter = (a <= B) && (A <= b);
		assertTrue(inter);

		a = 50; b=60; A=55; B=55;
		inter = (a <= B) && (A <= b);
		assertTrue(inter);
		
		a = 55; b=56; A=50; B=60;
		inter = (a <= B) && (A <= b);
		assertTrue(inter);
		
		a = 60; b=65; A=50; B=59;
		inter = (a <= B) && (A <= b);
		assertFalse(inter);
		
		a = 60; b=65; A=50; B=60;
		inter = (a <= B) && (A <= b);
		assertTrue(inter);
	}

	@Test
	public void eQTLOverlapCreationWithErrors() throws Exception {

		Path gdir = Paths.get("tmp/eQTLOverlapCreationWithErrors/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
	
		Path input =  getPath("data/eQTL/mm/lo/Aging_Bone_withErrors_lo.csv");
		try (AbstractOverlapConnector<Variant, Entity> conn = new EQTLOverlapConnector<>("Mus musculus", "eqtloverlaps")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long neqtl = conn.create(null, IPrintStream.of(System.out)); 
			assertEquals(10, neqtl);
		}
	}

	@Test
	public void eQTLOverlapMouseAgingBone() throws Exception {

		Path gdir = Paths.get("tmp/eQTLOverlapMouse/mm");
		FileUtils.deleteQuietly(gdir.toFile());
		Files.createDirectories(gdir);
	
		Path input =  getPath("data/eQTL/mm/lo/Aging_Bone_DO_small_lo.csv");
		Path vpath = getPath("data/eQTL/some.gvf");
		testIntersections(gdir, input, 90, vpath, 6);
	}

	private void testIntersections(Path gdir, Path input, int size, Path vpath, int expected) throws Exception {
		try (AbstractOverlapConnector<Variant, Entity> conn = new EQTLOverlapConnector<>("Mus musculus", "eqtloverlaps")) {
			conn.setLocation(gdir);
			conn.add(input);
		
		    long neqtl = conn.create(null, IPrintStream.of(System.out)); 
			assertEquals(size, neqtl);
			
			StreamReader<Variant> vars = ReaderFactory.getReader(new ReaderRequest(vpath.getFileName().toString(), vpath));
			List<Entity> allEntities = vars.stream()
				.flatMap(conn::stream)
				.collect(Collectors.toList());
			
			List<Entity> eqtlOvers = allEntities.stream().filter(e -> e instanceof EQTLOverlap).toList();
			assertEquals(expected, eqtlOvers.size());
			assertTrue(eqtlOvers.stream().allMatch(e->"Bone".equalsIgnoreCase(((EQTLOverlap)e).getTissueName())));
		}

	}
}
