package org.jax.voice.io.connector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
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
	public static FlatIntervalTree createTree(Path file, String chr) throws IOException {
		
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

	public static final Pattern pattern = Pattern.compile("^([A-Za-z]+)_(\\d+|X|Y|M|NA|nA).ser$");

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

}
