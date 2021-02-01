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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jax.gweaver.domain.Entity;

/**
 * Simple factory for getting reader by file extension.
 * 
 * @author gerrim
 *
 */
public class ReaderFactory {

	/** The Constant classes. */
	@SuppressWarnings({ "unused", "rawtypes" }) // Intentionally we avoid generics here
	private static final Map<String, Class> classes;
	static {
		@SuppressWarnings("rawtypes")
		Map<String, Class> tmp = new HashMap<>();
		tmp.put("gtf", 			GeneReader.class);
		tmp.put("gtf.gz", 		GeneReader.class);
		tmp.put("gtf.zip", 		GeneReader.class);
		tmp.put("gvf", 			VariantReader.class);
		tmp.put("gvf.gz", 		VariantReader.class);
		tmp.put("gvf.zip", 		VariantReader.class);
		tmp.put("bed", 			BedReader.class);
		tmp.put("bed.gz", 		BedReader.class);
		tmp.put("bed.zip", 		BedReader.class);
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
	public static <T extends Entity> AbstractReader<T> getReader(String species, File file) throws ReaderException {
		Class<AbstractReader<T>> clazz = getClass(file.getName());
		try {
			return clazz.getConstructor(String.class, File.class).newInstance(species, file);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ReaderException(e);
		}
	}

	/**
	 * Get a reader using the name passed in to find the correct one.
	 *
	 * @param <T> the generic type
	 * @param species the species
	 * @param in the in
	 * @param name the name
	 * @return the reader
	 * @throws ReaderException the reader exception
	 */
	public static <T extends Entity> AbstractReader<T> getReader(String species, InputStream in, String name) throws ReaderException {
		Class<AbstractReader<T>> clazz = getClass(name);
		try {
			return clazz.getConstructor(String.class, InputStream.class).newInstance(species, in);
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
	@SuppressWarnings("unchecked")
	private static <T extends Entity> Class<AbstractReader<T>> getClass(String name) throws ReaderException {
		String ext = FilenameUtils.getExtension(name);
		if (ext==null) throw new ReaderException(name+" does not have an extension!");
		ext = ext.toLowerCase();
		if (!classes.containsKey(ext)) {
			String alt = name.substring(name.indexOf('.')+1);
			if (!classes.containsKey(alt)) {
				throw new ReaderException("There is no reader for "+name);
			}
			ext = alt;
		}
		return (Class<AbstractReader<T>>)classes.get(ext);
	}
}
