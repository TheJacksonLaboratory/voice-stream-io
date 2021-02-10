package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertEquals;

import org.jax.gweaver.domain.Fantom5Link;
import org.junit.Test;

public class Fantom5EnsemblMapReaderTest extends AbstractDataFileTest {

	
	@Test
	public void chunkSize() throws Exception {
		
		Fantom5EnsemblMapReader<Fantom5Link> reader = new Fantom5EnsemblMapReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(4096, reader.getChunkSize());
	}

	@Test
	public void parseSimpleTestFile1() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(48700-5, reader.stream().count());
	}

	@Test
	public void countGeneReferences() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(5084105, reader.stream().mapToInt(f->f.getEnsemblIds().size()).sum());
	}

	@Test
	public void countObjectsWithSpanGt5000() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(48592, reader.stream().mapToInt(f->f.span()).filter(i->i>5000).count());
	}

	@Test
	public void countObjectsWithSpanGt100000() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(36332, reader.stream().mapToInt(f->f.span()).filter(i->i>100000).count());
	}

	@Test
	public void countObjectsWithSpanGt1000000() throws Exception {
		
		LineIteratorReader<Fantom5Link> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/fantom5/fantom5-ensembl-map.tsv.gz")));
		assertEquals(43, reader.stream().mapToInt(f->f.span()).filter(i->i>1000000).count());
	}

}
