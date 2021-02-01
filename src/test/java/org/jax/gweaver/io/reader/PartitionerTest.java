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
package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jax.gweaver.io.Configuration;
import org.jax.gweaver.io.PartitionException;
import org.jax.gweaver.io.Partitioner;
import org.jax.gweaver.io.Configuration.ZipType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * Tests the partitioner with different inputs including nulls and empy files.
 * @author gerrim
 *
 */
public class PartitionerTest extends AbstractDataFileTest{
	 
	/** The parter. */
	private Partitioner parter;
	
	/**
	 * Creates the.
	 *
	 * @throws PartitionException the partition exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void create() throws PartitionException, IOException {
		Path parent = Paths.get("tmp/partitionerTest");
		parent.toFile().mkdirs();
		Path dir = Files.createTempDirectory(parent, "test_");
		clear(dir);
		dir.toFile().mkdirs();
		this.parter = new Partitioner(dir, Configuration.getDefault());
	}

	/**
	 * After.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void after() throws Exception {
		clear(parter.getDir());
	}
	
	/**
	 * Clear.
	 *
	 * @param dir the dir
	 */
	public void clear(Path dir) {
		if (Files.exists(dir)) {
			FileUtils.deleteQuietly(dir.toFile());
		}
	}
	
	/**
	 * Null source test.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=NullPointerException.class)
	public void nullSourceTest() throws Exception {
		List<Path> paths = new ArrayList<>(380);
		parter.partition(null, paths::add);
	}
	
	/**
	 * Null consumer allowed test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void nullConsumerAllowedTest() throws Exception {
		Path source = super.getPath("data/1000/hs_gtf/hg38_2.gtf");
		parter.getConfiguration().setPartitionLines(100);
		parter.getConfiguration().setZipType(ZipType.NONE);
		parter.partition(source, null); // NO CONSUMER
		assertEquals(7, Files.list(parter.getDir()).count());
		
		int fcount = Files.list(parter.getDir()).mapToInt(p->{
			try {
				return Files.readAllLines(p).size();
			} catch (IOException e) {
				fail(e.getMessage());
				return 0;
			}
		}).sum();
		assertEquals(1064, fcount);
	}
	
	/**
	 * File not there.
	 *
	 * @throws Exception the exception
	 */
	@Test(expected=NoSuchFileException.class)
	public void fileNotThere() throws Exception {
		Path source = Paths.get("FILE_NOT_THERE.gvf");
		parter.partition(source, null);
	}

	/**
	 * Large.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws PartitionException the partition exception
	 */
	@Ignore
	@Test
	public void large() throws IOException, InterruptedException, PartitionException {
		
		Path source = super.getPath("data/io/files.zip");
		List<Path> paths = new ArrayList<>(380);
		parter.partition(source, paths::add);
		
		assertEquals(379, paths.size());
		assertTrue(paths.stream().allMatch(p->p.getFileName().toString().endsWith(".zip")));
	}
	
