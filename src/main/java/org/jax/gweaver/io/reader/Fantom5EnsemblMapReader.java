package org.jax.gweaver.io.reader;

import java.util.function.Function;
import java.util.stream.Stream;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.Fantom5Link;
import org.jax.gweaver.domain.NamedEntity;

/**

Example:

## UID CHROM F5_CHROM_START F5_CHROM_END GENE_TARGET ENSEMBL_ID(S)
0	1	66331609	66733058	PDE4B	ENSR00000008110|ENSR00000008134|ENSR00000008117|ENSR00000008081|ENSR00000008071|ENSR00000008088|ENSR00000008109|ENSR00000008083|ENSR00000008080|ENSR00000008072|ENSR00000008127|ENSR00000008105|ENSR00000008132
1	1	66415892	66752957	TCTEX1D1	ENSR00000008110|ENSR00000008134|ENSR00000008117|ENSR00000008088|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
2	1	66438978	66930743	MIER1	ENSR00000008110|ENSR00000008134|ENSR00000008137|ENSR00000008117|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
3	1	66445327	66930743	MIER1	ENSR00000008110|ENSR00000008134|ENSR00000008137|ENSR00000008117|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132
4	1	66489150	66930743	MIER1	ENSR00000008110|ENSR00000008134|ENSR00000008137|ENSR00000008117|ENSR00000008109|ENSR00000008127|ENSR00000008105|ENSR00000008132

 * @author gerrim
 *
 * @param <N>
 */
class Fantom5EnsemblMapReader<N extends NamedEntity> extends AbstractScanner<N> {

	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Fantom5EnsemblMapReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\\t+");
		return this;
	}

	@Override
	public <U extends Entity> Function<N, Stream<U>> getDefaultConnector() {
		return null;
	}

	@Override
	protected N create(String line) throws ReaderException {
		
		String[] rec = line.split(getDelimiter());
		
		Fantom5Link link = new Fantom5Link();
		link.setUid(Long.parseLong(rec[0]));
		link.setChrom("chr"+rec[1]);
		link.setStart(Integer.parseInt(rec[2]));
		link.setEnd(Integer.parseInt(rec[3]));
		link.setName(rec[4]);
		link.addEnsemblIds(rec[5].split("|"));
		
		return (N)link;
	}

}
