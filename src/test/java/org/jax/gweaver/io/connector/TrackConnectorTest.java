package org.jax.gweaver.io.connector;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.Gene;
import org.jax.gweaver.domain.NamedEntity;
import org.jax.gweaver.domain.Produces;
import org.jax.gweaver.domain.Region;
import org.jax.gweaver.domain.Region.Strand;
import org.jax.gweaver.domain.Track;
import org.jax.gweaver.domain.Tracked;
import org.jax.gweaver.domain.Variant;
import org.jax.gweaver.domain.VariantEffect;
import org.jax.gweaver.io.reader.AbstractDataFileTest;
import org.jax.gweaver.io.reader.AbstractReader;
import org.jax.gweaver.io.reader.BedReader;
import org.jax.gweaver.io.reader.ReaderException;
import org.jax.gweaver.io.reader.ReaderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TrackConnectorTest extends AbstractDataFileTest {

	private TrackConnector connector;
	
	@Before
	public void before() throws Exception {
		connector = new TrackConnector();
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
		AbstractReader<NamedEntity> reader = ReaderFactory.getReader("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed"));
		assertEquals(29598, reader.stream().flatMap(b->connector.apply(b)).count());	
	}
	
	@Test
	public void hg38First100() throws Exception {
		AbstractReader<NamedEntity> reader = new BedReader<>("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg38.bed"));
		List<Entity> lines = reader.stream().flatMap(b->connector.apply(b)).limit(100).collect(Collectors.toList());
		check04998(lines);
	}

	private void check04998(List<Entity> lines) {
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
		AbstractReader<NamedEntity> reader = ReaderFactory.getReader("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz"));
		assertEquals(29598, reader.stream().flatMap(b->connector.apply(b)).count());	
	}
	
	@Test
	public void hg38First100gz() throws Exception {
		AbstractReader<NamedEntity> reader = ReaderFactory.getReader("Homo sapiens", getFile("data/bed/Hs_EPDnew_006_hg381.bed.gz"));
		List<Entity> lines = reader.stream().flatMap(b->connector.stream(b)).limit(100).collect(Collectors.toList());
		check04998(lines);
	}

	@Test
	public void simpleTrack() throws ReaderException, IOException {
		
		AbstractReader<NamedEntity> reader = new BedReader<>("Homo sapiens", getFile("data/bed/track1.bed"));
		List<Entity> lines = reader.stream().flatMap(b->connector.apply(b)).collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Region).count());
		assertEquals(9, lines.stream().filter(e->e instanceof Tracked).count());
		assertEquals(0, lines.stream().filter(e->e instanceof Produces).count());
	}

	@Test
	public void simpleGraphTrack() throws ReaderException, IOException {
		
		AbstractReader<NamedEntity> reader = ReaderFactory.getReader("Homo sapiens", getFile("data/bed/trackGraph1.bed"));
		List<Entity> lines = reader.stream().flatMap(b->connector.stream(b, null)).collect(Collectors.toList());

		assertEquals(1, lines.stream().filter(e->e instanceof Track).count());
		assertEquals(8, lines.stream().filter(e->e instanceof Region).count());
		assertEquals(8, lines.stream().filter(e->e instanceof Tracked).count());
		assertEquals(0, lines.stream().filter(e->e instanceof VariantEffect).count());
	}

}
