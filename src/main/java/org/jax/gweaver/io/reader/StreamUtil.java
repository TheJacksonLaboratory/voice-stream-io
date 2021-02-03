package org.jax.gweaver.io.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class StreamUtil {
	
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
