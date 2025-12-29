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
package org.jax.voice.io.reader;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * It is best not to use Scanner for these large files as it is slow
 * @see https://stackoverflow.com/questions/19486077/java-fastest-way-to-read-through-text-file-with-2-million-lines
 * 
 * @author gerrim
 *
 */
public class StreamUtil {
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static Iterator<String> createStream(ReaderRequest request) throws IOException {
		if (request.isFileRequest()) {
			return StreamUtil.createStream(request.getFile(), request.isCloseInputStream());
		} else if (request.getStream()!=null) {
			return StreamUtil.createStream(request.getStream(), request.name(), request.isCloseInputStream());
		}
		throw new IOException("Cannot create scanner from request "+request);
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Iterator<String> createStream(File file, boolean shouldClose) throws IOException {
		return createStream(new FileInputStream(file), file.getName(), shouldClose);
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Iterator<String> createStream(Path path, boolean shouldClose) throws IOException {
		String name = path.getFileName().toString();
		return createStream(Files.newInputStream(path), name, shouldClose);
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Iterator<String> createStream(URL url, boolean shouldClose) throws IOException {
		return createStream(url.openStream(), FilenameUtils.getName(url.toString()), shouldClose);
	}


	/**
	 * Creates the scanner which can also deal with zip and tar files.
	 *
	 * @param zdof the zdof
	 * @return the iterator
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Iterator<String> createStream(InputStream in, String name, boolean shouldClose) throws IOException {

		if (name.toLowerCase().endsWith(".gz")) {
			return new LineIterator(new GZIPInputStream(in), shouldClose);

		} else {
			return new LineIterator(in, shouldClose);
		}
	}
	
	/**
	 * Kind of an interator but remove() does not work.
	 * 
	 * @author gerrim
	 *
	 */
	private static class LineIterator implements Iterator<String>, Closeable {

		protected String nextLine;
		protected BufferedReader reader;
		protected boolean shouldClose;
		protected boolean shouldReadNext = true;
		protected boolean isNext;

		public LineIterator(InputStream in, boolean shouldClose) throws IOException {
			this.shouldClose = shouldClose;
			this.reader = new BufferedReader(new InputStreamReader(in));
		}
		
		@Override
		public boolean hasNext() {
			if (!shouldReadNext) return isNext;
			nextLine = line();
			isNext = nextLine!=null;
			shouldReadNext = false;
			return isNext;
		}

		@Override
		public String next() {
			shouldReadNext = true; // They had it!
			return nextLine;
		}
		
		protected String line() {
			
			String line = null;
			try {
				line = reader.readLine();
				if (line==null) {
					close();
				}
				return line;
				
			} catch (IOException ne) {
				throw new IllegalArgumentException(ne);
			}
		}

		public void close() throws IOException {
			if (shouldClose) reader.close();
		}
	}
	
	/**
	 * Makes sure the input stream is dealt with if zipped.
	 * If not zipped, returns stream, if gzip returns gz stream,
	 * if zip then gets first entry and returns zip stream.
	 *
	 * @param zdof the zdof
	 * @return the iterator
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static final InputStream unzip(InputStream in, String name) throws IOException {

		if (name.toLowerCase().endsWith(".zip")) {
			ZipInputStream zin = new ZipInputStream(in);
			zin.getNextEntry();
			return zin;

		} else if (name.toLowerCase().toLowerCase().endsWith(".gz")) {
			return new GZIPInputStream(in);

		} else {
			return in;
		}
	}

}
