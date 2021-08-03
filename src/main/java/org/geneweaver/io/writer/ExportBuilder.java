package org.geneweaver.io.writer;

import static org.geneweaver.io.DirectSave.save;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.io.Timer;
import org.geneweaver.io.reader.ReaderException;
import org.geneweaver.io.reader.ReaderFactory;
import org.geneweaver.io.reader.ReaderRequest;
import org.geneweaver.io.reader.StreamReader;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	@JsonIgnore
	private Export exporter = (builder, path) -> defaultExport(path, true);
	
	/**
	 * The value of the -c command line
	 */
	private String chunkProperty;
	
	/**
	 * 
	 */
	private String species;
	
	/**
	 * Stream for printing messages of each export run.
	 */
	@JsonIgnore
	private PrintStream out = System.out;
	
	/**
	 * Map of writers cached while we write all the files.
	 */
	@JsonIgnore
	private Map<Class<? extends Entity>, BufferedWriter> writers = new HashMap<>();
	
	public ExportBuilder() {
		
	}
	
	public void export() throws Exception {
		// Process one or more paths into the bulk file.
		for (Path input : inputs) {
			String message = exporter.export(this, input);
			out.println(message);
		}		
	}
	
	/**
	 * Default save stream the reader, gets its connector and writes the lot to file.
	 * 
	 * @param input
	 * @throws ReaderException 
	 */
	protected String defaultExport(Path input, boolean append) throws Exception {
		
	    StreamReader<Entity> reader = createReader(input);
	    Function<Entity, Stream<Entity>> connector = reader.getDefaultConnector();
		
		Timer timer = createTimer();
		
		long saved = reader.stream()
							.flatMap(g->connector.apply(g))
							.map(g->save(g, writers, dir, timer, append))
							.count();

		return "Wrote bulk file(s) for '"+input.getFileName()+"' in "+timer.getFormattedTime()+" parsed "+saved+" objects.";
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
	@JsonIgnore
	public Map<Class<? extends Entity>, BufferedWriter> getWriters() {
		return writers;
	}

	/**
	 * @return the exporter
	 */
	@JsonIgnore
	public Export getExporter() {
		return exporter;
	}

	/**
	 * @param exporter the exporter to set
	 */
	@JsonIgnore
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

	@Override
	public int hashCode() {
		return Objects.hash(chunkProperty, defaultChunkSize, dir, inputs, species);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExportBuilder))
			return false;
		ExportBuilder other = (ExportBuilder) obj;
		return Objects.equals(chunkProperty, other.chunkProperty) && defaultChunkSize == other.defaultChunkSize
				&& Objects.equals(dir, other.dir) && Objects.equals(inputs, other.inputs)
				&& Objects.equals(species, other.species);
	}

	/**
	 * @return the out
	 */
	@JsonIgnore
	public PrintStream getOut() {
		return out;
	}

	/**
	 * @param out the out to set
	 */
	@JsonIgnore
	public void setOut(PrintStream out) {
		this.out = out;
	}

}
