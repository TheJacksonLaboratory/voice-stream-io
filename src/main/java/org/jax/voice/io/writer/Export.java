package org.jax.voice.io.writer;

import java.nio.file.Path;

@FunctionalInterface
public interface Export {
	
	/**
	 * Interface for customizing exports.
	 * @param path
	 * @param builder
	 * @return a message to be logged regarding the progress of the export.
	 * @throws Exception
	 */
	String export(ExportBuilder builder, Path path) throws Exception;
}
