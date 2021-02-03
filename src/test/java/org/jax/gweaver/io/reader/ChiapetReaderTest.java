package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertEquals;

import org.jax.gweaver.domain.Anchor;
import org.jax.gweaver.domain.ChromatinInteraction;
import org.jax.gweaver.domain.ExperimentMetadata;
import org.junit.Test;

public class ChiapetReaderTest extends AbstractDataFileTest {

	@Test
	public void countSheet1() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = new ChiapetReader<>(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(1);
		reader.setConcreteClass(Anchor.class);
		assertEquals(11162-3, reader.stream().count());	
	}

	@Test
	public void countSheet2() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(2);
		reader.setConcreteClass(Anchor.class);
		assertEquals(14607-3, reader.stream().count());	
	}

	@Test
	public void countSheet3() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = new ChiapetReader<>(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(3);
		reader.setConcreteClass(Anchor.class);
		assertEquals(28977-3, reader.stream().count());	
	}

	@Test
	public void countSheet4() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(4);
		reader.setConcreteClass(Anchor.class);
		assertEquals(30588-3, reader.stream().count());	
	}

	@Test
	public void countSheet5() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(5);
		assertEquals(13285-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet6() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = new ChiapetReader<>(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(6);
		assertEquals(19860-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet7() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(7);
		assertEquals(65000-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet8() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(8);
		assertEquals(61894-4, reader.stream().count());	
	}
	
	@Test
	public void countSheet9() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(9);
		assertEquals(50060-4, reader.stream().count());	
	}
	
	@Test
	public void checkMetaReferenceInObjects() throws Exception {
		AbstractXlsReader<ChromatinInteraction, ExperimentMetadata> reader = new ChiapetReader<>(new ReaderRequest("Homo sapiens", getFile("data/ChIA-PET/NIHMS345629-supplement-02.xls.gz")));
		reader.setSheetIndex(9);
		reader.setMeta(new ExperimentMetadata("MCF7", "breast", "Polr2a", "ChIA-PET", "PMID:22265404"));

		assertEquals(50060-4, reader.stream().filter(ci->ci.getMeta()!=null).count());	
	}

}
