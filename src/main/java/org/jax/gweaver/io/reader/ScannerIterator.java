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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.jax.gweaver.io.reader.StreamUtil.unzip;


// TODO: Auto-generated Javadoc
/**
 * The Class ScannerIterator.
 *
 * @param <T> the generic type
 */
class ScannerIterator<T> implements Iterator<String> {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(ScannerIterator.class);

	/** The scanners. */
	private Iterator<Scanner> scanners;
	
	/** The current. */
	private Scanner current;
	
	/** The expander. */
	private Expander expander;
	
	/**
	 * Directory to expand into if we are using a zip
	 * You must call init(file) after setDir if you want to change the temp folder.
	 */
	private Path dir;

	/**
	 * Instantiates a new scanner iterator.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ScannerIterator() throws IOException {
		// You must call init(file) after setDir if you want to change the temp folder.
	}

	/**
	 * Instantiates a new scanner iterator.
	 *
	 * @param zipOrDirOrFile the zip or dir or file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ScannerIterator(File zipOrDirOrFile) throws IOException {
		init(zipOrDirOrFile);
	}

	/**
	 * Instantiates a new scanner iterator.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ScannerIterator(InputStream in, String name) throws IOException {
		List<Scanner> scans = Arrays.asList(new Scanner(unzip(in, name)));
		this.scanners = scans.iterator();
	}

	/**
	 * Call to create the iterator, including expanding zip files.
	 *
	 * @param zipOrDirOrFile the zip or dir or file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void init(File zipOrDirOrFile) throws IOException {
		this.scanners = createIterator(zipOrDirOrFile);
	}

	/**
	 * Checks for next.
	 *
	 * @return true, if successful
	 */
	@Override
	public synchronized boolean hasNext() {
		if (current==null) {
			if (!scanners.hasNext()) {
				clean();
				return false;
			}
		} else {
			return current.hasNextLine();
		}
		return true;
	}

	/**
	 * Next.
	 *
	 * @return the string
	 */
	@Override
	public synchronized String next() {
		if (current != null && !current.hasNext()) {
			current.close();
			current = null;
		}
		if (current==null) {
			if (!scanners.hasNext()) return null;
			current = scanners.next();
		}
		String nextLine = current.nextLine();
		if (!current.hasNext()) current = null;
		return nextLine;
	}


	/**
	 * Creates the iterator.
	 *
	 * @param zdof the zdof
	 * @return the iterator
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Iterator<Scanner> createIterator(File zdof) throws IOException {


		if (zdof.isFile()) {
			if (zdof.getName().toLowerCase().endsWith(".zip")) {
				if (this.dir == null) this.dir = Files.createTempDirectory("Scanner_gweaver");
				this.expander = new Expander(dir);
				List<Path> expanded = expander.expand(zdof.toPath());
				
				final List<Scanner> scans = expanded.stream().map(p->{
					try {
						return new Scanner(p.toFile());
					} catch (FileNotFoundException e) {
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList());
				return scans.iterator();
				
			} else if (zdof.getName().toLowerCase().endsWith(".gz")) {
				List<Scanner> scans = Arrays.asList(new Scanner(new GZIPInputStream(new FileInputStream(zdof))));
				return scans.iterator();

			} else {
				List<Scanner> scans = Arrays.asList(new Scanner(zdof));
				return scans.iterator();
			}
		}

		throw new IllegalArgumentException("No Scanner creator for "+zdof.getName());
	}
		
	/**
	 * Clean.
	 */
	private void clean() {
		try {
			if (current!=null) {
				current.close();
			}
		} catch (Exception e) {
			logger.error("Cannot close last scanner!", e);
		}
		try {
			if (expander!=null) {
				expander.close(); // Deletes files
			}
		} catch (Exception e) {
			logger.error("Cannot delete temp zip dir!", e);
		}
	}

	/**
	 * Gets the dir.
	 *
	 * @return the dir
	 */
	public Path getDir() {
		return dir;
	}

	/**
	 * Sets the dir.
	 *
	 * @param dir the dir to set
	 */
	public void setDir(Path dir) {
		this.dir = dir;
	}

}
