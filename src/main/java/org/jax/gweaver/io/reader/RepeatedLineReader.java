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

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.Gene;
import org.jax.gweaver.domain.GeneticEntity;
import org.jax.gweaver.domain.Transcript;
import org.jax.gweaver.domain.Variant;

// TODO: Auto-generated Javadoc
/**
 * This class repeats the same line a given number of times. 
 * It allows tests to be created, including in production, which
 * check the scale of the solution. The real gvf and gtf files
 * may be huge when really processed.
 *
 * @author Matthew Gerring
 * @param <T> the generic type
 */
@SuppressWarnings("all")
public class RepeatedLineReader<T extends GeneticEntity> extends LineIteratorReader<T> {

	/** The reader. */
	private LineIteratorReader<T> reader;
	
	/** The gene count. */
	private static int geneCount;
	
	/** The var count. */
	private static int varCount;
	

	public RepeatedLineReader() {
		
	}
	
	public RepeatedLineReader(ReaderRequest request) throws ReaderException {
		init(request);
	}
	
	/**
	 * Create a reader that just repeats a similar line 'size' number of times.
	 * Used for testing mostly.
	 *
	 * @param species the species
	 * @param size the size
	 * @param type the type
	 * @throws ReaderException 
	 */
	public RepeatedLineReader<T> init(ReaderRequest request) throws ReaderException {
		this.request = request;
		createIterator(request.getExpectedSize(), request.getObjType());
		setChunkSize(1000);
		
		request.setName(getType(request.getObjType()));
		request.setNoInputStream(true); // We do not read the stream
		this.reader = ReaderFactory.getReader(request);
		
		// We just start the counters somewhere representative.
		geneCount = 223180;
		varCount = 656;
		
		return this;
	}

	/**
	 * Creates the.
	 *
	 * @param line the line
	 * @return the t
	 * @throws ReaderException the reader exception
	 */
	@Override
	protected T create(String line) throws ReaderException {
		return reader.create(line);
	}

	/**
	 * Gets the assignment char.
	 *
	 * @return the assignment char
	 */
	@Override
	protected String getAssignmentChar() {
		return reader.getAssignmentChar();
	}

	/**
	 * Creates the iterator.
	 *
	 * @param <T> the generic type
	 * @param size the size
	 * @param type the type
	 * @return the iterator
	 */
	private static <T> Iterator<String> createIterator(final int size, Class<? extends Entity> objType) {
		
		return new Iterator<String>() {
			int counted = 0;

			@Override
			public boolean hasNext() {
				return counted<size;
			}

			@Override
			public String next() {
				String line = nextLine(objType);
				counted++;
				return line;
			}
			
		};
	}
	
	public boolean isEmpty() {
		return count>=request.getExpectedSize();
	}


	private static String getType(Class<? extends Entity> objType) {
		if (objType == Variant.class) return "file.gvf";
		if (objType == Gene.class || objType==Transcript.class) return "file.gtf";
		throw new IllegalArgumentException("Cannot repeat on type "+objType);
	}
	
	@Override
	protected synchronized String nextLine() {
		if (isEmpty()) return null;
		count++;
		return nextLine(request.getObjType());
	}
	/**
	 * Next line.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the string
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private static <T> String nextLine(Class<? extends Entity> type) throws IllegalArgumentException {
		if (type == Gene.class) {
			return "1	ensembl	gene	758233	758336	.	-	.	gene_id \"ENSG00000"+(++geneCount)+"\"; gene_version \"1\"; gene_name \"RNU6-1199P\"; gene_source \"ensembl\"; gene_biotype \"snRNA\";";
		} else if (type == Variant.class) {
			return "19	dbSNP	SNV	92959	92959	.	+	.	ID="+(++varCount)+";Variant_seq=G;ancestral_allele=A;Variant_effect=upstream_gene_variant 0 transcript ENST00000633500;evidence_values=Frequency;Dbxref=dbSNP_150:rs1025620664;Reference_seq=A";
		} else {
			throw new IllegalArgumentException("Cannot get example line for "+type);
		}
	}


	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		throw new IllegalArgumentException("The repeated line reader is for testing and does not have a connector type!");
	}

}
