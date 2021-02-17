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
package org.geneweaver.io.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geneweaver.domain.EQTL;
import org.geneweaver.io.reader.AbstractDataFileTest;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;
import org.geneweaver.io.reader.StreamUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @see EQTLFunctionTest
 * 
 * @author gerrim
 *
 */
@Ignore
public class EQTLFunctionMassiveTest extends AbstractDataFileTest {
	
	private Path dir;
	
	// The hard coded local input file. This test is used to check
	// performance for big stuff, not be a unit test. @see EQTLFunctionTest
	private File fmap = new File("/Volumes/jax-data/data/variant-orthology/eQTL/v8/GTEx_Analysis_2017-06-05_v8_WholeGenomeSeq_838Indiv_Analysis_Freeze.lookup_table.txt.gz");

	@Before
	public void create() throws Exception {
		
		this.dir = Paths.get("./tmp/eqtlFunctionMassiveTest");
		dir.toFile().mkdirs();
		
		// Create the database if it is not already there.
		// It is 6Gb - do not run this test unless you know what you are doing.
		// The database is not recreated if it exists.
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap)) {
			func.setLocation(dir);
			
			// We parse the database and create it
			func.create();
		}
	}
	

	/**
	 * @throws Exception
	 */
	@Test
	public void timeMassiveMapFile() throws Exception {
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap)) {
			func.setLocation(dir);
			
			assertTrue(func.exists());
			assertEquals(46569704, func.size());
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void timeMassive1000Lookups() throws Exception {
				
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap)) {
			func.setLocation(dir);
			
			// If the index on variantId is not working, this is very slow.
			Map<String,String> test = readTestMappings(getFile("data/eQTL/last1000Lookups.txt"));
			long start = System.currentTimeMillis();
			test.forEach((k,v)->testEQTL(func,k,v));
			long end = System.currentTimeMillis();
			System.out.println("Looking up "+test.size()+" in full file takes "+(end-start)+"ms");
		}
	}
	
	/**
	 * This test passes with 72686455 objects with an rsId.
	 * @throws Exception
	 */
	@Test
	public void testMassiveAllLookups() throws Exception {
				
		try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(fmap)) {
			func.setLocation(dir);
			
			// If the index on variantId is not working, this is very slow.
			File bigTar = new File("/Volumes/jax-data/data/variant-orthology/eQTL/v8/GTEx_Analysis_v8_eQTL.tar");
			StreamReader<EQTL> reader = ReaderFactory.getReader(new ReaderRequest(bigTar));
			long count = reader.stream()
							   .map(func::apply)
							   .filter(e->e.getRsId()!=null)
							   .count();
			
			assertEquals(72686455, count);
		}
	}

	
	private Map<String,String> readTestMappings(File file) throws Exception {
		
		Map<String,String> ret = new HashMap<>();
		Iterator<String> iterator = StreamUtil.createStream(file, true);
		
		try {
			int varIndex = -1;
			int rsIndex  = -1;

			while(iterator.hasNext()) {
				String line = iterator.next();
				String[] frags = line.split("\\t+");

				// Parse the header line, only if we have not
				if (varIndex<0 || rsIndex<0) {
					for (int i = 0; i < frags.length; i++) {
						if ("variant_id".equals(frags[i].toLowerCase())) {
							varIndex = i;
						} else if (frags[i].toLowerCase().startsWith("rs_id_")) {
							rsIndex = i;
						}
					}
					continue;
				}

				ret.put(frags[varIndex], frags[rsIndex]);
			}

		} finally {
			if (iterator instanceof Closeable) {
				try {
					((Closeable)iterator).close();
				} catch (IOException e) {
					throw e;
				}
			}
		}

		return ret;
	}
	
	private void testEQTL(EQTLFunction<EQTL, EQTL> func, String variantId, String rsId) {
		EQTL eqtl = new EQTL("test", variantId, null); 
		assertNull(eqtl.getRsId());
		func.apply(eqtl);
		assertEquals(rsId, eqtl.getRsId());
	}

}
