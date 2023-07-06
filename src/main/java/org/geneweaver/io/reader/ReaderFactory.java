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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * Simple factory for getting reader by file extension.
 * 
 * @author gerrim
 *
 */
public class ReaderFactory {

	/** The Constant classes. */
	private static final Map<Object, Object> classes;
	static {
		Map<Object, Object> tmp = new LinkedHashMap<>();
		
		// These guys are fairly standard I think.
		tmp.put("gtf", 			GeneReader.class);
		tmp.put("gvf", 			VariantReader.class);
		tmp.put("vcf", 			FastVCFReader.class);
		tmp.put("bed", 			BedReader.class);
		tmp.put("step", 		StepReader.class);
		
		// If there are multiple xls formats, we will have to ask 
		// if it is applicable for a given format and reader request.
		tmp.put("xls", 			ChiapetReader.class);
		
		// If there are multiple tsv formats, we will have to ask 
		// if it is applicable for a given format and reader request.
		tmp.put("tsv", 			Arrays.asList(Fantom5EnsemblMapReader.class, MapCSVReader.class));
		tmp.put("txt", 			MapCSVReader.class);

		// If there are multiple rpt formats, we will have to ask 
		// if it is applicable for a given format and reader request.
		tmp.put("rpt", 			HomologGeneReader.class);
		
		// This one is for the jax csv files which are parsed out of mouse eQTL data.
		tmp.put(Pattern.compile("^.+\\_balyor\\.csv(\\.gz)?$"), 	OrthologBaylorReader.class);
		tmp.put("csv", 												Arrays.asList(JaxEQTLReader.class, MapCSVReader.class));
		
		// These eQTLs are from this paper: https://www.biorxiv.org/content/10.1101/655670v1
		// And these files: https://zenodo.org/record/3408356#.YQljwlNKii6
		tmp.put(Pattern.compile("^(.+)_.+_eQTLs.txt(\\.gz)?$"),		FlexEQTLReader.class);
		
		// @see https://storage.googleapis.com/gtex_analysis_v8/single_tissue_qtl_data/README_eQTL_v8.txt
		tmp.put(Pattern.compile("^.+\\.egenes\\.txt(\\.gz)?$"), 					GTExEQTLReader.class);
		tmp.put(Pattern.compile("^.+\\.sgenes\\.txt(\\.gz)?$"), 					GTExEQTLReader.class);
		tmp.put(Pattern.compile("^.+\\.signif_variant_gene_pairs\\.txt(\\.gz)?$"),	GTExEQTLReader.class);
		tmp.put(Pattern.compile("^.+\\.sqtl_signifpairs\\.txt(\\.gz)?$"), 			GTExEQTLReader.class);
		tmp.put(Pattern.compile("^.+\\.allpairs\\.txt(\\.gz)?$"), 					GTExEQTLReader.class);
		tmp.put(Pattern.compile("^.+\\.sqtl_allpairs\\.txt(\\.gz)?$"),				GTExEQTLReader.class);
		// This is read directly into a database in EQTLFunction
		//tmp.put("^.+\\.lookup_table\\.txt(\\.gz)?$",				GTExEQTLReader.class);
		
		tmp.put(Pattern.compile("^GTEx.+Annotations.+Sample.+.txt(\\.gz)?$"),		GTExSampleReader.class);

		// Archive Reader just calls back this reader with each entry
		tmp.put("tar", 			ArchiveReader.class);
		tmp.put("zip", 			ArchiveReader.class);

		
		classes = Collections.unmodifiableMap(tmp);
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
	public static <R extends StreamReader<T>, T> R getReader(ReaderRequest request) throws ReaderException {
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
	private static <R extends StreamReader<T>, T> Class<R> getClass(ReaderRequest request) throws ReaderException {
		
		// Figure out reader from name. Later we may need more complex logic.
		Class<R> clazz = getClassByName(request);
		if (clazz!=null) return clazz;
		throw new ReaderException("There is no reader for "+request.name());
	}

	/**
	 * Check if a given reader request would result in a valid reader class.
	 * @param request
	 * @return true if we have a reader!
	 * @throws ReaderException 
	 */
	public static boolean isSupported(ReaderRequest request) throws ReaderException {
		Class<?> clazz = getClassByName(request);
		return clazz!=null;
	}
	
	@SuppressWarnings("unchecked")
	private static  <R extends StreamReader<T>, T> Class<R> getClassByName(ReaderRequest request) throws ReaderException{
		
		String name = request.name();
		
		// Unfortunately we have to loop here because files with the 
		// same extension can have different readers, e.g. txt, csv.
		Object found = null;
		
		// Process the patterns first, all of them
		for (Object key : classes.keySet()) {
			
			if (key instanceof Pattern) {
				Pattern pattern = (Pattern)key;
				Matcher matcher = pattern.matcher(name);
				if (matcher.matches()) {
					request.setMatcher(matcher);
					found = classes.get(key);
					break;
				}
			}
		}
		
		// Process the direct keys
		if (found==null) for (Object key : classes.keySet()) {
		    if (key instanceof String) {
				String ext = FilenameUtils.getExtension(name);
				if ("gz".equals(ext)) {
					ext = FilenameUtils.getExtension(name.substring(0, name.length()-3));
				}
				
				if (ext==null) throw new ReaderException(name+" does not have an extension!");
				ext = ext.toLowerCase();
				if (key.toString().toLowerCase().equals(ext)) {
					found = classes.get(ext);
					break;
				}
			}
		}
		
		if (found!=null) {
			if (found instanceof Class) {
				return (Class<R>)found;
			} else if (found instanceof Collection) {
				if (request.getReaderHint()==null) {
					return (Class<R>)((Collection)found).iterator().next();
				} else {
					String hint = request.getReaderHint();
					for (Iterator<Class<R>> it = ((Collection<Class<R>>)found).iterator(); it.hasNext();) {
						Class<R> clazz = it.next();
						if (clazz.getName().contains(hint)) return clazz;
					}
				}
			}
		}

		return null;
	}


}
