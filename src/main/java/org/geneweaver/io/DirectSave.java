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
package org.geneweaver.io;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.geneweaver.domain.Entity;

/**
 * A simple function for importing static to find readers in a map 
 * and save to them.
 * 
 * @author gerrim
 *
 */
public class DirectSave {

	/**
	 * This function uses the map to passed in to cache writers.
	 * @param e
	 * @param writers
	 * @param dir
	 * @param timer
	 * @return
	 */
	public static Entity save(Entity e, Map<Class<? extends Entity>, Map<String,BufferedWriter>> writers, Path dir, Timer timer) {
		return save(e, writers, dir, timer, false);
	}
	
	/**
	 * This function uses the map passed in to cache writers.
	 * It is synchronized to avoid writing to the same file at the same
	 * time if multiple threads are used. 
	 * 
	 * @param e
	 * @param writers
	 * @param dir
	 * @param timer - may be null
	 * @return
	 */
	public static Entity save(Entity e, Map<Class<? extends Entity>, Map<String,BufferedWriter>> writers, Path dir, Timer timer, boolean append) {
		
		synchronized(e.getClass()) {
			try {
				if (!writers.containsKey(e.getClass())) {
					BufferedWriter header = Files.newBufferedWriter(dir.resolve(e.getClass().getSimpleName()+"-header.csv"));
					header.write(e.getHeader());
					header.newLine();
					header.close();
					
					writers.put(e.getClass(), Collections.synchronizedMap(new HashMap<>()));
				}
				
				Map<String,BufferedWriter> brs = writers.get(e.getClass());
				String chr = e.getChr();
				if (chr==null) {
					throw new IllegalArgumentException("Null chromosome encountered writing "+e);
				}
				if (!brs.containsKey(chr)) {
					Path pbody;
					if (chr.equals(Entity.NO_CHR)) {
						pbody = dir.resolve(e.getClass().getSimpleName()+".csv.gz");
					} else {
						pbody = dir.resolve(e.getClass().getSimpleName()+"-chr"+chr+".csv.gz");
					}
					BufferedWriter body = createWriter(pbody, append);
					brs.put(chr, body);
				}
				
				BufferedWriter writer = writers.get(e.getClass()).get(chr);
				writer.write(e.toCsv());
				writer.newLine();
				if (timer!=null) {
					timer.time();
				}
			} catch (IOException ne) {
				throw new RuntimeException(ne);
			}
		}

		return e;
	}
	
	/**
	 * Create a GZip writer for use with writing bulk import files for neo4j,
	 * or in fact any gzip file.
	 * 
	 * @param pbody
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static BufferedWriter createWriter(File pbody, boolean append) throws FileNotFoundException, IOException {
		return createWriter(pbody.toPath(), append);
	}

	/**
	 * Create a GZip writer for use with writing bulk import files for neo4j,
	 * or in fact any gzip file.
	 * 
	 * @param pbody
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static BufferedWriter createWriter(Path pbody, boolean append) throws FileNotFoundException, IOException {
		BufferedWriter body = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(pbody.toFile(), append))));
		return body;
	}

}
