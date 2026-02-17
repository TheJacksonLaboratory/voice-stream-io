package org.jax.voice.io.connector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.jax.voice.domain.interval.FlatIntervalTree;
import org.jax.voice.domain.interval.Interval;
import org.jax.voice.io.IPrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalMarshall {

	private static final Logger logger = LoggerFactory.getLogger(IntervalMarshall.class);
	
	/**
	 * Used in unit tests to make the tree for a given set.
	 * @param file
	 * @param lchr
	 * @return
	 * @throws IOException
	 */
	public static FlatIntervalTree createTree(Path file, String chr) throws IOException {
		return createTree(file, chr, IPrintStream.of(System.out));
	}
	
	/**
	 * Used in unit tests to make the tree for a given set.
	 * @param dir
	 * @param chr
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static FlatIntervalTree createTree(Path file, String lchr,  IPrintStream log) throws IOException {
		
		log.println("Creating tree for "+file.getFileName());
		
		final String chr = lchr.toUpperCase();
		Path dir = file.getParent();
		List<Path> files = Files.list(dir)
				.filter(p->isIntervalFile(file, p, chr))
				.toList();

		List<Interval> intervals = new LinkedList<>();
		for (Path f : files) {
			try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(f))) {
				intervals.addAll((List<Interval>)ois.readObject());
			} catch (Exception e) {
				logger.error("Cannot load existing intervals for "+chr, e);
			}
		}

		FlatIntervalTree tree = new FlatIntervalTree(intervals);
		try (ObjectOutputStream ois = new ObjectOutputStream(Files.newOutputStream(file))) {
			ois.writeObject(tree);
		}
		return tree;
	}

	public static final Pattern pattern = Pattern.compile("^([A-Za-z]+)_(\\d+|X|Y|M|NA).ser$");

	private static boolean isIntervalFile(Path treeFile, Path interFile, String chr) {
		// treeFile is Variant_1.ser or Gene_15.ser
		Matcher m = pattern.matcher(treeFile.getFileName().toString());
		if (m.matches()) {
			String base = m.group(1);
			boolean isFile = interFile.getFileName()
							.toString()
							.matches(base+"_intervals."+chr+".[a-zA-Z]{6}.ser");
			return isFile;
		}
		return false;
	}
	
	public static FlatIntervalTree loadTree(String basePath, String chr, IPrintStream log) throws Exception {
		
		chr = chr.toUpperCase();
		String path = basePath+"_"+chr;
		
		synchronized(path.intern()) {
			
			Path file = Paths.get(path+".ser");

			// If we made it, reload it.
			if (Files.exists(file)) {
				log.println("Loading tree for "+chr+" from file: "+file);
				try {
					return IntervalMarshall.loadTree(file);
				} catch (ClassNotFoundException | IOException e) {
					log.println("Error loading tree for "+chr+" from file: "+file+" - "+e.getMessage());
					throw e;
				}
			}
			
			// If we have not made it, load all the fragments into one intervals tree.
			log.println("WARNING: Making tree which should not happen during streaming!");
			try {
				return IntervalMarshall.createTree(file, chr, log);
			} catch (IOException e) {
				log.println("Error loading tree for "+chr+" from file: "+file+" - "+e.getMessage());
				throw e;
			}
		}
	}

	public static FlatIntervalTree loadTree(Path file) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
			return (FlatIntervalTree)ois.readObject();
		}
	}

	static void saveIntervals(Path path, List<Interval> intervals) throws IOException {
		
		path.getParent().toFile().mkdirs();
		try (ObjectOutputStream ois = new ObjectOutputStream(Files.newOutputStream(path))) {
			ois.writeObject(intervals);
		}

	}
	

	public static void saveIntervals(String basePath, String chr, List<Interval> intervals, IPrintStream out) throws IOException {
		
		chr = chr.toUpperCase();
		String unc = RandomStringUtils.secure().nextAlphabetic(6);
		Path path = Paths.get(basePath+"_intervals."+chr+"."+unc+".ser");

		// In the unlikely event that the file already exists, 
		// look for one not taken. This is still not entirely
		// concurrent safe but should be good enough for our use case.
		while (Files.exists(path)) {
			// Unlikley but not impossible
			out.println("WARNING: "+path.getFileName()+" exists");
			unc = RandomStringUtils.secure().nextAlphabetic(6);
			path = Paths.get(basePath+"_intervals."+chr+"."+unc+".ser");
		}
		
		// This is still not entirely thread safe because two threads could
		// generate the same random string at the same time, but the odds of that are astronomically 
		// low and this is only used in one off build from which the logs can be checked.
		
		saveIntervals(path, intervals);
	}

	public static long getIntervalFileSize(Path path) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			FlatIntervalTree tree = (FlatIntervalTree)ois.readObject();
			return tree.size();
		} 
	}

	public static List<Path> createTrees(Path dir) throws ClassNotFoundException, IOException {
		return createTrees(dir, dir, IPrintStream.of(System.out), true);
	}
	
	public static List<Path> createTrees(Path dir, IPrintStream out, boolean delete) throws ClassNotFoundException, IOException {
		return createTrees(dir, dir, out, delete);
	}
	
	private static ExecutorService executor;
	static {
		int par = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		executor = Executors.newFixedThreadPool(par);
	}

	private static final Pattern shardPattern = Pattern.compile("^([A-Za-z]+)_intervals\\.(\\d+|X|Y|M|NA)\\.([a-zA-Z]{6})\\.ser$");

	/**
	 * Create all the trees from the interval files.
	 *
	 * Expected input filenames:
	 *   <type>_intervals.<chr>.<random 6 letters>.ser
	 * And outputs:
	 *   <type>_<chr>.ser
	 *
	 * Groups all interval shards by (type, chr), loads all contained Interval lists,
	 * builds a FlatIntervalTree, and writes the merged tree to the output file.
	 *
	 * @param dir directory containing interval shard files
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static List<Path> createTrees(Path dir, Path outDir, IPrintStream out, boolean delete) throws IOException, ClassNotFoundException {
		
		try {
			if (dir == null) {
				throw new IllegalArgumentException("dir cannot be null");
			}
			if (!Files.isDirectory(dir)) {
				throw new IllegalArgumentException("Not a directory: " + dir);
			}
	
			out.println("Reading files in "+dir);
	
			// Keep insertion order for stable/log-friendly processing.
			final Map<String, List<Path>> groups = new LinkedHashMap<>();
			Files.list(dir).filter(Files::isRegularFile).forEach(p -> {
				Matcher m = shardPattern.matcher(p.getFileName().toString());
				if (!m.matches()) return;
				String type = m.group(1);
				String chr = m.group(2);
				String key = type + "\t" + chr;
				groups.computeIfAbsent(key, k -> new LinkedList<>()).add(p);
			});
	
			if (groups.isEmpty()) {
				out.println("No interval shard files found in "+dir);
				return null;
			}
			
			out.println("Making trees for "+dir);
	
			List<Future<Path>> outputFiles = new LinkedList<>();
			for (Map.Entry<String, List<Path>> entry : groups.entrySet()) {
				outputFiles.add(executor.submit(
						()->treeForChromosome(entry.getKey(), entry.getValue(), 
												outDir, out, delete))
					);
			}
			
			return outputFiles.stream().map(f -> {
				try {
					return f.get();
				} catch (Exception e) {
					out.println("Error creating tree: " + e.getMessage());
					e.printStackTrace(out.getPrintStream());
					return null;
				}
			}).filter(p -> p != null).toList();
			
		} catch (Exception e) {
			out.println(e.getMessage());
			e.printStackTrace(out.getPrintStream());
			throw e;
		}
	}
	
	private static Path treeForChromosome(String key, List<Path> files, 
											Path outDir, IPrintStream out, 
											boolean delete) throws IOException, ClassNotFoundException {
		
		String[] parts = key.split("\t", 2);
		String type = parts[0];
		String chr = parts[1];

		Path outFile = outDir.resolve(type + "_" + chr + ".ser");
		String msg = String.format("Creating tree %s from %s shard file(s)", outFile.getFileName(), files.size());
		out.println(msg);

		// This will be ~10 mill in size.
		List<Interval> intervals = new LinkedList<>();
		for (Path shard : files) {
			try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(shard))) {
				@SuppressWarnings("unchecked")
				List<Interval> shardIntervals = (List<Interval>) ois.readObject();
				if (shardIntervals != null) {
					out.println("Loaded "+shard.getFileName());
					intervals.addAll(shardIntervals);
				}
			}
			if (delete) {
				Files.delete(shard);
			}
		}

		out.println("Making tree ");
		FlatIntervalTree tree = new FlatIntervalTree(intervals);
		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(outFile))) {
			oos.writeObject(tree);
			out.println("Wrote "+outFile.getFileName());
		}
		return outFile;
		
	}

}