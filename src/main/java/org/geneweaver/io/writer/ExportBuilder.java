package org.geneweaver.io.writer;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;

import org.geneweaver.domain.Entity;
import org.geneweaver.io.DirectSave;
import org.geneweaver.io.IPrintStream;
import org.geneweaver.io.Timer;
import org.geneweaver.io.connector.Connector;
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
	private Export exporter = (builder, path) -> defaultExport(path, false);
	
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
	 * When processing connections we can use parallel with this option.
	 */
	private boolean parallelConnections = false;
	
	/**
	 * Stream for printing messages of each export run.
	 */
	@JsonIgnore
	private IPrintStream out = IPrintStream.of(System.out);
	
	private boolean verbose = false;
	
	/**
	 * Map of writers cached while we write all the files.
	 */
	@JsonIgnore
	private Map<Class<? extends Entity>, Map<String,BufferedWriter>> writers = Collections.synchronizedMap(new HashMap<>());
	
	@JsonIgnore
	private Map<Class<? extends Entity>, Map<String,Path>> paths = Collections.synchronizedMap(new HashMap<>());

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

	private void parallelExport() throws InterruptedException, ExecutionException {
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<String>> futures = new ArrayList<>();
		
		// Mostly the number of files is around 23 for all the chromosome
		// files. 23 long running threads should be reasonably efficient without
		// using executor service. If swapping small tasks would use.
		for (Path input : inputs) {
			
			Future<String> future = executor.submit(()->exportQuietly(input));
			futures.add(future);
		}
		
		for (Future<String> future : futures) {
			future.get();
		}
	}

	private String exportQuietly(Path input) {
		try {
			return exporter.export(this, input);
		} catch (Exception e) {
			errors.add(e);
			return null;
		} 
	}
	
	public String status() {
		if (errors.isEmpty()) return "Complete";
		
		String message = "";
		for (Throwable err : errors) {
			err.printStackTrace(out.getPrintStream());
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
		
		
		if (isVerbose() && getOut()!=null) {
			getOut().println("Input file: "+input);
		}
		
	    StreamReader<Entity> reader = createReader(input);
	    Collection<Function<Entity, Stream<Entity>>> conns = getConnnectors(reader);
	    
		if (isVerbose() && getOut()!=null) {
			getOut().println("Input file: "+input);
			getOut().println("There are "+conns.size()+" connectors");
			for (Function<Entity, Stream<Entity>> c : conns) {
				getOut().println("Connector type: "+c.getClass().getName());
				boolean isConnector = (c instanceof Connector<Entity, Entity>);
				getOut().println("Connector instance of 'Connector' class: "+isConnector);
			}
		}

		try (DirectSave saver = new DirectSave(getOut(), isVerbose())) {
			
			Timer timer = createTimer();
			
			long total = 0L;
			if (isParallelConnections()) {
				// This is likely to fail tests and is not used in production.
				total = parallelConnectors(reader, saver, conns, timer, append);
			} else {
				total = standardConnectors(reader, saver, conns, timer, append);
			}
			
			return "Wrote bulk file(s) for '"+input.getFileName()+"' in "+timer.getFormattedTime()+" parsed "+total+" objects.";
		}
	}

	private long standardConnectors(StreamReader<Entity> reader, DirectSave saver,
								Collection<Function<Entity, 
								Stream<Entity>>> conns, 
								Timer timer, boolean append) throws ReaderException {
		
		Stream<Entity> stream = reader.stream().distinct();
		for (Function<Entity, Stream<Entity>> c : conns) {
			boolean isConnector = (c instanceof Connector<Entity, Entity>);
			if (isVerbose() && isConnector) {
				Connector<Entity, Entity> conn = (Connector<Entity, Entity>)c;
				stream = stream.flatMap(g->conn.stream(g, null, getOut()));
			} else {
				stream = stream.flatMap(g->c.apply(g));
			}
		}
		
		return stream.map(g->saver.save(g, paths, writers, dir, timer, append))
				     .count();
	}

	/**
	 * TODO In tests having parallel connections neither speeded things up nor
	 * got the right answer. The stream of objects contains repeats because it is
	 * not a single stream flat mapped over to file (I think).
	 * @param reader
	 * @param saver
	 * @param conns
	 * @param timer
	 * @param append
	 * @return size of saved items
	 * @throws ReaderException
	 */
	private long parallelConnectors(StreamReader<Entity> reader, DirectSave saver, 
								Collection<Function<Entity, 
								Stream<Entity>>> conns, 
								Timer timer, boolean append) throws ReaderException {
		
		// For each of the file processors, we have ten connection processors.
		// In tests it seems that the connections are bounded by lookup, each being
		// slowish at least on a large scale but many in parallel being possible.
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		// When it comes to looking up all the variant connections,
		// all these flat maps are the slow parts. Other connections are
		// quite fast but these require lookups into the peak tables which
		// makes it slow.
		Stream<Entity> stream = reader.stream().distinct();
		List<Future<Long>> saved = new ArrayList<>();
		stream.forEach(g -> {
			Future<Long> future = executor.submit(createSaver(g, conns, saver, timer, append));
			saved.add(future);
		});
		
		long total = 0L;
		for (Future<Long> future : saved) {
			try {
				total+=future.get();
			} catch (Exception e) {
				errors.add(e);
				e.printStackTrace(getOut().getPrintStream());
			}
		}
		
		return total;
	}

	private Callable<Long> createSaver(Entity entity, 
								Collection<Function<Entity, Stream<Entity>>> conns,
								DirectSave saver,
								Timer timer,
								boolean append) {
		return () -> {
			
			Set<Entity> toSave = new LinkedHashSet<>();
			for (Function<Entity, Stream<Entity>> c : conns) {
				
				boolean isConnector = (c instanceof Connector<Entity, Entity>);
				if (isVerbose() && isConnector) {
					Connector<Entity, Entity> conn = (Connector<Entity, Entity>)c;
					toSave.addAll(conn.stream(entity, null, getOut()).toList());
				} else {
					toSave.addAll(c.apply(entity).toList());
				}
			}

			for (Entity s : toSave) {
				saver.save(s, paths, writers, dir, timer, append);
			}
			return (long)toSave.size();
		};
	}

	private Collection<Function<Entity, Stream<Entity>>> getConnnectors(StreamReader<Entity> reader) {
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
	    return conns;
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
	 * @return the writers
	 */
	@JsonIgnore
	public Map<Class<? extends Entity>, Map<String,Path>> getPaths() {
		return paths;
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
	public IPrintStream getOut() {
		return out;
	}

	/**
	 * @param out the out to set
	 */
	@JsonIgnore
	public ExportBuilder setOut(PrintStream out) {
		this.out = IPrintStream.of(out);
		return this;
	}

	@JsonIgnore
	public ExportBuilder setOut(IPrintStream out) {
		this.out = out;
		return this;
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

	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose the verbose to set
	 */
	public ExportBuilder setVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	/**
	 * @return the parallelConnections
	 */
	public boolean isParallelConnections() {
		return parallelConnections;
	}

	/**
	 * @param parallelConnections the parallelConnections to set
	 */
	public ExportBuilder setParallelConnections(boolean parallelConnections) {
		this.parallelConnections = parallelConnections;
		return this;
	}

}
