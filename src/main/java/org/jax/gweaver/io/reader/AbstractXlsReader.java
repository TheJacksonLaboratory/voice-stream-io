package org.jax.gweaver.io.reader;

import static org.jax.gweaver.io.reader.StreamUtil.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.jax.gweaver.domain.Entity;

public abstract class AbstractXlsReader<T extends Entity> implements StreamReader<T> {

	private ReaderRequest request;
	private int sheetIndex = 0;
	private int linesProcessed;
	private Class concreteClass;

	public AbstractXlsReader(ReaderRequest request) throws IOException {
		this.request = request;
	}
	
	/**
	 * Create a stream of domain objects which may be processed
	 * into a datastructure without holding the data in memory.
	 * 
	 * @return stream of type we are parsing.
	 * @throws ReaderException 
	 */
	public Stream<T> stream() throws ReaderException {
		
		try(InputStream in = unzip(request.stream(), request.name());
			HSSFWorkbook wb = new HSSFWorkbook(in)) {
		    
		    HSSFSheet sheet = wb.getSheetAt(getSheetIndex());
		    
		    Stream<Row> rows = StreamSupport.stream(sheet.spliterator(), false);
		    this.linesProcessed = 0;
		    return rows.map(r->create(r))
		    		   .filter(n->n!=null)
		    		   .map(n->{
					    	linesProcessed++;
					    	return n;
						});
		    
		} catch (Exception e) {
			throw new ReaderException(e);
		} finally {
			try {
				request.close();
			} catch (IOException e) {
				throw new ReaderException(e);
			}
		}
	}

	/**
	 * Parse the line to type T.
	 *
	 * @param line the line
	 * @return the t
	 * @throws ReaderException the reader exception
	 */
	protected abstract T create(Row row);
		 

	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		return null;
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
	public boolean isEmpty() {
		if (isDataSource()) return false;
		return request.getStream()!=null;
	}

	@Override
	public void close() throws IOException {

	}

	/**
	 * @return the sheetIndex
	 */
	protected int getSheetIndex() {
		return sheetIndex;
	}

	/**
	 * @param sheetIndex the sheetIndex to set
	 */
	protected void setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
	}

	/**
	 * @return the concreteClass
	 */
	protected Class getConcreteClass() {
		return concreteClass;
	}

	/**
	 * @param concreteClass the concreteClass to set
	 */
	protected void setConcreteClass(Class concreteClass) {
		this.concreteClass = concreteClass;
	}

}
