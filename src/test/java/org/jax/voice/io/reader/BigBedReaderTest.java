package org.jax.voice.io.reader;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jetbrains.bio.big.BedEntry;
import org.jetbrains.bio.big.BigBedFile;
import org.junit.Test;

import gnu.trove.map.TIntObjectMap;

/**
 * TODO Use the BigBEd reader to process Peaks similar to how
 * the bed reader does already. Just need to decipher columns of BedEntry.rest
 */
public class BigBedReaderTest extends AbstractDataFileTest{

	
	@Test
	public void readBB() throws IOException {
		Path bb = getPath("data/bigbed/atac_seq_forebrain_m_11.5_d.bb");
		assertTrue(Files.exists(bb));
		
		try (BigBedFile file = BigBedFile.read(bb, null)) {
			
			System.out.println("BigBedFile: " + file);
			TIntObjectMap<String> hm = file.getChromosomes();
			
			for (String chromosome : hm.valueCollection()) {
				List<BedEntry> entries = file.query(chromosome);
				
				// Entry has start, end and rest which is a string something like: peak_17880	1000	.	511.489746	6.713345	3.790925	78
				System.out.println(entries.size());
	        }			
		}
	}
}
