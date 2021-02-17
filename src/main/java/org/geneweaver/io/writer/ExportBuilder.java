package org.geneweaver.io.writer;

import static org.geneweaver.io.DirectSave.save;

import java.io.BufferedWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.io.Timer;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;

/**
 * Builder for bulk exports.
 * 
 * @author gerrim
 *
 */
public class ExportBuilder implements AutoCloseable {

	/**
	 * The directory in which to export
	 */
	private Path dir;
	
	/**
	 * Iterable of paths from which we will export.
	 */
	private Iterable<Path> inputs;
	
	/**
	 * The chunk size if there is none on the command line.
	 */
	private int defaultChunkSize = 4096;
	
	/**
	 * Consumer for running the export. By default the export does a 
	 * simple save using the default connector.
	 */
	private Export exporter = (builder, path) -> defaultExport(path);
	
	/**
	 * The value of the -c command line
	 */
	private String chunkProperty;
	
	/**
	 * 
	 */
	private String species;
	
	/**
	 * Map of writers cached while we write all the files.
	 */
	private Map<Class<? extends Entity>, BufferedWriter> writers = new HashMap<>();
	
	public ExportBuilder() {
		
	}
	
	public void export() throws Exception {
		// Process one or more paths into the bulk file.
		for (Path input : inputs) {
			exporter.export(this, input);
		}		
	}
	
	/**
	 * Default save stream the reader, gets its connector and writes the lot to file.
	 * 
	 * @param input
	 */
	protected String defaultExport(Path input) {
		
	    StreamReader<Entity> reader;
		try {
			reader = createReader(input);
		} catch (ReaderException e) {
			System.out.println("Cannot write bulk file(s) for '"+input.getFileName());
			e.printStackTrace();
			return e.getMessage();
		}

	    Function<Entity, Stream<Entity>> connector = reader.getDefaultConnector();
		
				
		Timer timer = createTimer();
		
		// Directly saving the streams with no chunks is fast.
		try {
			long saved = reader.stream()
							  .flatMap(g->connector.apply(g))
							  .map(g->save(g, writers, dir, timer))
							  .count();
	
			return "Wrote bulk file(s) for '"+input.getFileName()+"' in "+timer.getFormattedTime()+" parsed "+saved+" objects.";
			
		} catch (ReaderException ne) {
			ne.printStackTrace();
			return "Cannot write bulk file(s) for '"+input.getFileName();
		}

	}

	/**
	 * Create a 
	 * @param input
	 * @return
	 * @throws ReaderException
	 */
	public <T extends Entity> StreamReader<T> createReader(Path input) throws ReaderException {
		
	    StreamReader<T> reader = ReaderFactory.getReader(new ReaderRequest(species, input.toFile()));
	    
	    // With direct streams, chunk size does little.
		reader.setChunkSize(chunkSize());
		
	    return reader;
	}

	/**
	 * @return the dir
	 */
	public Path getDir() {
		return dir;
	}

	/**
	 * @param dir the dir to set
	 */
	public ExportBuilder setDir(Path dir) {
		this.dir = dir;
		return this;
	}

	/**
	 * @return the inputs
	 */
	public Iterable<Path> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public ExportBuilder setInputs(Iterable<Path> inputs) {
		this.inputs = inputs;
		return this;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public ExportBuilder setInput(Path input) {
		this.inputs = Arrays.asList(input);
		return this;
	}

	/**
	 * @return the defaultChunkSize
	 */
	public int getDefaultChunkSize() {
		return defaultChunkSize;
	}

	/**
	 * @param defaultChunkSize the defaultChunkSize to set
	 */
	public ExportBuilder setDefaultChunkSize(int defaultChunkSize) {
		this.defaultChunkSize = defaultChunkSize;
		return this;
	}

	@Override
	public void close() throws Exception {
		for (BufferedWriter writer : writers.values()) writer.close();
	}


	/**
	 * @return the writers
	 */
	public Map<Class<? extends Entity>, BufferedWriter> getWriters() {
		return writers;
	}

	/**
	 * @return the exporter
	 */
	public Export getExporter() {
		return exporter;
	}

	/**
	 * @param exporter the exporter to set
	 */
	public ExportBuilder setExporter(Export exporter) {
		this.exporter = exporter;
		return this;
	}

	/**
	 * Create a simple timer.
	 * @return
	 */
	public Timer createTimer() {
		Timer timer = new Timer(); // Just used as timer.
		int chunkSize  = chunkSize();
		timer.setTimedChunkSize(chunkSize);
		return timer;
	}

	/**
	 * The chunk size.
	 * @return
	 */
	public int chunkSize() {
	    String c = chunkProperty!=null ? chunkProperty : String.valueOf(defaultChunkSize);
		return Integer.parseInt(c);
	}

	/**
	 * @return the chunkProperty
	 */
	public String getChunkProperty() {
		return chunkProperty;
	}

	/**
	 * @param chunkProperty the chunkProperty to set
	 */
	public ExportBuilder setChunkProperty(String chunkProperty) {
		this.chunkProperty = chunkProperty;
		return this;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @param species the species to set
	 */
	public ExportBuilder setSpecies(String species) {
		this.species = species;
		return this;
	}

}
