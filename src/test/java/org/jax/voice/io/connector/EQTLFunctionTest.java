/*-
 * 
 * Copyright 2018, 2020  The Jackson Laboratory Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Matthew Gerring
 */
package org.jax.voice.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.jax.voice.domain.EQTL;
import org.jax.voice.io.reader.AbstractDataFileTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EQTLFunctionTest extends AbstractDataFileTest {
	
	private Path dir;
	
	@After
	@Before
	public void delete() throws Exception {
		this.dir = Paths.get("./tmp/eqtlFunctionTest");
		FileUtils.deleteQuietly(dir.toFile());
		dir.toFile().mkdirs();
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void mappingStreamTest() throws Exception {
		checkMappingStream(new EQTLFunction<>(getFile("data/eQTL/GTExLookup-frag.lookup_table.txt"), getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz")));
		checkMappingStream(new EQTLFunction<>(getFile("data/eQTL/GTExLookup-frag.lookup_table.txt").toURL(), getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz").toURL()));
	}

	private void checkMappingStream(EQTLFunction<EQTL, EQTL> func) throws IOException {
		func.setLocation(dir);
		try (InputStream in = func.stream(func.getMapping()) ) {
			assertNotNull(in);
		}
	}

	@Test
	public void noMap() throws Exception {
		File fmap = getFile("data/eQTL/GTExLookup-frag.lookup_table.txt");
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap, getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz"))) {
			func.setLocation(dir);
			assertFalse(func.exists());
		}
	}
	
	@Test
	public void testMap() throws Exception {
		
		File fmap = getFile("data/eQTL/GTExLookup-frag.lookup_table.txt");
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap, getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz"))) {
			func.setLocation(dir);
			
			// We parse the database and create it
			func.create();
			
			assertTrue(func.exists());
			assertEquals(999, func.size());
		}
	}
	
	@Test
	public void testFunction() throws Exception {
		
		File fmap = getFile("data/eQTL/GTExLookup-frag.lookup_table.txt");
		File attributes = getFile("data/eQTL/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt.gz");
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap, attributes)) {
			func.setLocation(dir);
			
			// We parse the database and create it
			func.create();
			
			testEQTL(func,"chr1_14673_G_C_b38", "rs4690");
			testEQTL(func,"chr1_595505_GCTTCCCAGCAGTGCAGA_G_b38", "rs1314538055");
			testEQTL(func,"chr1_632946_G_A_b38", "rs1463015995");
		}
	}

	
	private void testEQTL(EQTLFunction<EQTL, EQTL> func, String variantId, String rsId) {
		EQTL eqtl = new EQTL("test", variantId, null); 
		eqtl.setSpecies(10090);
		assertNull(eqtl.getRsId());
		func.apply(eqtl);
		assertEquals(rsId, eqtl.getRsId());
	}

}
