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

import java.util.Map;

import org.geneweaver.domain.Entity;
import org.geneweaver.domain.Sample;  

/**
 * 

Example of GTEx sample file.
@see https://storage.googleapis.com/gtex_analysis_v8/annotations/GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt

SAMPID	SMATSSCR	SMCENTER	SMPTHNTS	SMRIN	SMTS	SMTSD	SMUBRID	SMTSISCH	SMTSPAXSMNABTCH	SMNABTCHT	SMNABTCHD	SMGEBTCH	SMGEBTCHD	SMGEBTCHT	SMAFRZE	SMGTC	SME2MPRSMCHMPRS	SMNTRART	SMNUMGPS	SMMAPRT	SMEXNCRT	SM550NRM	SMGNSDTC	SMUNMPRT	SM350NRM	SMRDLGTH	SMMNCPB	SME1MMRT	SMSFLGTH	SMESTLBS	SMMPPD	SMNTERRT	SMRRNANSMRDTTL	SMVQCFL	SMMNCV	SMTRSCPT	SMMPPDPR	SMCGLGTH	SMGAPPCT	SMUNPDRD	SMNTRNRSMMPUNRT	SMEXPEFF	SMMPPDUN	SME2MMRT	SME2ANTI	SMALTALG	SME2SNSE	SMMFLGTSME1ANTI	SMSPLTRD	SMBSMMRT	SME1SNSE	SME1PCTS	SMRRNART	SME1MPRT	SMNUM5CSMDPMPRT	SME2PCTS
GTEX-1117F-0003-SM-58Q7G		B1			Blood	Whole Blood	0013756	1188		BP-38516	DNA isolation_Whole Blood_QIAGEN Puregene (Manual)	05/02/2013	LCSET-4574	01/15/2014	Standard Exome Sequencing v3 (ICE)	WES									
GTEX-1117F-0003-SM-5DWSB		B1			Blood	Whole Blood	0013756	1188		BP-38516	DNA isolation_Whole Blood_QIAGEN Puregene (Manual)	05/02/2013	GTEx_OM25_Dec_01	01/28/2014	Illumina OMNI SNP Array	OMNI										
GTEX-1117F-0003-SM-6WBT7		B1			Blood	Whole Blood	0013756	1188		BP-38516	DNA isolation_Whole Blood_QIAGEN Puregene (Manual)	05/02/2013	LCSET-6056	09/20/2014	PCR+ 30x Coverage WGS v2 (HiSeqX)	WGS									

 * @author gerrim
 *
 * @param <N>
 */
class GTExSampleReader<N extends Entity> extends LineIteratorReader<N> {

	/**
	 * Create the reader by setting its data
	 * 
	 * @param request
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public GTExSampleReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\\t");
		return this;
	}
	
	private Map<String,Integer> indices;

	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
		
		String[] segs = line.split(getDelimiter());
		if (line.startsWith("SAMPID")) {
			indices = parseIndices(segs);
			return null; // It's a header.
		}
		
		Sample ret = new Sample();
		ret.setTissueGroup(segs[indices.get("smts")]);
		ret.setTissueName(segs[indices.get("smtsd")]);
		ret.trim();
		
		return (N)ret;
	}
}
