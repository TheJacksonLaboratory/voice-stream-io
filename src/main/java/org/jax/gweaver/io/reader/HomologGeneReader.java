package org.jax.gweaver.io.reader;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.HomologGene;
import org.jax.gweaver.io.connector.HomologConnector;

/**
 * 
 * 

Homologene records from @see http://www.informatics.jax.org/downloads/reports/HOM_MouseHumanSequence.rpt

Example:

HomoloGene ID	Common Organism Name	NCBI Taxon ID	Symbol	EntrezGene ID	Mouse MGI ID	HGNC ID	OMIM Gene ID	Genetic Location	Genomic Coordinates (mouse: , human: )	Nucleotide RefSeq IDs	Protein RefSeq IDs	SWISS_PROT IDs
3	mouse, laboratory	10090	Acadm	11364	MGI:87867			Chr3 78.77 cM	Chr3:153922357-153944632(-)	NM_007382	NP_031408	P45952
3	human	9606	ACADM	34		HGNC:89	OMIM:607008	Chr1 p31.1	Chr1:75724347-75763679(+)	NM_001286043,NM_000016,NM_001127328,NM_001286042,NM_001286044	NP_001120800,NP_001272971,NP_001272972,NP_001272973,NP_000007	P11310
5	mouse, laboratory	10090	Acadvl	11370	MGI:895149			Chr11 42.96 cM	Chr11:70010183-70015411(-)	NM_017366	NP_059062	P50544
5	human	9606	ACADVL	37		HGNC:92	OMIM:609575	Chr17 p13.1	Chr17:7217125-7225267(+)	NM_000018,NM_001033859,NM_001270448,NM_001270447	NP_001029031,NP_001257377,XP_006721579,XP_011522131,XP_011522132,NP_000009,XP_024306509,NP_001257376	P49748
6	mouse, laboratory	10090	Acat1	110446	MGI:87870			Chr9 29.12 cM	Chr9:53580522-53610350(-)	NM_144784	NP_659033	Q8QZT1
6	human	9606	ACAT1	38		HGNC:93	OMIM:607809	Chr11 q22.3	Chr11:108121531-108148168(+)	NM_001386689,NM_001386688,NM_001386687,NM_001386686,NM_001386691,NM_001386678,NM_000019,NM_001386677,NM_001386685,NM_001386682,NM_001386679,NM_001386681,NM_001386690	NP_001373615,NP_001373617,NP_001373618,NP_001373619,NP_001373620,XP_016873171,XP_024304282,NP_001373607,NP_001373606,NP_000010,NP_001373608,NP_001373610,NP_001373611,NP_001373614,NP_001373616	P24752
7	mouse, laboratory	10090	Acvr1	11477	MGI:87911			Chr2 33.05 cM	Chr2:58446438-58566828(-)	NM_001355049,NM_001110205,NM_001355048,NM_007394,NM_001110204,XM_006497622	NP_001341978,NP_001103675,NP_001103674,NP_001341977,NP_031420,XP_006497685	P37172
7	human	9606	ACVR1	90		HGNC:171	OMIM:102576	Chr2 q24.1	Chr2:157736446-157875880(-)	NM_001347667,NM_001347666,NM_001347665,NM_001347664,NM_001347663,NM_001111067,NM_001105	NP_001334594,NP_001334595,NP_001334596,XP_006712888,XP_011510410,NP_001096,NP_001104537,NP_001334592,NP_001334593	Q04771
9	mouse, laboratory	10090	Sgca	20391	MGI:894698			Chr11 59.01 cM	Chr11:94962791-94976327(-)	XM_011248836,NM_009161	NP_033187,XP_011247138	P82350
9	human	9606	SGCA	6442		HGNC:10805	OMIM:600119	Chr17 q21.33	Chr17:50165517-50175932(+)	NM_001135697,NM_000023	XP_011523422,NP_001129169,NP_000014,XP_024306641,XP_011523426,XP_011523425,XP_011523424,XP_011523423	Q16586
12	mouse, laboratory	10090	Adsl	11564	MGI:103202			Chr15 37.95 cM	Chr15:80948490-80970946(+)	NM_009634	NP_033764	P54822
12	human	9606	ADSL	158		HGNC:291	OMIM:608222	Chr22 q13.1	Chr22:40346500-40387408(+)	NM_000026,NM_001363840,NM_001317923,NM_001123378	XP_016884128,XP_024307934,XP_016884129,XP_016884127,XP_016884126,XP_016884125,XP_011528282,XP_011528279,NP_001350769,NP_001304852,NP_001116850,NP_000017	P30566

e.g. for human
HomoloGene ID									3
Common Organism Name							human
NCBI Taxon ID									9606
Symbol											ACADM
EntrezGene ID									34
Mouse MGI ID									HGNC:89
HGNC ID	OMIM Gene ID							OMIM:607008
Genetic Location								Chr1 p31.1
Genomic Coordinates (mouse: , human: )			Chr1:75724347-75763679(+)
Nucleotide RefSeq IDs							NM_001286043,NM_000016,NM_001127328,NM_001286042,NM_001286044
Protein RefSeq IDs								NP_001120800,NP_001272971,NP_001272972,NP_001272973,NP_000007
SWISS_PROT IDs									P11310

e.g. for mouse
HomoloGene ID									12
Common Organism Name							mouse, laboratory
NCBI Taxon ID									10090
Symbol											Adsl
EntrezGene ID									11564
Mouse MGI ID									MGI:103202
HGNC ID	OMIM Gene ID							
Genetic Location								Chr11 59.01 cM
Genomic Coordinates (mouse: , human: )			Chr11:94962791-94976327(-)
Nucleotide RefSeq IDs							XM_011248836,NM_009161
Protein RefSeq IDs								P_033187,XP_011247138
SWISS_PROT IDs									P82350

 * 
 * @author gerrim
 *
 */
