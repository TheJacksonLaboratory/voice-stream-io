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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.jax.gweaver.domain.GeneticEntity;

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
public class RepeatedLineReader<T extends GeneticEntity> extends AbstractReader<T> {

	/** The reader. */
	private AbstractReader<T> reader;
	
	/**
	 * Create a reader that just repeats a similar line 'size' number of times.
	 * Used for testing mostly.
	 *
	 * @param species the species
	 * @param size the size
	 * @param type the type
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public RepeatedLineReader(String species, int size, Class<? extends AbstractReader> type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		super(species, createIterator(size, type));
		setChunkSize(1000);
		this.reader = type.getDeclaredConstructor(String.class).newInstance(species);
		
		// We just start the counters somewhere representative.
		geneCount = 223180;
		varCount = 656;
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
	private static <T> Iterator<String> createIterator(final int size, Class<? extends AbstractReader> type) {
		return new Iterator<String>() {
			int counted = 0;

			@Override
			public boolean hasNext() {
				return counted<size;
			}

			@Override
			public String next() {
				String line = nextLine(type);
				counted++;
				return line;
			}
			
		};
	}

	/** The gene count. */
	private static int geneCount;
	
	/** The var count. */
	private static int varCount;
	
	/**
	 * Next line.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the string
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private static <T> String nextLine(Class<? extends AbstractReader> type) throws IllegalArgumentException {
		if (type == GeneReader.class) {
			return "1	ensembl	gene	758233	758336	.	-	.	gene_id \"ENSG00000"+(++geneCount)+"\"; gene_version \"1\"; gene_name \"RNU6-1199P\"; gene_source \"ensembl\"; gene_biotype \"snRNA\";";
		} else if (type == VariantReader.class) {
			return "19	dbSNP	SNV	92959	92959	.	+	.	ID="+(++varCount)+";Variant_seq=G;ancestral_allele=A;Variant_effect=upstream_gene_variant 0 transcript ENST00000633500;evidence_values=Frequency;Dbxref=dbSNP_150:rs1025620664;Reference_seq=A";
		} else {
			throw new IllegalArgumentException("Cannot get example line for "+type);
		}
	}

}
