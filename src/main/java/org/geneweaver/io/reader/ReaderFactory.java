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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.geneweaver.domain.Entity;

/**
 * Simple factory for getting reader by file extension.
 * 
 * @author gerrim
 *
 */
public class ReaderFactory {

	/** The Constant classes. */
	private static final Map<String, Class> classes;
	static {
		@SuppressWarnings("rawtypes")
		Map<String, Class> tmp = new HashMap<>();
		
		// These guys are fairly standard I think.
		tmp.put("gtf", 			GeneReader.class);
		tmp.put("gvf", 			VariantReader.class);
		tmp.put("bed", 			BedReader.class);
		
		// If there are multiple xls formats, we will have to ask 
		// if it is applicable for a given format and reader request.
		tmp.put("xls", 			ChiapetReader.class);
		
		// If there are multiple tsv formats, we will have to ask 
		// if it is applicable for a given format and reader request.
		tmp.put("tsv", 			Fantom5EnsemblMapReader.class);

		// If there are multiple rpt formats, we will have to ask 
		// if it is applicable for a given format and reader request.
		tmp.put("rpt", 			HomologGeneReader.class);
		
		// @see https://storage.googleapis.com/gtex_analysis_v8/single_tissue_qtl_data/README_eQTL_v8.txt
		tmp.put("^.+\\.egenes\\.txt(\\.gz)?$", 						GTExEQTLReader.class);
		tmp.put("^.+\\.sgenes\\.txt(\\.gz)?$", 						GTExEQTLReader.class);
		tmp.put("^.+\\.signif_variant_gene_pairs\\.txt(\\.gz)?$",	GTExEQTLReader.class);
		tmp.put("^.+\\.sqtl_signifpairs\\.txt(\\.gz)?$", 			GTExEQTLReader.class);
		tmp.put("^.+\\.allpairs\\.txt(\\.gz)?$", 					GTExEQTLReader.class);
		tmp.put("^.+\\.sqtl_allpairs\\.txt(\\.gz)?$",				GTExEQTLReader.class);
		// This is read directly into a database in EQTLFunction
		//tmp.put("^.+\\.lookup_table\\.txt(\\.gz)?$",				GTExEQTLReader.class);
		
		tmp.put("^GTEx.+Annotations.+Sample.+.txt(\\.gz)?$",		GTExSampleReader.class);

		// Archive Reader just calls back this reader with each entry
		tmp.put("tar", 			ArchiveReader.class);
		tmp.put("zip", 			ArchiveReader.class);

		
		classes = Collections.unmodifiableMap(tmp);
	}
	
	/**
	 * A list of supported file extensions.
	 * @return
	 */
	public static Collection<String> extensions() {
		return classes.keySet();
	}
	
	/**
	 * Get a reader using the file extension to find the correct one.
	 *
	 * @param <T> the generic type
	 * @param species the species
	 * @param file the file
	 * @return the reader
	 * @throws ReaderException the reader exception
	 */
	public static <R extends StreamReader<T>, T extends Entity> R getReader(ReaderRequest request) throws ReaderException {
		Class<R> clazz = getClass(request);
		try {
			Constructor<R> constructor = clazz.getDeclaredConstructor();
			R instance = constructor.newInstance();
			
			if (request.isInitRequired()) {
				Method init = clazz.getMethod(StreamReader.INIT, ReaderRequest.class);
				init.invoke(instance, request);
			}
			return instance;
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ReaderException(e);
		}
	}

	/**
	 * Gets the class.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the class
	 * @throws ReaderException the reader exception
	 */
	private static <R extends StreamReader<T>, T extends Entity> Class<R> getClass(ReaderRequest request) throws ReaderException {
		
		// Figure out reader from name. Later we may need more complex logic.
		String name = request.name();
		Class<R> clazz = getClassByName(name);
		if (clazz!=null) return clazz;
		throw new ReaderException("There is no reader for "+name);
	}

	/**
	 * Check if a given reader request would result in a valid reader class.
	 * @param request
	 * @return true if we have a reader!
	 * @throws ReaderException 
	 */
	public static boolean isSupported(ReaderRequest request) throws ReaderException {
		String name = request.name();
		Class<?> clazz = getClassByName(name);
		return clazz!=null;
	}
	
	@SuppressWarnings("unchecked")
	private static  <R extends StreamReader<T>, T extends Entity> Class<R> getClassByName(String name) throws ReaderException{
		
		String ext = FilenameUtils.getExtension(name);
		if (ext==null) throw new ReaderException(name+" does not have an extension!");
		ext = ext.toLowerCase();
		if (classes.containsKey(ext)) {
			return (Class<R>)classes.get(ext);
		}
		if ("gz".equals(ext)) {
			String alt = name.substring(name.indexOf('.')+1);
			if (alt.contains(".")) {
				ext = alt.substring(0, alt.indexOf('.'));
				if (classes.containsKey(ext)) {
					return (Class<R>)classes.get(ext);
				}
			}
		}
		
		// Try matching the name with the keys
		for (String key : classes.keySet()) {
			if (Pattern.compile(key).matcher(name).matches()) {
				return (Class<R>)classes.get(key);
			}
		}
		
		return null;
	}


}
