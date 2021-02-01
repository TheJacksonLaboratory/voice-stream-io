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
package org.jax.gweaver.io;

// TODO: Auto-generated Javadoc
/**
 * The Class PartitionException.
 */
public class PartitionException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5134504280278559966L;

	/**
	 * Instantiates a new partition exception.
	 */
	public PartitionException() {
		super();
	}

	/**
	 * Instantiates a new partition exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public PartitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new partition exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public PartitionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new partition exception.
	 *
	 * @param message the message
	 */
	public PartitionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new partition exception.
	 *
	 * @param cause the cause
	 */
	public PartitionException(Throwable cause) {
		super(cause);
	}

}
