package org.geneweaver.io.writer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Variant;
import org.geneweaver.io.DirectSave;
import org.geneweaver.io.IPrintStream;
import org.geneweaver.io.Timer;
import org.geneweaver.io.connector.PeakOverlapConnector;
import org.geneweaver.io.connector.RegulatoryFeatureOverlapConnector;
import org.geneweaver.io.connector.TranscriptOverlapConnector;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.junit.Ignore;
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
		assertTrue(Files.exists(dir.resolve("Gene-chr1.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Produces-header.csv")));
		assertTrue(Files.exists(dir.resolve("Produces-chr1.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Transcript-header.csv")));
		assertTrue(Files.exists(dir.resolve("Transcript-chr1.csv.gz")));
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
		assertTrue(Files.exists(dir.resolve("Variant-chr2.csv.gz")));
		assertTrue(Files.exists(dir.resolve("VariantEffect-header.csv")));
		assertTrue(Files.exists(dir.resolve("VariantEffect-chr2.csv.gz")));
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
		assertTrue(Files.exists(dir.resolve("Gene-chr1.csv.gz")));
		assertFalse(Files.exists(dir.resolve("Produces-header.csv")));
		assertFalse(Files.exists(dir.resolve("Produces-chr1.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Transcript-header.csv")));
		assertTrue(Files.exists(dir.resolve("Transcript-chr1.csv.gz")));

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
		assertTrue(Files.exists(dir.resolve("Variant-chr2.csv.gz")));
		assertFalse(Files.exists(dir.resolve("VariantEffect-header.csv")));
		assertFalse(Files.exists(dir.resolve("VariantEffect.csv.gz")));
	}

	private String exportNoConnector(Path dir, ExportBuilder b, Path input) throws Exception {
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
		try (DirectSave saver = new DirectSave((IPrintStream)null, false)){
			long saved = reader.stream()
							  .map(g->saver.save(g, b.getPaths(), b.getWriters(), dir, timer, false))
							  .count();
	
			return "Wrote bulk file(s) for '"+input.getFileName()+"' in "+timer.getFormattedTime()+" parsed "+saved+" objects.";
			
		} catch (ReaderException ne) {
			ne.printStackTrace();
			return "Cannot write bulk file(s) for '"+input.getFileName();
		}
	}
	
	@Test
	public void testHumanBedFiles() throws Exception {
		
		// recursively process all bed files.
		testBedExport("data/bed_peaks/homo_sapiens/", "-human");
	}
	
	@Test
	public void testMouseBedFiles() throws Exception {
		
		// recursively process all bed files.
		testBedExport("data/bed_peaks/mus_musculus/", "-mouse");
	}


	private void testBedExport(String spath, String dirEnding) throws Exception {
		
		Path bedDir = getPath(spath);
		List<Path> bfiles = new LinkedList<>();
		Files.walk(bedDir).forEach(f -> {
			if (f.getFileName().toString().toLowerCase().endsWith(".bed.gz")) {
				bfiles.add(f);
			}
		});
		
		Path dir = Paths.get("./tmp/testBedExport"+dirEnding);
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		try(ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
				   .setChunkProperty("1000")
				   .setDir(dir)
				   .setInputs(bfiles)
				   .setDefaultChunkSize(10000)
				   .setExporter((b,path)->exportNoConnector(dir, b, path))) {
			
			builder.export();
		}
		
		assertTrue(Files.exists(dir.resolve("Peak-header.csv")));
		for (int i = 1; i < 20; i++) {
			assertTrue(Files.exists(dir.resolve("Peak-chr"+i+".csv.gz")));
			assertTrue(Files.size(dir.resolve("Peak-chr"+i+".csv.gz"))>100);
		}
		assertTrue(Files.exists(dir.resolve("Peak-chrM.csv.gz")));
		assertTrue(Files.size(dir.resolve("Peak-chrM.csv.gz"))>100);
		assertTrue(Files.exists(dir.resolve("Peak-chrX.csv.gz")));
		assertTrue(Files.size(dir.resolve("Peak-chrX.csv.gz"))>100);
		assertTrue(Files.exists(dir.resolve("Peak-chrY.csv.gz")));
		assertTrue(Files.size(dir.resolve("Peak-chrY.csv.gz"))>100);
		
		// Bad names should not be there.
		assertFalse(Files.exists(dir.resolve("Peak-chr1_KI270713v1_random.csv.gz")));
		assertFalse(Files.exists(dir.resolve("Peak-chr14_GL000225v1_random.csv.gz")));
		assertFalse(Files.exists(dir.resolve("Peak-GL456372.1.csv.gz")));
		assertFalse(Files.exists(dir.resolve("Peak-JH584300.1.csv.gz")));
		assertFalse(Files.exists(dir.resolve("Peak-null.csv.gz")));
	}
	
	@Test
	public void testExportWithOverlaps() throws Exception {
		
		Path ppath = getPath("data/bed_peaks/some.bed");
		Path tpath = getPath("data/1000/hs_gtf/hg38_2.gtf");
		Path rpath = getPath("data/bed_peaks/some.gff");

		Path dir = Paths.get("./tmp/testBedExportWithOverlaps");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		try(ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
				   .setChunkProperty("1000")
				   .setDir(dir)
				   .setInput(ppath)
				   .setDefaultChunkSize(10000)) {
			
			builder.export();
		}
		assertTrue(Files.exists(dir.resolve("Peak-chr1.csv.gz")));
		assertTrue(Files.exists(dir.resolve("Peak-header.csv")));

		try(ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
				   .setChunkProperty("1000")
				   .setDir(dir)
				   .setInput(rpath)
				   .setDefaultChunkSize(10000)) {
			
			builder.export();
		}
		assertTrue(Files.exists(dir.resolve("RegulatoryFeature-chr1.csv.gz")));
		assertTrue(Files.exists(dir.resolve("RegulatoryFeature-header.csv")));
		assertFalse(Files.readString(dir.resolve("RegulatoryFeature-header.csv")).contains("(Rs-Id-null)"));

		// We make 23 copies of the input in order to test 
		// in parallel mode.
		Path vpath = getPath("data/bed_peaks/some.gvf");
		List<Path> copies = new LinkedList<>();
		for (int i = 0; i < 24; i++) {
			Path tmp = dir.resolve("copy"+i+".gvf");
			Files.copy(vpath, tmp);
			copies.add(tmp);
		}

		try (PeakOverlapConnector<Variant, Entity> pconn = new PeakOverlapConnector<>();
			 TranscriptOverlapConnector<Variant, Entity> tconn = new TranscriptOverlapConnector<>();
			 RegulatoryFeatureOverlapConnector<Variant, Entity> rconn = new RegulatoryFeatureOverlapConnector<>()) {
			pconn.setAllowNulls(true);     // Just for testing
			pconn.setAllowNoTissue(true);  // Just for testing
			pconn.setFrequency(100);
			pconn.setLocation(dir);
			pconn.add(ppath);
			pconn.create();
			
			tconn.setFrequency(100);
			tconn.setLocation(dir);
			tconn.add(tpath);
			tconn.create();
			
			rconn.setFrequency(100);
			rconn.setLocation(dir);
			rconn.add(rpath);
			rconn.create();

			try (@SuppressWarnings("resource")
				ExportBuilder builder = new ExportBuilder().setSpecies("Homo sapiens")
					   .setChunkProperty("1000")
					   .setAlwaysUseDefaultConnector(true)
					   .addConnector(pconn)
					   .addConnector(tconn)
					   .addConnector(rconn)
					   .setVerbose(true)
					   .setOut(System.out)
					   .setDir(dir)
					   .setInputs(copies) 
					   .setParallelFiles(true)
					   .setDefaultChunkSize(1000)) {
				
				builder.export();
			}
		}
		
		assertNumber(dir, "Variant-chr1.csv.gz", 815);
		assertNumber(dir, "PeakOverlap-chr1.csv.gz", 719); 
		assertNumber(dir, "TranscriptOverlap-chr1.csv.gz", 2399); 
		assertNumber(dir, "RegulatoryFeatureOverlap-chr1.csv.gz", 695);
		// TODO Some eQTL overlaps as well...

		assertTrue(Files.exists(dir.resolve("PeakOverlap-header.csv")));
		assertTrue(Files.exists(dir.resolve("TranscriptOverlap-header.csv")));
		assertFalse(Files.readString(dir.resolve("TranscriptOverlap-header.csv")).contains("(Rs-Id-null)"));
		assertTrue(Files.exists(dir.resolve("RegulatoryFeatureOverlap-header.csv")));
		assertTrue(Files.exists(dir.resolve("Peak-chr1.csv.gz")));
		assertTrue(Files.size(dir.resolve("Peak-chr1.csv.gz"))>100);
		assertTrue(Files.exists(dir.resolve("RegulatoryFeature-chr1.csv.gz")));
		assertTrue(Files.size(dir.resolve("RegulatoryFeature-chr1.csv.gz"))>100);
		assertTrue(Files.exists(dir.resolve("Peak-header.csv")));
		assertTrue(Files.exists(dir.resolve("VariantEffect-chr1.csv.gz")));
		assertTrue(Files.exists(dir.resolve("VariantEffect-header.csv")));
		assertFalse(Files.readString(dir.resolve("VariantEffect-header.csv")).contains("(Rs-Id-null)"));
	}
	
	private void assertNumber(Path dir, String name, int size) throws IOException, ReaderException {
		
		assertTrue(Files.exists(dir.resolve(name)));
		assertTrue(Files.size(dir.resolve(name))>100);
		ReaderRequest reader = new ReaderRequest("test", dir.resolve(name));
		reader.setReaderHint("MapCSVReader");		

		assertEquals(size, ReaderFactory.getReader(reader).stream().count());
	}

	@Ignore("This is the code for the full scale one")
	@Test
	public void testFullHumanMapping() throws Exception {
		
		Path hdir = Paths.get("/Volumes/Work/tmp/peaks/ftp.ensembl.org/pub/current_regulation/homo_sapiens");
		Path dir = Paths.get("./tmp/testLARGEWholeHumanPeaks");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		try (PeakOverlapConnector<Variant, Entity> conn = new PeakOverlapConnector<>()) {
			conn.setLocation(dir);
			conn.addAll(hdir);
			conn.create();
		}
	}
	
	@Ignore("This is the code for the full scale one")
	@Test
	public void testFullRegFeatureMapping() throws Exception {
		
		Path mdir = Paths.get("/Volumes/Work/JAX/repos/gweaver/gweaver-graph-build/build/ensembl-107/reg_features/mus_musculus/RegulatoryFeatureActivity");
		Path dir = Paths.get("./tmp/testLARGEWholeMouseRegFeatures");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();

		try (RegulatoryFeatureOverlapConnector<Variant, Entity> conn = new RegulatoryFeatureOverlapConnector<>()) {
			conn.setLocation(dir);
			conn.addAll(mdir);
			conn.create();
		}
	}

}
