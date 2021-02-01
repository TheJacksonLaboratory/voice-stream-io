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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jax.gweaver.io.reader.Expander;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * Test the expander class.
 * 
 * @author Matthew Gerring
 *
 */
public class ExpanderTest extends AbstractDataFileTest{

	/** The expander. */
	private Expander expander;
	
	/** The all directories. */
	private static List<Path> allDirectories;
	
	/**
	 * Innit.
	 */
	@BeforeClass
	public static void innit() {
		allDirectories = new ArrayList<>();
	}
	
	/**
	 * Check gone.
	 *
	 * @throws Exception the exception
	 */
	@AfterClass
	public static void checkGone() throws Exception {
		allDirectories.stream().forEach(dir->assertFalse(Files.exists(dir)));
	}

	/**
	 * Creates the.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void create() throws IOException {
		Path dir = Files.createTempDirectory("ExpanderDir");
		this.expander = new Expander(dir);
	}
	
	/**
	 * Clean.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@After
	public void clean() throws IOException {
		allDirectories.add(expander.getDir());
		this.expander.close();
	}

	/**
	 * Unzip 1.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void unzip1() throws Exception {
	    Path path = getPath("data/zip/hs_gtf/hg38_1.gtf.zip");
	    expander.expand(path);
	    
	    assertEquals(1, Files.list(expander.getDir()).count());
	    assertTrue(expander.isDeleteOnExit());
	}

}