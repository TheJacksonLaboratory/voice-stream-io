/*-
 * 
 * Copyright 2024  The Jackson Laboratory Inc.
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
package org.geneweaver.domain;

import java.util.Objects;

import javax.annotation.processing.Generated;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

// TODO: Auto-generated Javadoc
/**
 
 The data files on the regulation FTP site follow a naming convention, which is described 
in greater detail here:

https://github.com/FAANG/faang-metadata/blob/master/docs/faang_analysis_metadata.md#file-naming

The file names include the following information separated with a dot ('.'):

  - species,
  - assembly version,
  - cell type (if applicable),
  - feature type (if applicable),
  - analysis name,
  - results type,
  - data freeze date and
  - file format.

E.g.: homo_sapiens.GRCh38.K562.Regulatory_Build.regulatory_activity.20161111.gff.gz

(...Because encoding metadata into file files is what we do in biology :( )

The data available on our FTP site include:

Peaks
-----

The set of peaks for transcription factors and histone modifications 
that are part of our regulatory resources. 

Regulatory Feature Activity
---------------------------

The current set of regulatory features along with their predicted activity in 
every cell type. We provide one gff file per cell type in the 
'RegulatoryFeatureActivity' subdirectory.

Motif Features
--------------

For each transcription factor (TF), for which a ChIP-seq data set is part of the Ensembl Regulation 
resources and a position weight matrix (PWM) is available, we annotate the position of putative TF 
binding sites within the ChIP-seq peaks. The results are provided in the 'MotifFeatures' subdirectory.
 */
@Generated("POJO")
@NodeEntity(label="RegulatoryFeature")
public class RegulatoryFeature extends GeneticEntity {

	// chr, source, type, start, end, ?,?,?, params
	// Filename: species, assembly version, cell type (if applicable), feature type (if applicable), analysis name,results type,data freeze date
	
	/** The transcript id. */
	@Index(unique=true)
	private String featureId;
	
	private String activity;
	
	private int boundEnd;
	private int boundStart;
	private String description;
	private String epigenome;
	private String featureType;
	private String assemblyVersion;
	private String cellType;
	private String analysisName;
	private String resultsType;
	private String date;
	
	public RegulatoryFeature() {
		
	}

	public RegulatoryFeature(String id, int start, int end) {
		this();
		this.featureId = id;
		this.setStart(start);
		this.setEnd(end);
	}

	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	@Override
	public String getHeader() {
		StringBuilder buf = new StringBuilder();
		buf.append("featureId:ID(Feature-Id)");
		buf.append(getDelimiter());
		buf.append("activity");
		buf.append(getDelimiter());
		buf.append("epigenome");
		buf.append(getDelimiter());
		buf.append("featureType");
		buf.append(getDelimiter());
		buf.append("assemblyVersion");
		buf.append(getDelimiter());
		buf.append("cellType");
		buf.append(getDelimiter());
		buf.append("description");
		buf.append(getDelimiter());
		buf.append(super.getHeader());
		return buf.toString();
	}
	
	/**
	 * To csv.
	 *
	 * @return the string
	 */
	@Override
	public String toCsv() {
		StringBuilder buf = new StringBuilder();
		buf.append(getFeatureId());
		buf.append(getDelimiter());
		buf.append(getActivity());
		buf.append(getDelimiter());
		buf.append(getEpigenome());
		buf.append(getDelimiter());
		buf.append(getFeatureType());
		buf.append(getDelimiter());
		buf.append(getAssemblyVersion());
		buf.append(getDelimiter());
		buf.append(getCellType());
		buf.append(getDelimiter());
		buf.append(getDescription());
		buf.append(getDelimiter());
		buf.append(super.toCsv());
		return buf.toString();
	}

	/**
	 * @return the featureId
	 */
	public String getFeatureId() {
		return featureId;
	}
	/**
	 * @param featureId the featureId to set
	 */
	public void setFeatureId(String featureId) {
		this.featureId = featureId;
	}
	/**
	 * @return the activity
	 */
	public String getActivity() {
		return activity;
	}
	/**
	 * @param activity the activity to set
	 */
	public void setActivity(String activity) {
		this.activity = activity;
	}
	/**
	 * @return the boundEnd
	 */
	public int getBoundEnd() {
		return boundEnd;
	}
	/**
	 * @param boundEnd the boundEnd to set
	 */
	public void setBoundEnd(int boundEnd) {
		this.boundEnd = boundEnd;
	}
	/**
	 * @return the boundStart
	 */
	public int getBoundStart() {
		return boundStart;
	}
	/**
	 * @param boundStart the boundStart to set
	 */
	public void setBoundStart(int boundStart) {
		this.boundStart = boundStart;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the epigenome
	 */
	public String getEpigenome() {
		return epigenome;
	}
	/**
	 * @param epigenome the epigenome to set
	 */
	public void setEpigenome(String epigenome) {
		this.epigenome = epigenome;
	}
	/**
	 * @return the featureType
	 */
	public String getFeatureType() {
		return featureType;
	}
	/**
	 * @param featureType the featureType to set
	 */
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	/**
	 * @return the assemblyVersion
	 */
	public String getAssemblyVersion() {
		return assemblyVersion;
	}
	/**
	 * @param assemblyVersion the assemblyVersion to set
	 */
	public void setAssemblyVersion(String assemblyVersion) {
		this.assemblyVersion = assemblyVersion;
	}
	/**
	 * @return the analysisName
	 */
	public String getAnalysisName() {
		return analysisName;
	}
	/**
	 * @param analysisName the analysisName to set
	 */
	public void setAnalysisName(String analysisName) {
		this.analysisName = analysisName;
	}
	/**
	 * @return the resultsType
	 */
	public String getResultsType() {
		return resultsType;
	}
	/**
	 * @param resultsType the resultsType to set
	 */
	public void setResultsType(String resultsType) {
		this.resultsType = resultsType;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(activity, analysisName, assemblyVersion, boundEnd, boundStart, cellType,
				date, description, epigenome, featureId, featureType, resultsType);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RegulatoryFeature))
			return false;
		RegulatoryFeature other = (RegulatoryFeature) obj;
		return Objects.equals(activity, other.activity) && Objects.equals(analysisName, other.analysisName)
				&& Objects.equals(assemblyVersion, other.assemblyVersion) && boundEnd == other.boundEnd
				&& boundStart == other.boundStart && Objects.equals(cellType, other.cellType)
				&& Objects.equals(date, other.date) && Objects.equals(description, other.description)
				&& Objects.equals(epigenome, other.epigenome) && Objects.equals(featureId, other.featureId)
				&& Objects.equals(featureType, other.featureType) && Objects.equals(resultsType, other.resultsType);
	}
	@Override
	public String id() {
		return getFeatureId();
	}

	/**
	 * @return the cellType
	 */
	public String getCellType() {
		return cellType;
	}

	/**
	 * @param cellType the cellType to set
	 */
	public void setCellType(String cellType) {
		this.cellType = cellType;
	}
	
}
	
