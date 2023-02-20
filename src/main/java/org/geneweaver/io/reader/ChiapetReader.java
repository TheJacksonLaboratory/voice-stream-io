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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.geneweaver.domain.Anchor;
import org.geneweaver.domain.AnchoredEntity;
import org.geneweaver.domain.ChromatinInteraction;
import org.geneweaver.domain.ExperimentMetadata;

/**
 	
 	Process the spreadsheet containing short/long range chromatin interactions in
    human MCF7 and K562 cells from study PMID:22265404.
    Threshold based on significance (FDR < 0.05), remove irrelevant fields, and return
    a dataframe containing the results.

    inputs
        fp:    input filepath
        sheet: optional value indicating what sheet in the excel file to parse
               These are the sheets, name and indices, in the excel file:
                    Summary                         0
                    MCF7 pilot peaks                1
                    All pilot peaks                 2
                    K562 saturated peaks            3
                    MCF7 saturated peaks            4
                    MCF7 pilot interactions         5
                    All pilot interactions          6
                    K562 saturated interactions #1  7
                    K562 saturated interactions #2  8
                    MCF7 saturated interactions     9

                We want sheets 5, 7, 8, 9.

 * @author gerrim
 *
 * @param <N>
 */
class ChiapetReader<N extends AnchoredEntity> extends AbstractXlsReader<N, ExperimentMetadata> {
	

	@SuppressWarnings("unchecked")
	@Override
	protected N create(Row row) {
		
		Class<N> concrete = getConcreteClass();
		try {
			if (concrete==null) concrete  = (Class<N>)ChromatinInteraction.class;
		} catch (RuntimeException ne) {
			throw new IllegalArgumentException("Please set the concrete class on "+getClass().getSimpleName(), ne);
		}
		
		Cell cell = row.getCell(0);
		if (cell==null) return null;
		String chr = cell.getStringCellValue();
		if (chr==null) return null;
		if (!chr.matches("chr(\\d+|X|Y|M)")) return null; // Null is filtered from the stream.
		
		if (concrete == ChromatinInteraction.class) {
			ChromatinInteraction c = new ChromatinInteraction();
			c.setChr(chr);
			ExperimentMetadata meta = getMeta();
			if (meta!=null) meta.setChr(chr);
			c.setMeta(meta);
			c.setLeft(createAnchor(row, 0,1,2));
			c.setRight(createAnchor(row, 3,4,5));
			c.setPetCount((int)Math.round(row.getCell(6).getNumericCellValue()));
			c.setP(row.getCell(7).getNumericCellValue());
			c.setFdr(row.getCell(8).getNumericCellValue());
			
			String overlap = row.getCell(9)!=null ? row.getCell(9).getStringCellValue() : null;
			c.setOverlapDNAPET("Yes".equalsIgnoreCase(overlap));
			
			return (N)c;
		} else if (concrete == Anchor.class) {
			return (N)createAnchor(row, 0,1,2,3);
		}
		
		throw new IllegalArgumentException("Class "+getConcreteClass()+" cannot be pared using "+getClass().getSimpleName());
	}

	private Anchor createAnchor(Row row, int i, int j, int k, int l) {
		return new Anchor(row.getCell(i).getStringCellValue(),
				  (int)Math.round(row.getCell(j).getNumericCellValue()),
				  (int)Math.round(row.getCell(k).getNumericCellValue()),
				  (int)Math.round(row.getCell(l).getNumericCellValue()));
	}

	private Anchor createAnchor(Row row, int i, int j, int k) {
		return new Anchor(row.getCell(i).getStringCellValue(),
						  (int)Math.round(row.getCell(j).getNumericCellValue()),
						  (int)Math.round(row.getCell(k).getNumericCellValue()));
	}

}
