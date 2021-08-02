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

import java.util.Arrays;
import java.util.Collection;

import org.geneweaver.domain.AbstractEntity;
import org.geneweaver.domain.Ortholog;

// TODO: Auto-generated Javadoc
/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 * 
 
MGI:88282,Cbln2,ENSMUSG00000024647,HGNC:1544,CBLN2,ENSG00000141668
MGI:1098806,Commd2,ENSMUSG00000036513,HGNC:24993,COMMD2,ENSG00000114744
HGNC:18287,VPS37D,ENSMUSG00000043614,MGI:2159402,Vps37d,ENSG00000176428
MGI:1932682,Cxcl16,ENSMUSG00000018920,HGNC:16642,CXCL16,ENSG00000161921
HGNC:20837,PAOX,ENSMUSG00000025464,MGI:1916983,Paox,ENSG00000148832
MGI:88607,Cyp2e1,ENSMUSG00000025479,HGNC:2631,CYP2E1,ENSG00000130649
MGI:1919192,Myof,ENSMUSG00000048612,HGNC:3656,MYOF,ENSG00000138119
MGI:2141942,Ccp110,ENSMUSG00000033904,HGNC:24342,CCP110,ENSG00000103540

 * 
 *
 */
class OrthologBaylorReader<N extends AbstractEntity> extends LineIteratorReader<N>{
	
	/** The Constant VARIANTS. */
	// TODO Are all invariant types a Variant or are some ignored?
	public static final Collection<String> VARIANTS = Arrays.asList("snv", "deletion", "insertion", "indel", "substitution");

	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public OrthologBaylorReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter(","); 
		return this;
	}

	/**
	 * Creates the.
	 *
	 * @param line the line
	 * @return the n
	 * @throws ReaderException the reader exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
        
		String[] rec = line.split(getDelimiter());
		Ortholog bean = new Ortholog(rec[2], rec[5]);
        parseColon(bean, rec[0], rec[3]);
        bean.setSource("BAYLOR");
        return (N)bean;
	}

	private void parseColon(Ortholog orth, String... values) {
		for (String value : values) {
			String[] split = value.split(":");
			if (split[0].toLowerCase().startsWith("mgi")) {
				orth.setMgi(Integer.parseInt(split[1]));
	        } else if (split[0].toLowerCase().startsWith("hgnc")) {
				orth.setHgnc(Integer.parseInt(split[1]));
	        }
		}
	}


}