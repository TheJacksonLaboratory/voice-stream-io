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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geneweaver.domain.EQTL;
import org.geneweaver.domain.Entity;  

/**
 * 

Example of GTEx analysis file.
@see https://storage.googleapis.com/gtex_analysis_v8/single_tissue_qtl_data/GTEx_Analysis_v8_eQTL.tar

Example of lookup:
@see https://storage.googleapis.com/gtex_analysis_v8/reference/GTEx_Analysis_2017-06-05_v8_WholeGenomeSeq_838Indiv_Analysis_Freeze.lookup_table.txt.gz

// eGenes file example.
gene_id	gene_name	gene_chr	gene_start	gene_end	strand	num_var	beta_shape1	beta_shape2	true_df	pval_true_df	variant_id	tss_distance	chr	variant_pos	ref	alt	num_alt_per_site	rs_id_dbSNP151_GRCh38p7	minor_allele_samples	minor_allele_count	maf	ref_factor	pval_nominal	slope	slope_se	pval_perm	pval_beta	qval	pval_nominal_threshold	log2_aFC	log2_aFC_lower	log2_aFC_upper
ENSG00000227232.5	WASH7P	chr1	14410	29553	-	1364	1.02984	294.487	455.958	6.29063e-08	chr1_64764_C_T_b38	35211	chr1	64764	C	T	1	rs769952832	70	71	0.0611015	1	1.01661e-08	0.586346	0.100677	9.999e-05	1.32112e-05	1.01141e-05	0.000505559	0.584194	0.435298	0.744545
ENSG00000268903.1	RP11-34P13.15	chr1	135141	135895	-	1863	1.04872	330.017	441.174	0.00088883	chr1_103147_C_T_b38	-32748	chr1	103147	C	T	1	rs866355763	18	18	0.0154905	1	0.000347332	-0.612097	0.169958	0.241904	0.2337	0.0810742	0.000472534	-1.823931	-4.015491	-0.676333
ENSG00000269981.1	RP11-34P13.16	chr1	137682	137965	-	1868	1.0494	358.23	449.483	0.000308725	chr1_108826_G_C_b38	-29139	chr1	108826	G	C	1	rs62642117	40	40	0.0344234	1	0.000119525	0.431229	0.111226	0.0926907	0.0917911	0.0375434	0.000436045	0.771338	0.523984	1.052938
ENSG00000241860.6	RP11-34P13.13	chr1	141474	173862	-	2066	1.03665	389.634	449.333	4.79733e-07	chr1_14677_G_A_b38	-159185	chr1	14677	G	A	1	rs201327123	60	60	0.0516351	1	7.91948e-08	0.700658	0.128601	0.00039996	0.000134301	9.22675e-05	0.000388632	1.395702	1.081218	1.959566
ENSG00000279457.4	RP11-34P13.18	chr1	185217	195411	-	2234	1.05269	393.374	442.596	1.04042e-06	chr1_599167_G_A_b38	403756	chr1	599167	G	A	1	rs188376087	50	51	0.0438898	1	1.53694e-07	-0.687794	0.12923	0.00019998	0.000265081	0.000175637	0.000400259	-0.813716	-1.236065	-0.532370
ENSG00000228463.9	AP006222.2	chr1	257864	297502	-	2799	1.05699	499.496	449.371	4.95379e-18	chr1_280550_G_A_b38	-16952	chr1	280550	G	A	1	rs1206875823	17	17	0.0146299	1	2.72051e-20	-1.82203	0.189149	9.999e-05	3.5488e-16	6.26848e-16	0.000318503	-6.643856	-6.643856	-6.643856

// Pairs file example:
variant_id	gene_id	tss_distance	ma_samples	ma_count	maf	pval_nominal	slope	slope_se	pval_nominal_threshold	min_pval_nominal	pval_beta
chr1_64764_C_T_b38	ENSG00000227232.5	35211	70	71	0.0611015	1.01661e-08	0.586346	0.100677	0.000505559	1.01661e-08	1.32112e-05
chr1_665098_G_A_b38	ENSG00000227232.5	635545	122	129	0.111015	8.624e-07	0.384453	0.0771678	0.000505559	1.01661e-08	1.32112e-05
chr1_666028_G_A_b38	ENSG00000227232.5	636475	106	113	0.0972461	6.21075e-06	0.371935	0.0814417	0.000505559	1.01661e-08	1.32112e-05
chr1_108826_G_C_b38	ENSG00000269981.1	-29139	40	40	0.0344234	0.000119525	0.431229	0.111226	0.000436045	0.000119525	0.0917911
chr1_126108_G_A_b38	ENSG00000269981.1	-11857	73	73	0.0628227	0.000141096	-0.338384	0.0882293	0.000436045	0.000119525	0.0917911
chr1_133160_G_A_b38	ENSG00000269981.1	-4805	81	82	0.070568	0.000195804	-0.311613	0.0830605	0.000436045	0.000119525	0.0917911
chr1_134234_G_A_b38	ENSG00000269981.1	-3731	79	80	0.0688468	0.000217605	-0.310923	0.0834836	0.000436045	0.000119525	0.0917911
chr1_135032_G_A_b38	ENSG00000269981.1	-2933	77	78	0.0671256	0.000245623	-0.311533	0.084361	0.000436045	0.000119525	0.0917911
chr1_502653_G_T_b38	ENSG00000269981.1	364688	75	76	0.0654045	0.000180376	-0.322783	0.0855567	0.000436045	0.000119525	0.0917911
chr1_14677_G_A_b38	ENSG00000241860.6	-159185	60	60	0.0516351	7.91948e-08	0.700658	0.128601	0.000388632	7.91948e-08	0.000134301


Mouse eQTL information.
@see https://churchilllab.jax.org/qtlviewer/DO/hippocampus
@see https://churchilllab.jax.org/qtlviewer/DO/DrugNaiveStriatum
@see https://churchilllab.jax.org/qtlviewer/attie/islets

You can either have:
1. (Gene)-[EQTL]-(Variant)
2. (Gene)-[eLINK]-(EQTL)-[LOOKUP]-(Variant)

I think 1. is correct because EQTL var id is not unique i.e. there might be links with the same eqtlVariantId. This means
that you cannot create a node from EQTL and use eqtlVariantId as its unique id.

 * @author gerrim
 *
 * @param <N>
 */