	/**
	 * Smaller than 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void smallerThan1() throws Exception {
		lines("data/1000/hs_gtf/hg38_2.gtf", 1, 1064);
	}
	
	/**
	 * Smaller than 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void smallerThan2() throws Exception {
		lines("data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf", 1, 1034);
	}
	
	/**
	 * Reduced partition.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void reducedPartition() throws Exception {
		parter.getConfiguration().setPartitionLines(100);
		lines("data/1000/hs_gtf/hg38_2.gtf", 7, 1064);
	}
	
	/**
	 * Hs 38 1 G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void hs38_1GZip() throws Exception {
		parter.getConfiguration().setPartitionLines(10000);
		lines("data/gz/hg38_1.gtf.gz", 116, ZipType.GZ);
	}
	
	/**
	 * Homo sapiens incl consequences G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void homo_sapiens_incl_consequencesGZip() throws Exception {
		parter.getConfiguration().setPartitionLines(10000);
		lines("data/gz/homo_sapiens_incl_consequences_1.gvf.gz", 88, ZipType.GZ);
	}

	/**
	 * Mm 10 1 G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void mm10_1GZip() throws Exception {
		parter.getConfiguration().setPartitionLines(10000);
		lines("data/gz/mm10_1.gtf.gz", 90, ZipType.GZ);
	}
	
	/**
	 * Mus musculus incl consequences G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void mus_musculus_incl_consequencesGZip() throws Exception {
		parter.getConfiguration().setPartitionLines(10000);
		lines("data/gz/mus_musculus_incl_consequences_1.gvf.gz", 173, ZipType.GZ);
	}
	
	/**
	 * Homo sapiens incl consequences 2 G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void homo_sapiens_incl_consequences2GZip() throws Exception {
		parter.getConfiguration().setPartitionLines(10000);
		lines("data/gz/homo_sapiens_incl_consequences_2.gvf.gz", 1, ZipType.GZ);
	}
	
	/**
	 * Homo sapiens incl consequences 3 G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void homo_sapiens_incl_consequences3GZip() throws Exception {
		parter.getConfiguration().setPartitionLines(100);
		lines("data/gz/homo_sapiens_incl_consequences_2.gvf.gz", 11, ZipType.GZ);
	}

	/**
	 * Hs 38 2 G zip.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void hs38_2GZip() throws Exception {
		parter.getConfiguration().setPartitionLines(100);
		lines("data/gz/hg38_2.gtf.gz", 7, ZipType.GZ);
	}

	/**
	 * Lines.
	 *
	 * @param path the path
	 * @param nfiles the nfiles
	 * @param count the count
	 * @return the list
	 * @throws Exception the exception
	 */
	private List<Path> lines(String path, int nfiles, int count) throws Exception {
		List<Path> paths = lines(path, nfiles, ZipType.NONE);
		int fcount = paths.stream().mapToInt(p->{
			try {
				return Files.readAllLines(p).size();
			} catch (IOException e) {
				fail(e.getMessage());
				return 0;
			}
		}).sum();
		assertEquals(count, fcount);
		Path source = super.getPath(path);
		assertEquals(Files.readAllLines(source).size(), fcount);
		return paths;
	}
	
	/**
	 * Lines.
	 *
	 * @param path the path
	 * @param nfiles the nfiles
	 * @param type the type
	 * @return the list
	 * @throws Exception the exception
	 */
	private List<Path> lines(String path, int nfiles, ZipType type) throws Exception {
		Path source = super.getPath(path);
		this.parter.getConfiguration().setZipType(type);
		
		List<Path> paths = new ArrayList<>(380);
		parter.partition(source, paths::add);

		assertEquals(nfiles, paths.size());
		
		if (type == ZipType.GZ) { // Check that they all end with .gz
			long nonGz = paths.stream().filter(p->!p.getFileName().toString().endsWith(".gz")).count();
			assertEquals(0, nonGz);
		}
		return paths;
	}
	
	/**
	 * Slow job test.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void slowJobTest() throws Exception {
		
		Path source = super.getPath("data/1000/hs_gtf/hg38_2.gtf");
		parter.getConfiguration().setZipType(ZipType.NONE);
		this.parter.getConfiguration().setPartitionLines(100);
		
		List<Path> paths = new ArrayList<>(380);
		parter.partition(source, p->{
			try {
				paths.add(p); 
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(7, paths.size());
		int fcount = paths.stream().mapToInt(p->{
			try {
				return Files.readAllLines(p).size();
			} catch (IOException e) {
				fail(e.getMessage());
				return 0;
			}
		}).sum();
		assertEquals(Files.readAllLines(source).size(), fcount);
		assertEquals(1064, fcount);
	}

	/**
	 * Parse every gene to a new file.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parseEveryGene1() throws Exception {
		parseEveryGene(getPath("data/1000/hs_gtf/hg38_2.gtf"), 60);
	}

	/**
	 * Parses the every gene 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void parseEveryGene2() throws Exception {
		parseEveryGene(getPath("data/1000/mm_gtf/mm10_2.gtf"), 55);
	}

	/**
	 * Parses the every gene.
	 *
	 * @param source the source
	 * @param hardCodedSize the hard coded size
	 * @throws PartitionException the partition exception
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseEveryGene(Path source, int hardCodedSize) throws PartitionException, InterruptedException, IOException {
		this.parter.getConfiguration().setZipType(ZipType.NONE);
		this.parter.getConfiguration().setPartitionLines(1); // So each gene in a new file.

		Map<String, Set<String>> table = new GeneTable(source.toFile(), " ").parse();
		
		List<Path> paths = new ArrayList<>();
		parter.partition(source, p->paths.add(p)); 
		
		// The first file is the comments which are separated
		// because the logic says > part size (1) and a gene
		// so the comments are written to a file then every Gene.
		assertEquals(table.size()+1, paths.size());
		assertEquals(hardCodedSize, paths.size());
	}
}
