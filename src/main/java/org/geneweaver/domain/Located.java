package org.geneweaver.domain;

import java.util.UUID;

/**
 * Some entities have a location.
 * 
 * Important: We use ints
 * 
For the human genome (GRCh38):
Chromosome 1 length: ~248,956,422 bp
Largest chromosome length: < 2.5 × 10^8
Below the 32-bit signed int limit (≈ 2.1 × 10^9).
 
For mouse (Mus musculus, GRCm39 genome build):
Longest chromosome is chromosome 1 at about 195,154,279 bp
All other autosomes and sex chromosomes are shorter
Every mouse chromosome is under the 2.1 billion signed 32-bit int limit.

If we ever use Axolotl (Ambystoma mexicanum) it has a genome size of about 32 billion bp
and some chromosomes are longer than 2.1 billion so we would need to use Longs there.
 * 
 * @author gerrim
 *
 */
public interface Located extends IdGenerator {

	/**
	 * The start location (base pairs).
	 * @return
	 */
	Integer getStart(); // Int is okay for human and mouse genomes.
	
	/**
	 * The end location (base pairs).
	 * @return
	 */
	Integer getEnd(); // Int is okay for human and mouse genomes.
	
	/**
	 * The chromosome on which the entity is located.
	 * Should not start with the text 'chr'
	 * @return
	 */
	String getChr();
	
	static Located at(String chr, Integer start, Integer end) {
		final UUID rand = UUID.randomUUID();
		return new Located() {
			
			@Override
			public String id() {
				return rand.toString();
			}
			
			@Override
			public Integer getStart() {
				return start;
			}
			
			@Override
			public Integer getEnd() {
				return end;
			}
			
			@Override
			public String getChr() {
				return chr;
			}
		};
	}
}
