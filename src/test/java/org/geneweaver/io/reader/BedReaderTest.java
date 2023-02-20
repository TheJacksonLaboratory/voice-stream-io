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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geneweaver.domain.NamedEntity;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.Peak.Strand;
import org.geneweaver.domain.Track;
import org.junit.Test;

public class BedReaderTest extends AbstractDataFileTest {

	
	@Test
	public void chunkSize() throws Exception {
		
		LineIteratorReader<NamedEntity> reader = new BedReader<>().init(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed")));
		assertEquals(4096, reader.getChunkSize());
	}

	@Test
	public void hg38() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed")));
		assertEquals(29598, reader.stream().count());	
	}
	
	@Test
	public void hg38First100() throws Exception {
		StreamReader<NamedEntity> reader = new BedReader<>().init(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed")));
		List<NamedEntity> lines = reader.stream().limit(100).collect(Collectors.toList());
		check04998("Hs_EPDnew_006_hg38.bed", lines);
	}

	private void check04998(String fileName, List<NamedEntity> lines) {
		// line 0: chr1 959245 959305 NOC2L_1 900 - 959245 959256
		String peakId = BedReader.createPeakId(null, "chr1", null, 959245, 959305, true);
		Peak r0 = new Peak(peakId, "Homo sapiens", "chr1", 959245, 959305, "NOC2L_1", 900, Strand.REVERSE, 959245, 959256);
		assertEquals(r0, lines.get(0));

		// line 49: chr1 1727706 1727766 SLC35E2B_3 900 - 1727706 1727717
		peakId = BedReader.createPeakId(null, "chr1", null, 1727706, 1727766, true);
		Peak r49 = new Peak(peakId, "Homo sapiens", "chr1", 1727706, 1727766, "SLC35E2B_3", 900, Strand.REVERSE, 1727706, 1727717);
		assertEquals(r49, lines.get(49));

		// line 98: chr1 3752400 3752460 CCDC27_1 900 + 3752449 3752460
		peakId = BedReader.createPeakId(null, "chr1", null, 3752400, 3752460, true);
		Peak r98 = new Peak(peakId, "Homo sapiens", "chr1", 3752400, 3752460, "CCDC27_1", 900, Strand.FORWARD, 3752449, 3752460);
		assertEquals(r98, lines.get(98));
	}

	@Test
	public void hg38gz() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz")));
		assertEquals(29598, reader.stream().count());	
	}
	
