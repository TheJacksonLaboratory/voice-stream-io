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
 */
package org.jax.gweaver.domain;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.jax.gweaver.io.reader.ReaderRequest;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.jemos.podam.api.RandomDataProviderStrategyImpl;

/**
 * Test serialization of HomologyRows
 * 
 * @author gerrim
 *
 */
public class SerializationTest {

	private final static ObjectMapper mapper = new ObjectMapper();
	static {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		mapper.setDateFormat(df);
	}
	
	protected static final PodamFactory factory;
	static{
		factory = new PodamFactoryImpl();
		
		RandomDataProviderStrategyImpl strategy = new RandomDataProviderStrategyImpl();
		factory.setStrategy(strategy);
	}	
	
	@SuppressWarnings("rawtypes")
	private Collection<Class> testClasses;
	
	/**
	 * Get the classes for the test
	 * @throws Throwable
	 */
	@Before
	public void before() throws Throwable {
		
		ClassPath cp = ClassPath.from(getClass().getClassLoader());
		Collection<ClassInfo> info = cp.getTopLevelClassesRecursive(getClass().getPackageName());
		
		testClasses = new HashSet<>();
		
		for (ClassInfo classInfo : info) {
			if (classInfo.getName().endsWith("Test")) continue;

			@SuppressWarnings("rawtypes")
			Class clazz = getClass().getClassLoader().loadClass(classInfo.getName());
			if (clazz.isInterface()) continue;
			if (Modifier.isAbstract( clazz.getModifiers())) continue;
			if (!Modifier.isPublic(clazz.getModifiers())) continue;

			testClasses.add(clazz);
		}

		// Add any other Test classes
		testClasses.add(ReaderRequest.class);
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void emptyConstructor() throws Exception {
		for (@SuppressWarnings("rawtypes") Class clazz : testClasses) {
			Object empty = clazz.getConstructor().newInstance();
			round(empty, clazz);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testToString() throws Exception {
		for (@SuppressWarnings("rawtypes") Class clazz : testClasses) {
			Object empty = clazz.getConstructor().newInstance();
			empty.toString();
		}
	}

	
	public void dummyConstructors() throws Exception {
		for (@SuppressWarnings("rawtypes") Class clazz : testClasses) {
			Constructor<?>[] constructors = clazz.getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				int nnulls = constructor.getParameters().length;
				if (nnulls<1) continue;
				Object[] nulls = new Object[nnulls];
				Arrays.fill(nulls, null);
				
				try {
					constructor.newInstance(nulls);
				} catch (Exception ignored) {
					// Trying to fake coverage
					// TODO Something useful!
				}
			}
		}
	}

		
	@SuppressWarnings("unchecked")
	@Test
	public void randomFields() throws Throwable {
		
		for (@SuppressWarnings("rawtypes") Class clazz : testClasses) {
			Object rand = factory.manufacturePojo(clazz);
			round(rand, clazz);
		}
	}
	
	@Test
	public void csvWriting() throws Throwable {
		
		Path dir = Paths.get("tmp/csvwriting/");
		dir.toFile().mkdirs();
		
		for (@SuppressWarnings("rawtypes") Class clazz : testClasses) {
			checkCanWrite(dir, clazz);
		}

	}

	private void checkCanWrite(Path dir, @SuppressWarnings("rawtypes") Class clazz) throws IOException {
		Path csv = dir.resolve(clazz.getSimpleName()+".csv");
		try (BufferedWriter writer = Files.newBufferedWriter(csv)) {
			@SuppressWarnings("unchecked")
			Object rand = factory.manufacturePojo(clazz);
			if (rand instanceof Entity) {
				Entity ent = (Entity)rand;
				if (ent.getHeader()==null) return; // Objects are not forced to implement this.
				writer.write(ent.getHeader());
				writer.newLine();
				writer.write(ent.toCsv());
				writer.newLine();
			}
		}	
	}

	private <T> T round(T from, Class<T> clazz) throws Exception {
		return round(from, clazz, true);
	}
	
	private <T> T round(T from, Class<T> clazz, boolean equals) throws Exception {
		
		String json = mapper.writeValueAsString(from);
		T to = mapper.readValue(json, clazz);
		if (equals) {
			if (clazz.isArray()) {
				assertArrayEquals((Object[])from, (Object[])to);
			} else {
				assertEquals(from, to);
				assertEquals(from.hashCode(), to.hashCode());
			}
		} else {
			assertNotEquals(from, to);
		}
		return to;
	}
}
