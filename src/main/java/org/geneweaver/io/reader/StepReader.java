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

import org.apache.commons.beanutils.BeanMap;
import org.geneweaver.domain.Step;

/**

# filename_num = 1
# method = 4 C
# species = mouse
# original_assembly = mm6
# cell_type = fetal liver
# Enhancer Source = NA
# Antibody = β-globin
# GEO Number = GSE5891
# DataSource = PubMed
# Reference = -2
# Resolution = 31.2kb
chr	start1	end1	chr	start2	end2	contact	FDR	p-value
chr7	103827929	103853207	chr7	73478840	73658205	NA	0.05	NA
chr7	103827929	103853207	chr7	78752268	78850141	NA	0.05	NA
chr7	103827929	103853207	chr7	80106278	80301277	NA	0.05	NA
chr7	103827929	103853207	chr7	81696768	81747483	NA	0.05	NA
chr7	103827929	103853207	chr7	83827289	83945446	NA	0.05	NA

 * @author gerrim
 *
 */
class StepReader extends LineIteratorReader<Step>{


	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public StepReader init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\t+"); // Must be a tab only
		setChunkSize(1000);
		return this;
	}

	/**
	 * Creates the.
	 *
	 * @param line the line
	 * @return the n
	 * @throws ReaderException the reader exception
	 */
	@Override
	protected Step create(String line) throws ReaderException {
        
		if (line.trim().equals("chr	start1	end1	chr	start2	end2	contact	FDR	p-value")) {
			// We ignore the header line
			return null;
		}
		
		String[] rec = line.split(getDelimiter());

		Step step = new Step();
		BeanMap d = new BeanMap(step);
		d.put("chr1", 	rec[0]);
		d.put("start1", rec[1]);
		d.put("end1", 	rec[2]);
		d.put("chr2",	rec[3]);
		d.put("start2", rec[4]);
		d.put("end2", 	rec[5]);
		
        Map<String,Object> attributes = parseHeaders();
        // TODO Which properties are needed?
        d.put("method", attributes.get("method"));
        d.put("cellType", attributes.get("cell_type"));

		return step;
	}
}