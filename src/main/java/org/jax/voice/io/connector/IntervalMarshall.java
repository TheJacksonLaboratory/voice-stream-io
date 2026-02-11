package org.jax.voice.io.connector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jax.voice.domain.interval.FlatIntervalTree;
import org.jax.voice.domain.interval.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalMarshall {

	private static final Logger logger = LoggerFactory.getLogger(IntervalMarshall.class);
	/**
	 * Used in unit tests to make the tree for a given set.
	 * @param dir
	 * @param chr
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static FlatIntervalTree createTree(Path file, String lchr) throws IOException {
		
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
	
	public static FlatIntervalTree loadTree(Path file) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
			return (FlatIntervalTree)ois.readObject();
		}
	}

	public static void saveIntervals(Path path, List<Interval> intervals) throws IOException {
		try (ObjectOutputStream ois = new ObjectOutputStream(Files.newOutputStream(path))) {
			ois.writeObject(intervals);
		}
	}

	public static long getIntervalFileSize(Path path) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			FlatIntervalTree tree = (FlatIntervalTree)ois.readObject();
			return tree.size();
		} 
	}

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
	public static List<Path> createTrees(Path dir) throws IOException, ClassNotFoundException {
		if (dir == null) {
			throw new IllegalArgumentException("dir cannot be null");
		}
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException("Not a directory: " + dir);
		}

		final Pattern shardPattern = Pattern.compile("^([A-Za-z]+)_intervals\\.(\\d+|X|Y|M|NA)\\.[a-zA-Z]{6}\\.ser$");

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
			logger.info("No interval shard files found in {}", dir);
			return null;
		}

		List<Path> outputFiles = new LinkedList<>();
		for (Map.Entry<String, List<Path>> entry : groups.entrySet()) {
			String[] parts = entry.getKey().split("\t", 2);
			String type = parts[0];
			String chr = parts[1];
			List<Path> shardFiles = entry.getValue();

			Path outFile = dir.resolve(type + "_" + chr + ".ser");
			logger.info("Creating tree {} from {} shard file(s)", outFile.getFileName(), shardFiles.size());

			List<Interval> intervals = new LinkedList<>();
			for (Path shard : shardFiles) {
				try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(shard))) {
					@SuppressWarnings("unchecked")
					List<Interval> shardIntervals = (List<Interval>) ois.readObject();
					if (shardIntervals != null) intervals.addAll(shardIntervals);
				}
			}

			FlatIntervalTree tree = new FlatIntervalTree(intervals);
			try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(outFile))) {
				oos.writeObject(tree);
				outputFiles.add(outFile);
			}
		}
		return outputFiles;
	}

}