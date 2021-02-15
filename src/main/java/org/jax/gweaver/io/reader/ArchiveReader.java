package org.jax.gweaver.io.reader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.jax.gweaver.domain.Entity;

public class ArchiveReader<T extends Entity> extends AbstractStreamReader<T> {

	private Iterator<T> iterator;
	private int linesProcessed;

	@Override
	public Stream<T> stream() throws ReaderException {
		try {
			this.linesProcessed = 0;
			this.iterator = createIterator(false);
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
			
		} catch (IOException e) {
			throw new ReaderException(e);
		}
	}
	
	public List<T> wind() throws ReaderException {

		try {
			if (iterator==null) this.iterator = createIterator(false);
			
			StreamReader<T> reader = ((StreamIterator)iterator).getActiveReader();
			return reader.wind();
			
		} catch (IOException e) {
			throw new ReaderException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		
		if (iterator==null) return false;
		try {
			StreamReader<T> reader = ((StreamIterator)iterator).getActiveReader();
			return reader.isEmpty();
		} catch (IOException | ReaderException e) {
			return true;
		}
	}

	private Iterator<T> createIterator(boolean closeStream) throws IOException, ReaderException {
		
		String name = request.name();
		if (name.toLowerCase().endsWith(".zip")) {
			return new ZipIterator(request.stream());
			
		} else if (name.toLowerCase().endsWith(".tar")) {
			return new TarIterator(request.stream());
		} else {
			throw new IllegalArgumentException("Cannot find archive reader for "+name);
		}
	}


	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		throw new IllegalArgumentException("An archive may contain different files of different types!");
	}

	@Override
	public int linesProcessed() {
		return linesProcessed;
	}

	@Override
	public boolean isDataSource() {
		return request.isFileRequest();
	}

	@Override
	public void close() throws IOException {
		if (iterator!=null && iterator instanceof Closeable) {
			((Closeable)iterator).close();
		}
	}

	private abstract class StreamIterator implements Iterator<T>, Closeable {

		protected InputStream parent;
		protected Iterator<T> currentIterator;
		protected AbstractStreamReader<T> reader;

		public StreamIterator(InputStream in) throws IOException {
			this.parent = in;
		}
		
		public StreamReader<T> getActiveReader() throws IOException, ReaderException {
			if (reader!=null && !reader.isEmpty()) return reader;
			nextIterator(); // If stream exhausted, see if there is another file.
			return reader;
		}

		@Override
		public boolean hasNext() {
			if (currentIterator==null) return false;
			boolean more = currentIterator.hasNext();
			if (!more) {
				linesProcessed+=reader.linesProcessed();
			}
			return more;
		}

		@Override
		public T next() {
			if (currentIterator==null) return null;
			
			T next = currentIterator.next();
			
			if (!currentIterator.hasNext()) {
				linesProcessed+=reader.linesProcessed();
				try {
					currentIterator = nextIterator();
				} catch (IOException | ReaderException e) {
					throw new RuntimeException(e);
				}
			}
			
			return next;
		}

		protected abstract Iterator<T> nextIterator() throws IOException, ReaderException;
		
		protected boolean isEntryValid(String name) {
			if (name==null) return false;
			if (request.getFileFilter()!=null) {
				if (!name.matches(request.getFileFilter())) {
					return false;
				}
			}
			return true;
		}

		public void close() throws IOException {
			parent.close();
		}
	}

	/**
	 * Kind of an interator but remove() does not work.
	 * 
	 * @author gerrim
	 *
	 */
	private class TarIterator extends StreamIterator {

		private TarArchiveInputStream tstream;
		
		public TarIterator(InputStream in) throws IOException, ReaderException {
			super(new TarArchiveInputStream(in));
			this.tstream = (TarArchiveInputStream)parent;
			this.currentIterator = nextIterator();
		}
		
		@Override
		protected Iterator<T> nextIterator() throws IOException, ReaderException {
			TarArchiveEntry entry = tstream.getNextTarEntry();
			if (entry==null) return null;
			while(!isEntryValid(entry.getName())) {
				entry = tstream.getNextTarEntry();
				if (entry==null) return null;
			}
			this.reader = ReaderFactory.getReader(new ReaderRequest(tstream, entry.getName(), false));
			reader.setChunkSize(getChunkSize());
			reader.setEntryName(entry.getName());
			return reader.stream().iterator();
		}
	}

	/**
	 * Kind of an iterator but remove() does not work.
	 * 
	 * @author gerrim
	 *
	 */
	private class ZipIterator extends StreamIterator {

		private ZipInputStream zstream;
		public ZipIterator(InputStream in) throws IOException, ReaderException {
			super(new ZipInputStream(in));
			this.zstream = (ZipInputStream)parent;
			this.currentIterator = nextIterator();
		}
		
		@Override
		protected Iterator<T> nextIterator() throws IOException, ReaderException {
			ZipEntry entry = zstream.getNextEntry();
			if (entry==null) return null;
			while(!isEntryValid(entry.getName())) {
				entry = zstream.getNextEntry();
				if (entry==null) return null;
			}
			this.reader = ReaderFactory.getReader(new ReaderRequest(zstream, entry.getName(), false));
			reader.setChunkSize(getChunkSize());
			reader.setEntryName(entry.getName());
			return reader.stream().iterator();
		}
	}

}