	@Test
	public void hg38First100gz() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz")));
		List<NamedEntity> lines = reader.stream().limit(100).collect(Collectors.toList());
		check04998("Hs_EPDnew_006_hg381.bed.gz", lines);
	}

	@Test
	public void simpleTrack() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = new BedReader<>().init(new ReaderRequest("Homo sapiens", getFile("data/bed/track1.bed")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Peak).count());
	}

	@Test
	public void simpleGraphTrack() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/trackGraph1.bed")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(8, lines.stream().filter(e->e instanceof Peak).count());
	}

	@Test
	public void enhancerTss() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/enhancer_tss_associations_hg38.bed.gz")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(0, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(66752, lines.stream().filter(e->e instanceof Peak).count());
	}
	
	@Test
	public void unmappedLocations() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/unmapped_locations_hg38.bed.gz")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(0, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(190, lines.stream().filter(e->e instanceof Peak).count());
	}

	@Test
	public void peaksOneFileHomoSap() throws Exception {
		StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", 
															getPath("data/bed_peaks/homo_sapiens/A549/BCL3/homo_sapiens.GRCh38.A549.BCL3.SWEmbl_R0005.peaks.20210107.bed.gz")));
		List<Peak> peaks = reader.stream().collect(Collectors.toList());
		assertEquals(5737, peaks.size());	
		peaks.stream().allMatch(p->"A549".equals(p.getEpigenome()));
		peaks.stream().allMatch(p->"BCL3".equals(p.getFeatureType()));
	}
	
	@Test
	public void peaksOneMusMus() throws Exception {
		StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", 
				getPath("data/bed_peaks/mus_musculus/CH12_LX/BHLHE40/mus_musculus.GRCm39.CH12_LX.BHLHE40.SWEmbl_R0005.peaks.20201021.bed.gz")));
		List<Peak> peaks = reader.stream().collect(Collectors.toList());
		assertEquals(33350, peaks.size());	
		peaks.stream().allMatch(p->"CH12_LX".equals(p.getEpigenome()));
		peaks.stream().allMatch(p->"BHLHE40".equals(p.getFeatureType()));
	}

	@Test
	public void allTestPeaksHomoSap() throws Exception {
		testDir("Homo sapiens", getPath("data/bed_peaks/homo_sapiens/"));
	}

	@Test
	public void allTestPeaksMusMus() throws Exception {
		testDir("Mus musculus", getPath("data/bed_peaks/mus_musculus/"));
	}

	private void testDir(String sname, Path sdir) throws IOException {
		Files.list(sdir).forEach(edir->{
			if (!Files.isDirectory(edir)) return;
			try {
				testFeatureDir(sname, edir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void testFeatureDir(String sname, Path edir) throws IOException {
		Files.list(edir).forEach(fdir->{
			if (!Files.isDirectory(fdir)) return;
			try {
				testDir(sname, edir.getFileName().toString(), fdir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void testDir(String sname, String ename, Path fdir) throws IOException {
		// In Ensembl dirs are named <Epigenome>/<FeatureType>/*.bed.gz
		String fname = fdir.getFileName().toString();
		
		Files.list(fdir).forEach(bed -> {
			if (Files.isDirectory(bed)) return;
			if (!bed.getFileName().toString().endsWith(".bed.gz")) return;
			try {
				StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest(sname, bed));
				List<Peak> peaks = reader.stream().collect(Collectors.toList());
				peaks.stream().allMatch(p->ename.equals(p.getEpigenome()));
				peaks.stream().allMatch(p->fname.equals(p.getFeatureType()));
				
				List<Peak> withDes = peaks.stream().filter(p->p.getTissueDescription()!=null).collect(Collectors.toList());
				if (withDes.size() <= 0) fail("Could not find description for "+ename);
				
				boolean withChromo = peaks.stream().allMatch(p->p.getChr()!=null);
				if (!withChromo) fail("Some peaks do not have chromosomes!");

				System.out.println("Tested: "+bed);
			} catch (ReaderException | IOException e) {
				throw new RuntimeException(e);
			}

		});
	}
	
	@Test
	public void testHomoSapDescriptions() throws Exception {
		BedReader<Peak> reader = new BedReader<>();
		Map<String,String> des = reader.getEpigenomeDescriptions("Homo sapiens");
		assertNotNull(des);
		des.keySet().forEach(key->{
			assertTrue(key+" has unexpected characters", key.matches("[a-z0-9]+"));
		});
		
		String descr = des.get(BedReader.getKey("GM18505"));
		String expected = "B-lymphoblastoid cell line, International HapMap Project, Yoruba in Ibadan, Nigera, treatment: Epstein-Barr Virus transformed";
		assertEquals(expected, descr);
		
		descr = des.get(BedReader.getKey("EM CD4+ ab T (PB)"));
		expected = "Roadmap Epigenomics Mapping Consortium Epigenome (Class 5) for Primary T Cells Effector/Memory Enriched from Peripheral Blood using donors/samples:62;332";
		assertEquals(expected, descr);

	}
	
	@Test
	public void testMusMusDescriptions() throws Exception {
		BedReader<Peak> reader = new BedReader<>();
		Map<String,String> des = reader.getEpigenomeDescriptions("Mus musculus");
		assertNotNull(des);
		des.keySet().forEach(key->{
			assertTrue(key+" has unexpected characters", key.matches("[a-z0-9]+"));
		});
		
		String descr = des.get(BedReader.getKey("thymus adult"));
		String expected = "Mouse thymus from adult 8 weeks";
		assertEquals(expected, descr);
		
		descr = des.get(BedReader.getKey("lung E15.5"));
		expected = "Mouse lung from embryonic 15.5 days";
		assertEquals(expected, descr);

	}

	@Test
	public void idsReproducible1() throws Exception {
		StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", 
										getPath("data/bed_peaks/homo_sapiens/A549/BCL3/homo_sapiens.GRCh38.A549.BCL3.SWEmbl_R0005.peaks.20210107.bed.gz")));
		idsReproducible(reader);
	}
	
	@Test
	public void idsReproducible2() throws Exception {
		StreamReader<Peak> reader = ReaderFactory.getReader(new ReaderRequest("Mus musculus", 
										getPath("data/bed_peaks/mus_musculus/CH12_LX/BHLHE40/mus_musculus.GRCm39.CH12_LX.BHLHE40.SWEmbl_R0005.peaks.20201021.bed.gz")));
		idsReproducible(reader);
	}

	private void idsReproducible(StreamReader<Peak> reader) throws ReaderException {
		
		List<String> idsPass1 = reader.stream().map(p->p.getPeakId().toString()).collect(Collectors.toList());
		List<String> idsPass2 = reader.stream().map(p->p.getPeakId().toString()).collect(Collectors.toList());
		assertEquals(idsPass1, idsPass2);
		
		// Make sure no duplicates
		Set<String> unique = new HashSet<>(idsPass1);
		assertEquals(idsPass1.size(), unique.size());
	}

}