class HomologGeneReader<T extends Entity> extends AbstractScanner<T> {

	/**
	 * Instantiates a new gene reader.
	 *
	 * @param request
	 * @throws IOException 
	 */
	@Override
	public HomologGeneReader<T> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\t+"); // Must be a tab only as spaces are allowed
		return this;
	}

	@Override
	public <U extends Entity> Function<T, Stream<U>> getDefaultConnector() {
		return new HomologConnector<>(); // Creates connections for two consecutive lines.
	}

	@Override
	protected T create(String line) throws ReaderException {
		
		if (line.startsWith("HomoloGene ID")) return null;
		
		String[] rec = line.split(getDelimiter());
		HomologGene gene = new HomologGene();
		gene.setSource(request.getSource());
		
		int i = 0;
		gene.setHid(Long.parseLong(rec[i++]));
		gene.setOrganismName(rec[i++]);
		gene.setTaxonId(Long.parseLong(rec[i++]));
		gene.setSymbol(rec[i++]);
		gene.setEntrezId(Long.parseLong(rec[i++]));
		gene.setMgiId(rec[i++]);
		
		boolean isHum = gene.getOrganismName().equalsIgnoreCase("human");
		if (isHum) {
			if (rec.length>6) gene.setHgncId(rec[i++]);
		}
		
		// Sometimes data at the end is missing.
		if (isHum) {
			if (rec.length>7) gene.setLocation(rec[i++]);
			if (rec.length>8) gene.setCoords(rec[i++]);
			if (rec.length>9) gene.setNucelotideSeqIds(Arrays.asList(rec[i++].split(",")));
			if (rec.length>10) gene.setProteinSeqIds(Arrays.asList(rec[i++].split(",")));
			if (rec.length>11) gene.setSwissProtIds(Arrays.asList(rec[i++].split(",")));
		} else {
			if (rec.length>6) gene.setLocation(rec[i++]);
			if (rec.length>7) gene.setCoords(rec[i++]);
			if (rec.length>8) gene.setNucelotideSeqIds(Arrays.asList(rec[i++].split(",")));
			if (rec.length>9) gene.setProteinSeqIds(Arrays.asList(rec[i++].split(",")));
			if (rec.length>10) gene.setSwissProtIds(Arrays.asList(rec[i++].split(",")));
		}
		
		return (T)gene;
	}

}
