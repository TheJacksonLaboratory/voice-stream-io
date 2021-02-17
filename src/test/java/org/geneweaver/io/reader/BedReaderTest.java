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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.geneweaver.domain.NamedEntity;
import org.geneweaver.domain.Region;
import org.geneweaver.domain.Region.Strand;
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
		check04998(lines);
	}

	private void check04998(List<NamedEntity> lines) {
		// line 0: chr1 959245 959305 NOC2L_1 900 - 959245 959256
		Region r0 = new Region("Homo sapiens", "chr1", 959245, 959305, "NOC2L_1", 900, Strand.REVERSE, 959245, 959256);
		assertEquals(r0, lines.get(0));

		// line 49: chr1 1727706 1727766 SLC35E2B_3 900 - 1727706 1727717
		Region r49 = new Region("Homo sapiens", "chr1", 1727706, 1727766, "SLC35E2B_3", 900, Strand.REVERSE, 1727706, 1727717);
		assertEquals(r49, lines.get(49));

		// line 98: chr1 3752400 3752460 CCDC27_1 900 + 3752449 3752460
		Region r98 = new Region("Homo sapiens", "chr1", 3752400, 3752460, "CCDC27_1", 900, Strand.FORWARD, 3752449, 3752460);
		assertEquals(r98, lines.get(98));	}

	@Test
	public void hg38gz() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz")));
		assertEquals(29598, reader.stream().count());	
	}
	
	@Test
	public void hg38First100gz() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz")));
		List<NamedEntity> lines = reader.stream().limit(100).collect(Collectors.toList());
		check04998(lines);
	}

	@Test
	public void simpleTrack() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = new BedReader<>().init(new ReaderRequest("Homo sapiens", getFile("data/bed/track1.bed")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Region).count());
	}

	@Test
	public void simpleGraphTrack() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/trackGraph1.bed")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(8, lines.stream().filter(e->e instanceof Region).count());
	}

	@Test
	public void enhancerTss() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/enhancer_tss_associations_hg38.bed.gz")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(0, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(66752, lines.stream().filter(e->e instanceof Region).count());
	}
	
	@Test
	public void unmappedLocations() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/unmapped_locations_hg38.bed.gz")));
		List<NamedEntity> lines = reader.stream().collect(Collectors.toList());

		assertEquals(0, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(190, lines.stream().filter(e->e instanceof Region).count());
	}

}
