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
package org.jax.gweaver.io.reader;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class ScannerIteratorTest.
 */
public class ZipTest extends AbstractDataFileTest {
	
	
	/**
	 * Clear.
	 *
	 * @param dir the dir
	 */
	public void clear(Path dir) {
		if (dir==null) return; // Scanners do not always have them
		if (Files.exists(dir)) {
			FileUtils.deleteQuietly(dir.toFile());
		}
	}

	
	/**
	 * Variant zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void variantZipRead1() throws Exception {
		// 34 comment lines
		count("data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip", 872993+34);
	}

	/**
	 * Variant zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void variantZipRead2() throws Exception {
		// 31 comment lines
		count("data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip", 1726211+31);
	}
	
	/**
	 * Gene zip read 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void geneZipRead1() throws Exception {
		// 5 comment lines
		count("data/zip/hs_gtf/hg38_1.gtf.zip", 1173235+5);
	}

	/**
	 * Gene zip read 2.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void geneZipRead2() throws Exception {
		// 5 comment lines
		count("data/zip/mm_gtf/mm10_1.gtf.zip", 899084+5);
	}
	
	
	/**
	 * Files zip.
	 *
	 * @throws Exception the exception
	 */
	@Test(timeout = 120000)
	public void filesZip() throws Exception {
		// Active lines and comment lines in these files.
		count("data/io/files.zip", (1173235+5)+(899084+5)+(1726211+31));
	}

	/**
	 * Count.
	 *
	 * @param path the path
	 * @param expected the expected
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void count(String path, long expected) throws IOException {
		
		Iterator<String> iterator = StreamUtil.createScanner(getPath(path));
		
		try(TimeInfo info = new TimeInfo()) {
			iterator.forEachRemaining(info::increment);
			
			assertEquals(expected, info.getCount());
			
			info.close();
			System.out.println("Iterated '"+path+"' in "+info.getTime()+ " ms");
		}
	}

}
