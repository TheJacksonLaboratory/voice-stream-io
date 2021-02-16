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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
	public static Entity save(Entity e, Map<Class<? extends Entity>, BufferedWriter> writers, Path dir, Timer timer) {
		
		try {
			if (!writers.containsKey(e.getClass())) {
				BufferedWriter header = Files.newBufferedWriter(dir.resolve(e.getClass().getSimpleName()+"-header.csv"));
				header.write(e.getHeader());
				header.newLine();
				header.close();
	
				Path pbody = dir.resolve(e.getClass().getSimpleName()+".csv.gz");
				BufferedWriter body = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(pbody.toFile()))));
				writers.put(e.getClass(), body);
			}
	
			writers.get(e.getClass()).write(e.toCsv());
			writers.get(e.getClass()).newLine();
			if (timer!=null) {
				timer.time();
			}
		} catch (IOException ne) {
			throw new RuntimeException(ne);
		}

		return e;
	}

}
