package org.jax.voice.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.jax.voice.domain.VariantCall;
import org.jax.voice.io.reader.FastVCFReader;
import org.jax.voice.io.reader.ReaderException;
import org.jax.voice.io.reader.ReaderFactory;
import org.jax.voice.io.reader.ReaderRequest;
import org.jax.voice.io.reader.StreamReader;
import org.junit.Ignore;
import org.junit.Test;

public class FastVCFReaderTest extends AbstractDataFileTest {

	@Test
	public void direct1() throws Exception {
		FastVCFReader<VariantCall> reader = new FastVCFReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/vcf/chr20.trunc.vcf.gz")));
		
		List<VariantCall> found = reader.stream().collect(Collectors.toList());
		assertNotNull(found);
		assertEquals(747, found.size());
		check(found);
	}
	
	@Test
	public void direct2() throws Exception {
		FastVCFReader<VariantCall> reader = new FastVCFReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/vcf/chr22.trunc.vcf")));
		
		List<VariantCall> found = reader.stream().collect(Collectors.toList());
		assertNotNull(found);
		assertEquals(747, found.size());
		check(found);
	}

	@Test
	public void factory() throws Exception {
		
		StreamReader<VariantCall> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/vcf/chr21.trunc.vcf.gz")));		
		List<VariantCall> found = reader.stream().collect(Collectors.toList());
		assertNotNull(found);
		assertEquals(747, found.size());
		check(found);
	}
	
	private void check(List<VariantCall> found) {
		
		found.forEach(v->{
			assertNotNull(v.getId());
		});
	}

	@Test(expected=IllegalArgumentException.class)
	public void wrongFileFormat() throws Exception {
		
		FastVCFReader<VariantCall> reader = new FastVCFReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/1000/hs_gtf/hg38_2.gtf")));

		reader.stream().collect(Collectors.toList());
	}
	
	/* 
	 * -----------------------------------------------------
	 * Ignored big files but useful for checking performance.
	 * -----------------------------------------------------
	 */
	@Ignore
	@Test
	public void fullScale1() throws Exception {
		big("/Volumes/jax-data/data/variant-orthology/vcf/ALL.chr20.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz", 629339);
	}
	
	@Ignore
	@Test
	public void fullScale2() throws Exception {
		big("/Volumes/jax-data/data/variant-orthology/vcf/ALL.chr21.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz", 861528);
	}
	
	@Ignore
	@Test
	public void fullScale23() throws Exception {
		big("/Volumes/jax-data/data/variant-orthology/vcf/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz", 598166);
	}

	private void big(String spath, int expected) throws ReaderException, IOException {
		Path path = Paths.get(spath);
		StreamReader<?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", path));
		
		long start = System.currentTimeMillis();
		long items = reader.stream().count();
		long end = System.currentTimeMillis();
		double tpv = (end-start)/(double)items;
		System.out.println(String.format("Read %s VariantCalls in %6.3g ms/call", items, tpv));
		assertEquals(expected, items);
	}
	
	/**
	 * For me samtools reads the files properly in 
	 * @throws Exception
	 */
//	@Test
//	public void fullScaleSamTools() throws Exception {
//		bigSamtools("/Volumes/jax-data/data/variant-orthology/vcf/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz", 
//					"/Volumes/jax-data/data/variant-orthology/vcf/ALL.chr22.phase3_shapeit2_mvncall_integrated_v5a.20130502.genotypes.vcf.gz.tbi",
//					598166);
//	}
//	
//	private void bigSamtools(String spath, String indPath, int expected) throws ReaderException, IOException {
//		Path path = Paths.get(spath);
//		Path iPath = Paths.get(indPath);
//		VCFFileReader reader = new VCFFileReader(path.toFile(), iPath.toFile(), false);
//		
//		long start = System.currentTimeMillis();
//		CloseableIterator<VariantContext> it = reader.iterator();
//		List<Long> count = new ArrayList<>();
//		count.add(0L);
//		it.forEachRemaining(c->count.set(0, count.get(0)+1));
//		long end = System.currentTimeMillis();
//		long items = count.get(0);
//		double tpv = (end-start)/(double)items;
//		System.out.println(String.format("Read %s VariantCalls in %6.3g ms/call", items, tpv));
//		assertEquals(expected, items);
//	}

}
