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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.geneweaver.domain.GeneticEntity;
import org.geneweaver.domain.RegulatoryFeature;
import org.junit.Test;

/**
 * A test of gene reading as a stream.
 * 
 * @author gerrim
 *
 */
public class RegulatoryFeatureTest extends AbstractDataFileTest {
	
	
	@Test
	public void simpleGeneRead1() throws Exception {
		RegulatoryFeatureReader<RegulatoryFeature> reader = new RegulatoryFeatureReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/1000/hs.reg.feat.gff")));
		List<RegulatoryFeature> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
	}
	
	@Test
	public void simpleGeneRead2() throws Exception {
		RegulatoryFeatureReader<RegulatoryFeature> reader = new RegulatoryFeatureReader<>();
		reader.init(new ReaderRequest("Homo sapiens", getFile("data/gff_peaks/homo_sapiens/A459/homo_sapiens.GRCh38.A549.Regulatory_Build.regulatory_activity.20220526.gff.gz")));
		
		
		List<RegulatoryFeature> found = reader.stream()
				.limit(1000) // There are a lot of them and it would take a while otherwise.
				.collect(Collectors.toList());
		
		assertEquals(1000, found.size());
	}

	@Test
	public void simpleGeneReadByReader1() throws Exception {
		RegulatoryFeatureReader<RegulatoryFeature> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/1000/hs.reg.feat.gff")));
		List<RegulatoryFeature> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
	}

	@Test
	public void simpleGeneReadByReader2() throws Exception {
		RegulatoryFeatureReader<RegulatoryFeature> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", getFile("data/gff_peaks/homo_sapiens/sigmoid_colon/homo_sapiens.GRCh38.sigmoid_colon.Regulatory_Build.regulatory_activity.20220526.gff")));
		List<RegulatoryFeature> found = reader.stream()
				.limit(1000)// There are a lot of them and it would take a while otherwise.
				.collect(Collectors.toList());
		
		assertEquals(1000, found.size());
	}

}
