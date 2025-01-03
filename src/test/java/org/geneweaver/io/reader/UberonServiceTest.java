package org.geneweaver.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UberonServiceTest {

	
	private UberonService service = new UberonService();
	
	@Test
	public void notEmpty() {
		assertTrue(!service.isEmpty());
	}
	
	@Test
	public void nullIsNull() {
		assertNull(service.getUberonCode(null));
	}
	
	@Test
	public void notThere() {
		assertNull(service.getUberonCode("NOT THERE"));
	}
	
	@Test
	public void heart() {
		assertEquals("UBERON:0000948", service.getUberonCode("heart"));
	}

	@Test
	public void skin() {
		assertEquals("UBERON:0002097", service.getUberonCode("SKIN"));
	}

	@Test
	public void skinSunExposed() {
		assertEquals("UBERON:0002097", service.getUberonCode("SKIN sun exposed"));
	}

	@Test
	public void skinSupraPubic() {
		assertEquals("UBERON:0036149", service.getUberonCode("skin Suprapubic"));
	}
	
	@Test
	public void smallIntestineIleum() {
		assertEquals("UBERON:0002116", service.getUberonCode("Small_Intestine_Terminal_Ileum"));
	}

	@Test
	public void brainCaudateBasalGanglia() {
		assertEquals("UBERON:0002420", service.getUberonCode("Brain_Caudate_basal_ganglia"));
	}

	@Test
	public void heartLeftVentricle() {
		assertEquals("UBERON:0002084", service.getUberonCode("Heart_Left_Ventricle"));
	}

	@Test
	public void bone() {
		assertEquals("UBERON:0002481", service.getUberonCode("Bone"));
	}
	
	@Test
	public void hippocampus() {
		assertEquals("UBERON:0002421", service.getUberonCode("Hippocampus"));
	}
	
	@Test
	public void striatum() {
		assertEquals("UBERON:0002435", service.getUberonCode("Striatum"));
	}

}
