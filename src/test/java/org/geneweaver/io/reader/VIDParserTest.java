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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.geneweaver.domain.NamedEntity;
import org.junit.Test;

public class VIDParserTest {

	
	private GTExEQTLReader<NamedEntity> reader = new GTExEQTLReader<>();
	
	@Test
	public void parse1() throws Exception {
		test("chr1_126108_G_A_b38", "chr1", "126108", "G", "A");
	}
	
	@Test
	public void parse2() throws Exception {
		test("chr1_595505_GCTTCCCAGCAGTGCAGA_G_b38", "chr1", "595505", "GCTTCCCAGCAGTGCAGA", "G");
	}
	
	@Test
	public void parse3() throws Exception {
		test("chr1_632946_G_A_b38", "chr1", "632946", "G", "A");
	}

	private void test(String vId, String... ret) {
		String[] inf = reader.parseVariantId(vId);
		assertNotNull(inf);
		assertEquals(4, inf.length);
		assertArrayEquals(inf, 	ret);
	}
}
