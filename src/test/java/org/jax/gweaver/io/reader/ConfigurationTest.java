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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.TimeUnit;

import org.jax.gweaver.io.Configuration;
import org.jax.gweaver.io.Configuration.ZipType;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class ConfigurationTest.
 */
public class ConfigurationTest {

	/**
	 * Empty equals.
	 */
	@Test
	public void emptyEquals() {
		assertEquals(new Configuration().hashCode(), new Configuration().hashCode());
		assertEquals(new Configuration(), new Configuration());
	}
	
	/**
	 * Not equals 1.
	 */
	@Test
	public void notEquals1() {
		assertNotEquals(new Configuration().setZipType(ZipType.NONE), new Configuration());
	}
	
	/**
	 * Not equals 2.
	 */
	@Test
	public void notEquals2() {
		assertNotEquals(new Configuration().setTimeout(2), new Configuration());
	}
	
	/**
	 * Not equals 3.
	 */
	@Test
	public void notEquals3() {
		assertNotEquals(new Configuration().setPartitionLines(100), new Configuration());
	}
	
	/**
	 * Not equals 4.
	 */
	@Test
	public void notEquals4() {
		assertNotEquals(new Configuration().setUnit(TimeUnit.DAYS), new Configuration());
	}
	
	/**
	 * Equals 6.
	 */
	@Test
	public void equals6() {
		assertEquals(new Configuration().setTimeout(30), new Configuration());
	}
	
	/**
	 * Equals 7.
	 */
	@Test
	public void equals7() {
		assertEquals(new Configuration().setPartitionLines(10000), new Configuration());
	}
	
	/**
	 * Not equals 8.
	 */
	@Test
	public void notEquals8() {
		assertNotEquals(new Configuration().setUnit(null), new Configuration());
	}

}
