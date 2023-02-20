package org.geneweaver.io.writer;

import static org.geneweaver.io.DirectSave.save;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
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
	 * If the connectors are different from the default connector.
	 * A connector must be a org.geneweaver.io.connector.Connector
	 * or a Function which returns a stream of entities when passed
	 * an entity.
	 */
	private Collection<Function<?,?>> connectors;
	
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
	 * Set to always add the default connector as the first connector when 
	 * making the connector list. If addConnector(..) has not been used,
	 * this setting does nothing as the default connector will be used anyway,
	 * however if 
	 */
	private boolean alwaysUseDefaultConnector = false;
	
	/**
	 * If there are multiple files, when calling export a
	 * parallel exporter will run each file with a separate thread.
	 */
	private boolean parallelFiles = false;
	
	/**
	 * Stream for printing messages of each export run.
	 */
	@JsonIgnore
	private PrintStream out = System.out;
	
	/**
	 * Map of writers cached while we write all the files.
	 */
	@JsonIgnore
	private Map<Class<? extends Entity>, Map<String,BufferedWriter>> writers = Collections.synchronizedMap(new HashMap<>());

	private Collection<Throwable> errors = new LinkedList<>();
	
	public ExportBuilder() {
		
	}
	
	public void export() throws Exception {
		try {
			if (isParallelFiles()) {
				parallelExport();
			} else {
				singleThreadExport();
			}
		} catch (Exception ne) {
			errors.add(ne);
			throw ne;
		}
	}
	
	private void singleThreadExport() throws Exception {
		// Process one or more paths into the bulk file.
		for (Path input : inputs) {
			String message = exporter.export(this, input);
			out.println(message);
		}		
	}

	private void parallelExport() throws InterruptedException {
		
		ThreadGroup pool = new ThreadGroup("Exporters");
		
		// Have to use list as inputs is just an iterator and
		// does not know its size.
		List<CountDownLatch> latches = new LinkedList<>();
		
		// Mostly the number of files is around 23 for all the chromosome
		// files. 23 long running threads should be reasonably efficient without
		// using executor service. If swapping small tasks would use.
		for (Path input : inputs) {
			CountDownLatch latch = new CountDownLatch(1);
			latches.add(latch);
			
			Thread thread = new Thread(pool, ()->exportQuietly(input, latch));
			thread.setName("Export "+input.getFileName());
			thread.start();
		}
		
		for (CountDownLatch latch : latches) {
			latch.await();
		}
	}

	private void exportQuietly(Path input, CountDownLatch count) {
		try {
			exporter.export(this, input);
		} catch (Exception e) {
			errors.add(e);
		} finally {
			count.countDown();
		}
	}
	
	public String status() {
		if (errors.isEmpty()) return "Complete";
		
		String message = "";
		for (Throwable err : errors) {
			err.printStackTrace(out);
			message = message+err.getMessage()+"\n";
		}
		return message;
	}

	/**
	 * Default save stream the reader, gets its connector and writes the lot to file.
	 * 
	 * @param input
	 * @throws ReaderException 
	 */
	protected String defaultExport(Path input, boolean append) throws Exception {
		
	    StreamReader<Entity> reader = createReader(input);
	    
	    Collection<Function<Entity, Stream<Entity>>> conns = null;
	    if (this.connectors==null || this.connectors.isEmpty()) {
	    	Function<Entity, Stream<Entity>> def = reader.getDefaultConnector();
	    	conns = Arrays.asList(def);
	    } else {
	    	conns = new LinkedList<>();
	    	if (isAlwaysUseDefaultConnector()) {
	    		conns.add(reader.getDefaultConnector());
	    	}
	    	
	    	for (Function<?, ?> function : this.connectors) {
	    		@SuppressWarnings("unchecked")
	    		// If you add a function which cannot be cast
				Function<Entity, Stream<Entity>> cast = (Function<Entity, Stream<Entity>>)function;
		    	conns.add(cast);
			}
	    }
		
		Timer timer = createTimer();
		
		Stream<Entity> stream = reader.stream();
		for (Function<Entity, Stream<Entity>> c : conns) {
			stream = stream.flatMap(g->c.apply(g));
		}
		
		long saved = stream.map(g->save(g, writers, dir, timer, append))
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
	 * @param inputs the inputs to set
	 */
	public ExportBuilder addConnector(Function<?,?> conn) {
		if (this.connectors==null) this.connectors = new LinkedList<>();
		this.connectors.add(conn);
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
		for (Map<String,BufferedWriter> brs : writers.values()) {
			for (BufferedWriter writer : brs.values()) {
				writer.close();
			}
		}
	}


	/**
	 * @return the writers
	 */
	@JsonIgnore
	public Map<Class<? extends Entity>, Map<String,BufferedWriter>> getWriters() {
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

	/**
	 * @return the alwaysUseDefaultConnector
	 */
	public boolean isAlwaysUseDefaultConnector() {
		return alwaysUseDefaultConnector;
	}

	/**
	 * @param alwaysUseDefaultConnector the alwaysUseDefaultConnector to set
	 */
	public ExportBuilder setAlwaysUseDefaultConnector(boolean alwaysUseDefaultConnector) {
		this.alwaysUseDefaultConnector = alwaysUseDefaultConnector;
		return this;
	}

	/**
	 * @return the parallel
	 */
	public boolean isParallelFiles() {
		return parallelFiles;
	}

	/**
	 * @param parallel the parallel to set
	 */
	public ExportBuilder setParallelFiles(boolean parallel) {
		this.parallelFiles = parallel;
		return this;
	}

}
