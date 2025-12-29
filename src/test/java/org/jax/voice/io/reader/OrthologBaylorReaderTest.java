package org.jax.voice.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.jax.voice.domain.Ortholog;
import org.jax.voice.io.reader.OrthologBaylorReader;
import org.jax.voice.io.reader.ReaderException;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;
import org.junit.Ignore;
import org.junit.Test;

public class OrthologBaylorReaderTest extends AbstractDataFileTest {

	
	@Test
	public void simpleParse() throws Exception {
		File file = getFile("prod/hom/mouse_human_mapping_balyor.csv");
		StreamReader<Ortholog> reader = ReaderFactory.getReader(new ReaderRequest(file));
		long size = reader.stream().count();
		assertTrue(size>40000);
	}
	
	@Test
	public void parseWrongFormat() throws Exception {
		File file = getFile("prod/eQTL/mm/Aging_Bone_DO.csv.gz");
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

	@Test
	public void aonMappings() throws Exception {
		Path aon = getPath("prod/hom/aon-mappings.csv");
		ReaderRequest req = new ReaderRequest(aon.toFile());
		req.setReaderHint("OrthologBaylorReader");
		StreamReader<Ortholog> reader = ReaderFactory.getReader(req);
		
		Stream<Ortholog> stream = reader.stream().filter(o->{
			boolean from = o.getGeneIdFrom()!=null && !o.getGeneIdFrom().isBlank();
			boolean to = o.getGeneIdTo()!=null && !o.getGeneIdTo().isBlank();
			return from && to;
		});

		long size = stream.count();
		assertEquals(48242, size);
		
	}

	@Ignore("These lines are valid however they break neo4j. Therefore we will remove them from the Ortholog stream.")
	@Test
	public void aonMappingsCheckColumns() throws Exception {
		Path aon = getPath("prod/hom/aon-mappings.csv");
        try (BufferedReader reader = Files.newBufferedReader(aon)) {
			boolean ok = reader.lines().allMatch(line -> {
				String[] cols = line.split(",");
				if (cols.length != 6) {
                    System.out.println("Wrong number of columns: "+line);
				}
				if (cols[0].isBlank()) {
                    System.out.println("No symbol: "+line);
				}
				return !cols[0].isBlank() && cols.length == 6;
			});
			assertTrue(ok);
        }
	}
}
