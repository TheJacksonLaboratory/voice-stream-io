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
package org.geneweaver.io.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geneweaver.domain.Variant;
import org.junit.Before;

// TODO: Auto-generated Javadoc
/**
 * A Test able to get data files.
 * @author gerrim
 *
 */
public abstract class AbstractDataFileTest {
	
	@Before
	public void before() throws Exception {
		BedReader.clearCounting();
	}
	
	/**
	 * You must clone git clone git@bitbucket.org:geneweaver/variant-orthology-data.git
	 * either along side this repo or into it (more convenient for builds) and it must
	 * be called either variant-orthology-data or variant-orthology-data.git and be a folder 
	 * with the the data folder in.
	 *
	 * @param path the path
	 * @return file of test with which we want to run.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public File getFile(String path) throws IOException {
		
		return getPath(path).toFile();
	}
	
	/**
	 * You must clone git clone git@bitbucket.org:geneweaver/variant-orthology-data.git
	 * either along side this repo or into it (more convenient for builds) and it must
	 * be called either variant-orthology-data or variant-orthology-data.git and be a folder 
	 * with the the data folder in.
	 *
	 * @param path the path
	 * @return file of test with which we want to run.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Path getPath(String path) throws IOException {
		
		Path dir = getData();
		if (dir == null) {
			throw new FileNotFoundException("The file "+path+" is not there. "+getFileMessage());
		}
		return dir.resolve(path);
	}


	/**
	 * Gets the data.
	 *
	 * @return the data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Path getData() throws IOException {
		String[] paths = new String[] {"./", "../", "../../", "../../../", "../../../../", "/Volumes/Work/JAX/repos/"};
		for (String spath : paths) {
			Path path = Paths.get(spath+"gweaver-test-data");
			if (Files.exists(path) && Files.isDirectory(path)) {
				return path;
			}
			path = Paths.get(spath+"gweaver-test-data.git");
			if (Files.exists(path) && Files.isDirectory(path)) {
				return path;
			}
		}
	    throw new FileNotFoundException(getFileMessage());
	}

	/**
	 * Gets the file message.
	 *
	 * @return the file message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String getFileMessage() throws IOException {
		StringBuilder buf = new StringBuilder("Please run 'git clone git@bitbucket.org:geneweaver/gweaver-test-data.git'\n");
		buf.append("Working directory: ");
		appendDir(".", buf);
		
		buf.append("\nAbove directory: ");
		appendDir("../", buf);
		return buf.toString();
	}

	/**
	 * Append dir.
	 *
	 * @param spath the spath
	 * @param buf the buf
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void appendDir(String spath, StringBuilder buf) throws IOException {
		Path path = Paths.get(spath);
		buf.append(path.toAbsolutePath());
		buf.append("\n");
		Files.list(path)
		 .map(Path::getFileName)
		 .map(buf::append)
		 .forEach(b->b.append("\n"));		
	}

	
	protected Variant createVariant(String rsId, String chr, int start, int end) {
		Variant var = new Variant();
		var.setRsId(rsId);
		var.setStart(start);
		var.setEnd(end);
		var.setChr(chr);
		return var;
	}


}
