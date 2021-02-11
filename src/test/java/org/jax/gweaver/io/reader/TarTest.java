package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.jax.gweaver.domain.Entity;
import org.junit.BeforeClass;
import org.junit.Test;


public class TarTest extends AbstractDataFileTest {

	@BeforeClass
	public static void makeTar() throws Exception {
		
		File dir = new File("./tmp/tarTest/");
		dir.mkdirs();
		
		// Output file stream
		FileOutputStream dest = new FileOutputStream(new File(dir, "test.tar"));

		// Create a TarOutputStream
		try (TarArchiveOutputStream out = new TarArchiveOutputStream(new BufferedOutputStream(dest))) {

			// Files to tar
			File[] filesToTar=new File[4];
			TarTest instance = new TarTest();
			filesToTar[0] = instance.getFile("data/eQTL/Brain_Substantia_nigra.v8.egenes.txt.gz");
			filesToTar[1] = instance.getFile("data/eQTL/Brain_Substantia_nigra.v8.signif_variant_gene_pairs.txt.gz");
			filesToTar[2] = instance.getFile("data/eQTL/Uterus.v8.egenes.txt.gz");
			filesToTar[3] = instance.getFile("data/eQTL/Uterus.v8.signif_variant_gene_pairs.txt.gz");
	
			for (File f : filesToTar) {
				out.putArchiveEntry(new TarArchiveEntry(f));
				try (BufferedInputStream in = new BufferedInputStream(new FileInputStream( f ))) {
					IOUtils.copy(in, out);
					out.flush();
				}
				out.closeArchiveEntry();
			}

		}
	}
	
	@Test
	public void checkTar() throws Exception {
		File tar = new File("./tmp/tarTest/test.tar");
		assertTrue(tar.exists());
		
		int nFiles = 0;
		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new FileInputStream(tar))) {
	        TarArchiveEntry entry;
	        while ((entry=tarInput.getNextTarEntry())!=null) {
	        	nFiles++;
	        }
		}
		assertEquals(4, nFiles);
	}
	
	@Test
	public void checkReadingGzipsUsingFactory() throws Exception {
		File tar = new File("./tmp/tarTest/test.tar");
		
		// We read each entry separately
		long total = 0;
		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new FileInputStream(tar))) {
	        TarArchiveEntry entry;
	        while ((entry=tarInput.getNextTarEntry())!=null) {
	        	
	        	StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(tarInput, entry.getName(), false));
	        	long lines = reader.stream().count();
	        	System.out.println("Lines in reader = "+lines);
	        	total+=lines;
	        }
		}
		
		// We read the tar as a stream using the reader
		StreamReader<Entity> reader = ReaderFactory.getReader(new ReaderRequest(tar));
		assertEquals(total, reader.stream().count());
	}
	
}
