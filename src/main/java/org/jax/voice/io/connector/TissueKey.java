package org.jax.voice.io.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jax.voice.domain.Sample;

/**
 * The file names in GTEx hold the tissue information.
 * The file GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt contains the tissue group 
 * and tissue name (official). These have been mangled into the file name instead of having 
 * a column with a unique key. Therefore we have to search in the samples for each file
 * name. 
 * 
 * In the files there are 700 mil declarations therefore we certainly do not want to search
 * in the sample list for every one as prevous implementations have done.
 * 
 * Example file name to match:
 * Brain_Anterior_cingulate_cortex_BA24.v8.signif_variant_gene_pairs.txt.gz
 * Brain_Putamen_basal_ganglia.v8.egenes.txt.gz
 * 
 * Example corresponding tissue group and name:
 * Brain - Anterior cingulate cortex (BA24)
 * Brain - Caudate (basal ganglia)
 * 
 * We use the following idea:
 * 1. The first word in the tissue group and the file name is used as a hashCode
 * 2. All the words are matched ignoring ( etc. in the equals. 
 * 
 * 
 * Example names from parsed file names:

Adipose Subcutaneous
Adipose Visceral Omentum
Adrenal Gland
Artery Aorta
Artery Coronary
Artery Tibial
Brain Amygdala
Brain Anterior cingulate cortex BA24
Brain Caudate basal ganglia
Brain Cerebellar Hemisphere
Brain Cerebellum
Brain Cortex
Brain Frontal Cortex BA9
Brain Hippocampus
Brain Hypothalamus
Brain Nucleus accumbens basal ganglia
Brain Putamen basal ganglia
Brain Spinal cord cervical c-1
Brain Substantia nigra
Breast Mammary Tissue
Cells Cultured fibroblasts
Cells EBV-transformed lymphocytes
Colon Sigmoid
Colon Transverse
Esophagus Gastroesophageal Junction
Esophagus Mucosa
Esophagus Muscularis
Heart Atrial Appendage
Heart Left Ventricle
Kidney Cortex
Liver
Lung
Minor Salivary Gland
Muscle Skeletal
Nerve Tibial
Ovary
Pancreas
Pituitary
Prostate
Skin Not Sun Exposed Suprapubic
Skin Sun Exposed Lower leg
Small Intestine Terminal Ileum
Spleen
Stomach
Testis
Thyroid
Uterus
Vagina
Whole Blood

Example names from sample file after trim() on Sample:
Adipose Tissue Subcutaneous
Adipose Tissue Visceral (Omentum)
Adrenal Gland Adrenal Gland
Bladder Bladder
Blood Vessel Aorta
Blood Vessel Coronary
Blood Vessel Tibial
Blood EBV-transformed lymphocytes
Blood Whole Blood
Bone Marrow Leukemia cell line (CML)
Brain Amygdala
Brain Anterior cingulate cortex (BA24)
Brain Caudate (basal ganglia)
Brain Cerebellar Hemisphere
Brain Cerebellum
Brain Cortex
Brain Frontal Cortex (BA9)
Brain Hippocampus
Brain Hypothalamus
Brain Nucleus accumbens (basal ganglia)
Brain Putamen (basal ganglia)
Brain Spinal cord (cervical c-1)
Brain Substantia nigra
Breast Mammary Tissue
Cervix Uteri Ectocervix
Cervix Uteri Endocervix
Colon Sigmoid
Colon Transverse
Esophagus Gastroesophageal Junction
Esophagus Mucosa
Esophagus Muscularis
Fallopian Tube Fallopian Tube
Heart Atrial Appendage
Heart Left Ventricle
Kidney Cortex
Kidney Medulla
Liver Liver
Lung Lung
Muscle Skeletal
Nerve Tibial
Ovary Ovary
Pancreas Pancreas
Pituitary Pituitary
Prostate Prostate
Salivary Gland Minor Salivary Gland
Skin Cultured fibroblasts
Skin Not Sun Exposed (Suprapubic)
Skin Sun Exposed (Lower leg)
Small Intestine Terminal Ileum
Spleen Spleen
Stomach Stomach
Testis Testis
Thyroid Thyroid
Uterus Uterus
Vagina Vagina
 * 
 * @author gerrim
 *
 */
public class TissueKey {

	// Lower case list of parsed names fragments.
	private List<String> frags;
	
	public TissueKey(Sample sample) {
		frags = unmangle(sample.getTissueGroup()+" "+sample.getTissueName());
	}
	public TissueKey(String tissue) {
		frags = unmangle(tissue);
	}

	/**
	 * We unmangle the tissue names to get them to match.
	 * This allows a hash lookup avoiding a nxm loop where
	 * n is order 700mill and m ~ 100.
	 * @param id
	 * @return list of name fragments.
	 */
	private List<String> unmangle(String id) {
		
		List<String> ret = new ArrayList<>();
		if (id==null) return ret;
		
		String[] frags = id.split(" ");
		for (int i = 0; i < frags.length; i++) {
			
			String frag = frags[i].toLowerCase();
			frag = frag.replace("(", "");
			frag = frag.replace(")", "");
			
			if (frag.equals("tissue") && ret.size()>(i-1)) {
				if ("adipose".equals(ret.get(i-1))) {
					continue;
				}
			}
			if (frag.equals("-")) continue;
			
			if (frag.equals("artery")) {
				ret.add("blood");
				ret.add("vessel");
				continue;
			}
			ret.add(frag);
		}
		
		// Group and name repeated 1 word
		if (ret.size()==2 && frags[0].equalsIgnoreCase(frags[1])) {
			return ret.subList(0, 1);
		}
		// Group and name repeated 2 words
		if (ret.size()==4 && ret.subList(0, 2).equals(ret.subList(2, 4))) {
			return ret.subList(0, 2);
		}
		// Group and name repeated 2 words name qualified
		if (ret.size()==5 && ret.subList(0, 2).equals(ret.subList(3, 5))) {
			return ret.subList(2, 5);
		}

		// Whole blood
		if (ret.size()==2 && ret.get(0).equals("whole") && ret.get(1).equals("blood")) {
			ret.add(0, "blood");
		}
		// Skin not 'cells'
		if (ret.size()==3 && ret.get(0).equals("cells")) {
			if (ret.get(1).equals("cultured")) {
				ret.set(0, "skin");
			}
		}
		// Blood not 'cells'
		if (ret.size()==3 && ret.get(0).equals("cells")) {
			if (ret.get(1).equals("ebv-transformed")) {
				ret.set(0, "blood");
			}
		}

		return ret;
	}

	@Override
	public int hashCode() {
		return Objects.hash(frags);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TissueKey))
			return false;
		TissueKey other = (TissueKey) obj;
		return Objects.equals(frags, other.frags);
	}
}
