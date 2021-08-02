package org.geneweaver.io.reader;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.geneweaver.domain.Ortholog;
import org.junit.Test;

public class OrthologBaylorReaderTest extends AbstractDataFileTest {

	
	@Test
	public void simpleParse() throws Exception {
		File file = getFile("prod/hom/mouse_human_mapping_balyor.csv");
		StreamReader<Ortholog> reader = ReaderFactory.getReader(new ReaderRequest(file));
		long size = reader.stream().count();
		assertTrue(size>40000);
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void parseWrongFormat() throws Exception {
		File file = getFile("prod/eQTL/Aging_Bone_DO.csv.gz");
		StreamReader<Ortholog> reader = new OrthologBaylorReader<Ortholog>();
		reader.init(new ReaderRequest(file));
		long size = reader.stream().count();
		assertTrue(size>100);
	}
	
	@Test(expected=ReaderException.class)
	public void notThere1() throws Exception {
		File file = new File("NOT_THERE");
		StreamReader<Ortholog> reader = new OrthologBaylorReader<Ortholog>();
		reader.init(new ReaderRequest(file));
		long size = reader.stream().count();
		assertTrue(size>100);
	}
	
	@Test(expected=ReaderException.class)
	public void notThere2() throws Exception {
		File file = new File("NOT_THERE_balyor.csv");
		StreamReader<Ortholog> reader = ReaderFactory.getReader(new ReaderRequest(file));
		long size = reader.stream().count();
		assertTrue(size>100);
	}

}
