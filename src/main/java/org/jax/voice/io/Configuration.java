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
package org.jax.voice.io;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

// TODO: Auto-generated Javadoc
/**
 * The Class Configuration.
 */
public class Configuration {

	/**
	 * Set to true to zip each partition.
	 */
	private volatile ZipType zipType = ZipType.valueOf(System.getenv().getOrDefault("PARTITION_ZIP", "GZ"));
	
	/**
	 * The number of lines in each partition.
	 */
	private volatile int partitionLines = Integer.parseInt(System.getenv().getOrDefault("PARTITION_LINES", "10000"));
	
	/**
	 * Length of timeout when shutting down notification pool.
	 */
	private volatile long timeout=Integer.parseInt(System.getenv().getOrDefault("PARTITION_TIMEOUT", "30"));
	
	/**
	 * Number of background runs which may be running the visitors (which upload files).
	 */
	private volatile int permits = Integer.parseInt(System.getenv().getOrDefault("SIMULTANEOUS_UPLOAD_PERMITS", "10"));
	
	/**
	 * Timeout unit.
	 */
	private volatile TimeUnit unit=TimeUnit.SECONDS;


	/**
	 * Gets the default.
	 *
	 * @return the default
	 */
	public static Configuration getDefault() {
		return new Configuration();
	}
	
	/**
	 * Gets the timeout.
	 *
	 * @return the timeout
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout to set
	 * @return the configuration
	 */
	public Configuration setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public TimeUnit getUnit() {
		return unit;
	}

	/**
	 * Sets the unit.
	 *
	 * @param unit the unit to set
	 * @return the configuration
	 */
	public Configuration setUnit(TimeUnit unit) {
		this.unit = unit;
		return this;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(partitionLines, timeout, unit, zipType);
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Configuration))
			return false;
		Configuration other = (Configuration) obj;
		return partitionLines == other.partitionLines && timeout == other.timeout && unit == other.unit
				&& zipType == other.zipType;
	}

	/**
	 * Gets the partition lines.
	 *
	 * @return the partition lines
	 */
	public int getPartitionLines() {
		return partitionLines;
	}

	/**
	 * Sets the partition lines.
	 *
	 * @param partitionLines the partition lines
	 * @return the configuration
	 */
	public Configuration setPartitionLines(int partitionLines) {
		this.partitionLines = partitionLines;
		return this;
	}

	/**
	 * The Enum FileType.
	 */
	public enum FileType {
		
		/** The gene. */
		/*
		 * Genes, Transcripts and the others.
		 * Smallish file.
		 */
		GENE,
		
		/**
		 * Variant files.
		 */
		VARIANT,
		
		/**
		 * If the file type is not known.
		 * File type will be analysed by file extension if it 
		 * is not known.
		 */
		UNKNOWN
	}
	
	/**
	 * The Enum ZipType.
	 */
	public enum ZipType {
		
		/** The none. */
		NONE("text/plain"),
		
		/** The zip. */
		ZIP("application/zip"), 
		
		/** The gz. */
		GZ("application/gzip");
		
		/** The ctype. */
		private String ctype;

		/**
		 * Instantiates a new zip type.
		 *
		 * @param ctype the ctype
		 */
		ZipType(String ctype) {
			this.ctype = ctype;
		}

		/**
		 * Gets the content type.
		 *
		 * @return the content type
		 */
		public String getContentType() {
			return ctype;
		}
	}

	/**
	 * Gets the zip type.
	 *
	 * @return the zipType
	 */
	public ZipType getZipType() {
		return zipType;
	}

	/**
	 * Sets the zip type.
	 *
	 * @param zipType the zipType to set
	 * @return the configuration
	 */
	public Configuration setZipType(ZipType zipType) {
		this.zipType = zipType;
		return this;
	}

	/**
	 * Gets the permits.
	 *
	 * @return the permits
	 */
	public int getPermits() {
		return permits;
	}

	/**
	 * Sets the permits.
	 *
	 * @param permits the permits to set
	 */
	public void setPermits(int permits) {
		this.permits = permits;
	}

}
