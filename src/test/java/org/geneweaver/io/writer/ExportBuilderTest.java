package org.geneweaver.io.writer;


import static org.geneweaver.io.DirectSave.save;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.io.Timer;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.StreamReader;
import org.junit.Test;

public class ExportBuilderTest extends AbstractDataFileTest {

	@Test(expected=Exception.class)
	public void fileNotThere() throws Exception {
		
		Path geneFile = Paths.get("NOT_THERE");
		
		try(ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
				   .setInput(geneFile)) {
			
			builder.export();
		}
	}

	@Test
	public void geneDefaultExporter() throws Exception {
		
		Path dir = Paths.get("./tmp/exportTest1");
		dir.toFile().mkdirs();
		Path geneFile = getPath("data/1000/hs_gtf/hg38_2.gtf");
		
		try(ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
				   .setChunkProperty("1000")
				   .setDir(dir)
				   .setInput(geneFile)
				   .setDefaultChunkSize(10000)) {
			
			builder.export();
		}
		
		assertTrue(Files.exists(dir.resolve("Gene-header.csv")));
		assertTrue(Files.exists(dir.resolve("Gene.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Produces-header.csv")));
		assertTrue(Files.exists(dir.resolve("Produces.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Transcript-header.csv")));
		assertTrue(Files.exists(dir.resolve("Transcript.csv.gz")));
	}
	
	@Test
	public void variantDefaultExporter() throws Exception {
		
		Path dir = Paths.get("./tmp/exportTest2");
		dir.toFile().mkdirs();
		Path geneFile = getPath("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf");
		
		try(ExportBuilder builder = new ExportBuilder().setSpecies("Mus musculus")
				   .setChunkProperty("100000")
				   .setDir(dir)
				   .setInput(geneFile)
				   .setDefaultChunkSize(100000)) {
			
			builder.export();
		}
		
		assertTrue(Files.exists(dir.resolve("Variant-header.csv")));
		assertTrue(Files.exists(dir.resolve("Variant.csv.gz")));
		assertTrue(Files.exists(dir.resolve("VariantEffect-header.csv")));
		assertTrue(Files.exists(dir.resolve("VariantEffect.csv.gz")));
	}

	@Test
	public void geneCustomExporter() throws Exception {

		Path dir = Paths.get("./tmp/exportTest3");
		dir.toFile().mkdirs();
		Path geneFile = getPath("data/1000/hs_gtf/hg38_2.gtf");
		
		try(ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
				   .setChunkProperty("1000")
				   .setDir(dir)
				   .setInputs(Arrays.asList(geneFile))
				   .setDefaultChunkSize(10000)
				   .setExporter((b,path)->exportNoConnector(dir, b, path))) {
			
			builder.export();
		}
		
		assertTrue(Files.exists(dir.resolve("Gene-header.csv")));
		assertTrue(Files.exists(dir.resolve("Gene.csv.gz")));
		assertFalse(Files.exists(dir.resolve("Produces-header.csv")));
		assertFalse(Files.exists(dir.resolve("Produces.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Transcript-header.csv")));
		assertTrue(Files.exists(dir.resolve("Transcript.csv.gz")));

	}
	
	@Test
	public void variantCustomExporter() throws Exception {
		
		Path dir = Paths.get("./tmp/exportTest4");
		dir.toFile().mkdirs();
		Path geneFile = getPath("data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf");
		
		try(ExportBuilder builder = new ExportBuilder().setSpecies("Mus musculus")
				   .setChunkProperty("100000")
				   .setDir(dir)
				   .setInputs(Arrays.asList(geneFile))
				   .setDefaultChunkSize(100000)
				   .setExporter((b,path)->exportNoConnector(dir, b, path))) {
			
			builder.export();
		}
		
		assertTrue(Files.exists(dir.resolve("Variant-header.csv")));
		assertTrue(Files.exists(dir.resolve("Variant.csv.gz")));
		assertFalse(Files.exists(dir.resolve("VariantEffect-header.csv")));
		assertFalse(Files.exists(dir.resolve("VariantEffect.csv.gz")));
	}

	private String exportNoConnector(Path dir, ExportBuilder b, Path input) {
	    StreamReader<Entity> reader;
		try {
			reader = b.createReader(input);
		} catch (ReaderException e) {
			System.out.println("Cannot write bulk file(s) for '"+input.getFileName());
			e.printStackTrace();
			return e.getMessage();
		}

		Timer timer = b.createTimer();
		
		// Directly saving the streams with no chunks is fast.
		try {
			long saved = reader.stream()
							  .map(g->save(g, b.getWriters(), dir, timer))
							  .count();
	
			return "Wrote bulk file(s) for '"+input.getFileName()+"' in "+timer.getFormattedTime()+" parsed "+saved+" objects.";
			
		} catch (ReaderException ne) {
			ne.printStackTrace();
			return "Cannot write bulk file(s) for '"+input.getFileName();
		}
	}
}
