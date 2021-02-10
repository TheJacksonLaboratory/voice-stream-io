package org.jax.gweaver.io.reader;

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
import java.util.zip.ZipEntry;
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
	public static Iterator<String> createScanner(ReaderRequest request) throws IOException {
		if (request.isFileRequest()) {
			return StreamUtil.createScanner(request.getFile());
		} else if (request.getStream()!=null) {
			return StreamUtil.createScanner(request.getStream(), request.name());
		}
		throw new IOException("Cannot create scanner from request "+request);
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static Iterator<String> createScanner(File file) throws IOException {
		return createScanner(new FileInputStream(file), file.getName());
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Iterator<String> createScanner(Path path) throws IOException {
		String name = path.getFileName().toString();
		return createScanner(Files.newInputStream(path), name);
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Iterator<String> createScanner(URL url) throws IOException {
		return createScanner(url.openStream(), FilenameUtils.getName(url.toString()));
	}


	/**
	 * Creates the scanner which can also deal with zip and tar files.
	 *
	 * @param zdof the zdof
	 * @return the iterator
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Iterator<String> createScanner(InputStream in, String name) throws IOException {


		if (name.toLowerCase().endsWith(".zip")) {
			return new ZipIterator(in);

		} else if (name.toLowerCase().endsWith(".gz")) {
			return new StreamIterator(new GZIPInputStream(in));

		} else {
			return new StreamIterator(in);
		}
	}
	
	/**
	 * Kind of an interator but remove() does not work.
	 * 
	 * @author gerrim
	 *
	 */
	private static class StreamIterator implements Iterator<String>, Closeable {

		private String nextLine;
		private BufferedReader reader;
		public StreamIterator(InputStream in) throws IOException {
			this.reader = new BufferedReader(new InputStreamReader(in));
		}

		private boolean shouldReadNext = true;
		private boolean isNext;
		
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
		
		private String line() {
			
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
			reader.close();
		}
	}
	
	/**
	 * Kind of an interator but remove() does not work.
	 * 
	 * @author gerrim
	 *
	 */
	private static class ZipIterator implements Iterator<String>, Closeable {

		private String nextLine;
		private BufferedReader reader;
		private ZipInputStream zstream;
		public ZipIterator(InputStream in) throws IOException {
			this.zstream = new ZipInputStream(in);
			this.reader = new BufferedReader(new InputStreamReader(zstream));
			zstream.getNextEntry();
		}

		private boolean shouldReadNext = true;
		private boolean isNext;
		
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
		
		private String line() {
			
			String line = null;
			try {
				line = reader.readLine();
				if (line == null) {
					ZipEntry entry = zstream.getNextEntry();
					if (entry == null) return null;
					line = reader.readLine();
				}
				
				if (line==null) {
					close();
				}
				return line;
				
			} catch (IOException ne) {
				throw new IllegalArgumentException(ne);
			}
		}

		public void close() throws IOException {
			reader.close();
			zstream.close();
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


	private static boolean isArchive(String name) {
		return name!=null && 
				(name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".tar"));
	}


}
