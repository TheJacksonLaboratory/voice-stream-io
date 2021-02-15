package org.jax.gweaver.io.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jax.gweaver.domain.NamedEntity;
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