class GTExEQTLReader<N extends Entity> extends LineIteratorReader<N> {

	/**
	 * Create the reader by setting its data
	 * 
	 * @param request
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public GTExEQTLReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\\t+");
		return this;
	}
	
	private Map<String,Integer> indices;

	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
		
		String[] segs = line.split(getDelimiter());
		if (line.startsWith("gene_id") || line.startsWith("variant_id")) {
			indices = parseIndices(segs);
			return null; // It's a header.
		}
		
		EQTL ret = new EQTL();
		String fullGeneId = segs[indices.get("gene_id")];
		ret.setGeneId(clean(fullGeneId));
		ret.setFullGeneId(fullGeneId);
		setVariantInfo(ret, segs[indices.get("variant_id")]);
		ret.setSlope(Double.parseDouble(segs[indices.get("slope")]));
		setTissueInfo(ret);
		
		String fileSrc = indices.get("gene_id")==0
					   ? "eGenes" : "pairs";
		ret.setSource("GTEx ("+fileSrc+")");
		
		// We can sometimes find rs_id, in which case, use it.
		if (indices.containsKey("rs_id_dbsnp151_grch38p7")) {
			ret.setRsId(segs[indices.get("rs_id_dbsnp151_grch38p7")]);
		}
		
		return (N)ret;
	}

	/** Example names:
	 
	 Brain_Amygdala.v8.egenes.txt.gz							Liver.v8.signif_variant_gene_pairs.txt.gz
	 Brain_Amygdala.v8.signif_variant_gene_pairs.txt.gz			Lung.v8.egenes.txt.gz
	 Brain_Anterior_cingulate_cortex_BA24.v8.egenes.txt.gz		Lung.v8.signif_variant_gene_pairs.txt.gz

	 * @return
	 */
	private void setTissueInfo(EQTL ret) {
		String name = getEntryName();
		if (entryName==null) {
			name = request.name();
		} else {
			name = name.substring(name.indexOf('/')+1);
		}
		if (name==null) return;
		
		String[] frags = name.split("\\.");
		ret.setTissue(frags[0].replace('_', ' '));
		ret.setVersion(frags[1]);
	}

	private Map<String, Integer> parseIndices(String[] segs) {
		Map<String, Integer> indices = new HashMap<>();
		for (int i = 0; i < segs.length; i++) {
			indices.put(segs[i].toLowerCase(), i);
		}
		return indices;
	}

	private String clean(String geneId) {
		if (geneId.indexOf('.')>0) {
			return geneId.substring(0, geneId.indexOf('.'));
		}
		return geneId;
	}

	private void setVariantInfo(EQTL ret, String varId) {
		ret.setEqtlVariantId(varId);
		String[] sa = parseVariantId(varId);
		ret.setChr(sa[0]);
		ret.setRefSeq(sa[2]);
		ret.setAltSeq(sa[3]);
	}

	private static final Pattern vIdPattern = Pattern.compile("^(chr[0-9XYM]+)_(\\d+)_([A-Z]+)_([A-Z]+)_b38$");
	
	/**
	 * Parse Id of the form: {chr}_{pos_first_ref_base}_{ref_seq}_{alt_seq}_b38
	 * @return String[] just because its faster than making a map.
	 * examples:chr1_126108_G_A_b38, chr1_595505_GCTTCCCAGCAGTGCAGA_G_b38, chr1_632946_G_A_b38
	 */
	String[] parseVariantId(String vId) {
		Matcher m = vIdPattern.matcher(vId);
		if (m.matches()) {
			return new String[] {m.group(1), m.group(2), m.group(3), m.group(4)};
		} else {
			return null;
		}
	}
}
