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
package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.NamedEntity;
import org.geneweaver.domain.Produces;
import org.geneweaver.domain.Peak;
import org.geneweaver.domain.Peak.Strand;
import org.geneweaver.domain.Track;
import org.geneweaver.domain.Tracked;
import org.geneweaver.domain.VariantEffect;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.geneweaver.io.reader.BedReader;
import org.geneweaver.io.reader.LineIteratorReader;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TrackConnectorTest extends AbstractDataFileTest {

	private BedConnector<NamedEntity, Entity> connector;
	
	@Before
	public void before() throws Exception {
		connector = new BedConnector<>();
	}
	
	@After
	public void after() throws Exception {
		connector = null;
	}

	@Test(expected=NullPointerException.class)
	public void nullException() {
		connector.stream(null);
	}
	
	@Test(expected=ConnectorException.class)
	public void noNameException() {
		connector.stream(new Track());
	}
	
	@Test(expected=ConnectorException.class)
	public void notATrackOrRegionException() {
		connector.stream(new NamedEntity());
	}
	
	@Test
	public void hg38() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed")));
		assertEquals(29598, reader.stream().flatMap(b->connector.apply(b)).count());	
	}
	
	@Test
	public void hg38First100() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed")));
		List<Entity> lines = reader.stream().flatMap(b->connector.apply(b)).limit(100).collect(Collectors.toList());
		check04998("Hs_EPDnew_006_hg38.bed", lines);
	}

	private void check04998(final String fileName, List<Entity> lines) {
		// line 0: chr1 959245 959305 NOC2L_1 900 - 959245 959256
		String peakId = BedReader.createPeakId(null, "chr1", null, 959245, 959305, 0, true);
		Peak r0 = new Peak(peakId, "Homo sapiens", "chr1", 959245, 959305, "NOC2L_1", 900, Strand.REVERSE, 959245, 959256);
		assertEquals(r0, lines.get(0));

		// line 49: chr1 1727706 1727766 SLC35E2B_3 900 - 1727706 1727717
		peakId = BedReader.createPeakId(null, "chr1", null, 1727706, 1727766, 49, true);
		Peak r49 = new Peak(peakId, "Homo sapiens", "chr1", 1727706, 1727766, "SLC35E2B_3", 900, Strand.REVERSE, 1727706, 1727717);
		assertEquals(r49, lines.get(49));

		// line 98: chr1 3752400 3752460 CCDC27_1 900 + 3752449 3752460
		peakId = BedReader.createPeakId(null, "chr1", null, 3752400, 3752460, 98, true);
		Peak r98 = new Peak(peakId, "Homo sapiens", "chr1", 3752400, 3752460, "CCDC27_1", 900, Strand.FORWARD, 3752449, 3752460);
		assertEquals(r98, lines.get(98));
	}

	@Test
	public void hg38gz() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz")));
		assertEquals(29598, reader.stream().flatMap(b->connector.apply(b)).count());	
	}
	
	@Test
	public void hg38First100gz() throws Exception {
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz")));
		List<Entity> lines = reader.stream().flatMap(b->connector.stream(b)).limit(100).collect(Collectors.toList());
		check04998("Hs_EPDnew_006_hg381.bed.gz", lines);
	}

	@Test
	public void simpleTrack() throws ReaderException, IOException {
		
		LineIteratorReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/track1.bed")));
		List<Entity> lines = reader.stream().flatMap(b->connector.apply(b)).collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Peak).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Tracked).count());
		assertEquals(0, lines.stream().filter(e->e instanceof Produces).count());
	}

	@Test
	public void simpleTrackFromReader() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/track1.bed")));
		this.connector = (BedConnector<NamedEntity, Entity>)reader.getDefaultConnector();
		List<Entity> lines = reader.stream().flatMap(b->connector.apply(b)).collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Peak).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Tracked).count());
		assertEquals(0, lines.stream().filter(e->e instanceof Produces).count());
	}

	@Test
	public void simpleGraphTrack() throws ReaderException, IOException {
		
		StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/bed/trackGraph1.bed")));
		List<Entity> lines = reader.stream().flatMap(b->connector.stream(b, null)).collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(8, lines.stream().filter(e->e instanceof Peak).count());
		assertEquals(8, lines.stream().filter(e->e instanceof Tracked).count());
		assertEquals(0, lines.stream().filter(e->e instanceof VariantEffect).count());
	}

}
