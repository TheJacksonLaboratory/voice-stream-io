package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Homolog;
import org.jax.voice.domain.HomologGene;
import org.jax.voice.io.DirectSave;
import org.jax.voice.io.Timer;
import org.jax.voice.io.connector.HomologFunction;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.jax.voice.io.reader.StreamReader;
import org.jax.voice.io.writer.ExportBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class HomologFunctionTest extends AbstractDataFileTest {

	
	@Test(expected=FileNotFoundException.class)
	public void badGeneFile1() throws Exception {
		Path hFile = getPath("data/NOTTHERE");
		try (HomologFunction<HomologGene, HomologGene> func = new HomologFunction<>()) {
			func.add(9606, hFile);
		}
	}
	
	@Test(expected=FileNotFoundException.class)
	public void badGeneFile2() throws Exception {
		Path mFile = getPath("data/1000/mm_gtf/mm10_2.gtf");
		Path hFile = getPath("data/NOTTHERE");
		try (HomologFunction<HomologGene, HomologGene> func = new HomologFunction<>()) {
			func.add(10090, mFile);
			func.add(9606, hFile);
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void noAdd() throws Exception {
		try (HomologFunction<HomologGene, HomologGene> func = new HomologFunction<>()) {
			func.create(); // Creates indexed database.
		}
	}
	
	@Test
	public void exists() throws Exception {

		Path dir = Paths.get("./tmp/homologFunctionTestExists");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();
		
		Path hFile = getPath("data/1000/hs_gtf/hg38_2.gtf");
		Path mFile = getPath("data/1000/mm_gtf/mm10_2.gtf");
		try (HomologFunction<HomologGene, HomologGene> func = new HomologFunction<>()) {
			func.add(9606, hFile);
			func.add(10090, mFile);
			func.setLocation(dir);
			func.create(); // Creates indexed database.
		}

		try (HomologFunction<HomologGene, HomologGene> func = new HomologFunction<>()) {
			func.setLocation(dir);
			assertTrue(func.exists()); 
		}
	}
	
	@Ignore
	@Test
	public void fullSize() throws Exception {
		
		Path hFile = Paths.get("/Volumes/jax-data/data/variant-orthology/ensembl-102/Homo_sapiens.GRCh38.102.gtf.gz");
		Path mFile = Paths.get("/Volumes/jax-data/data/variant-orthology/ensembl-102/Mus_musculus.GRCm38.102.gtf.gz");
		Path homologene = Paths.get("/Volumes/jax-data/data/variant-orthology/ensembl-102/HOM_MouseHumanSequence.rpt");
		
		try (HomologFunction<HomologGene, HomologGene> func = new HomologFunction<>(homologene.getFileName().toString());
			 ExportBuilder builder = new ExportBuilder()) {
			
			//func.setNewDatabase(true); // Do not use cached database.
			func.add(9606, hFile);
			func.add(10090, mFile);
			func.create(); // Creates indexed database.
			
			builder.setInput(homologene);
			builder.setSpecies("Homo sapiens");
			builder.setDir(hFile.getParent().resolve("mm"));
			builder.setExporter((build, path)->exportHomologs(build, path, func));
			
			// Run it!
			builder.export();
		}
	}

	private String exportHomologs(ExportBuilder builder, Path input, HomologFunction<HomologGene, HomologGene> func) throws Exception {
		
		StreamReader<HomologGene> reader = builder.createReader(input);
		Function<HomologGene,Stream<Entity>> connector = reader.getDefaultConnector();
		try (DirectSave saver = new DirectSave(System.out, true)) {
			Timer timer = builder.createTimer();
			long saved = reader.stream()
								.map(h->func.apply(h)) // Around 520 of the 10's of k cannot be mapped
								.filter(h->h.getGeneId()!=null)
								.flatMap(h->connector.apply(h))
								.filter(e->e instanceof Homolog)
								.map(g->saver.save(g, builder.getPaths(), builder.getWriters(), builder.getDir(), timer, false))
								.count();
			assertEquals(17087, saved);
			return "Wrote homologene import for '"+input.getFileName()+"' in "+timer.getFormattedTime()+" parsed "+saved+" objects.";
		}
		
	}
}
