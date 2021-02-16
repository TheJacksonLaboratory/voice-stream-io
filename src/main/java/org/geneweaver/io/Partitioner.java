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
package org.geneweaver.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
/**
 * A class to injest large data files of gvf or gft data and
 * spit out partitions of these files (multiple smaller files).
 * This is desirable in GCP because it means large files can have their
 * graphs created using a scatter / gather approach.
 * 
 * @author gerrim
 *
 */
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geneweaver.io.Configuration.FileType;
import org.geneweaver.io.Configuration.ZipType;
import org.geneweaver.io.reader.Expander;

// TODO: Auto-generated Javadoc
/**
 * Partitions without having to put lines in domain objects.
 * 
 * @author gerrim
 *
 */
public class Partitioner {

	/** The directory to which we expand. */
	private final Path dir;
	
	/** The configuration. */
	private final Configuration conf;
	
	/**
	 * Some of the visitors upload files. We do not want hundreds of
	 * files all uploading so we limit that with a semaphore.
	 */
	private Semaphore semaphore;

	/**
	 * The partition count, starting at 0. It is reset every time
	 * partition is called.
	 */
	private int partitionCount = 0;
	
	/**
	 * Instantiates a new partitioner.
	 *
	 * @param dir the dir
	 * @param conf the conf
	 * @throws PartitionException the partition exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Partitioner(File dir, Configuration conf) throws PartitionException, IOException {
		this(dir.toPath(), conf);
	}

	/**
	 * Instantiates a new partitioner.
	 *
	 * @param dir the dir
	 * @param conf the conf
	 * @throws PartitionException the partition exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Partitioner(Path dir, Configuration conf) throws PartitionException, IOException {
		if (!Files.exists(dir)) Files.createDirectories(dir);
		if (!Files.isDirectory(dir)) throw new PartitionException("The partition directory should be an empty existing dir "+dir);
		this.dir = dir;
		this.conf = conf;
		this.semaphore = new Semaphore(conf.getPermits());
	}
	
	/**
	 * Partitions the file.
	 *
	 * @param source the source
	 * @param visitor the visitor
	 * @throws InterruptedException the interrupted exception
	 * @throws PartitionException the partition exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized void partition(Path source, Consumer<Path> visitor) throws InterruptedException, PartitionException, IOException {
		
		this.partitionCount = 0;
		if (!Files.exists(source)) throw new NoSuchFileException("Cannot find file "+source);
		
		ExecutorService service = visitor != null ? Executors.newCachedThreadPool() : null;
		
		String fileName = source.getFileName().toString().toLowerCase();
		if (fileName.endsWith(".gz")) { 
			// The downloaded files are often gzipped.
			// This makes them a lot smaller to download.
			// We support this option such that they can be partitioned
			// directly without unzipping somewhere (which requires huge amounts of memory).
			gpartition(source, visitor, service);
			
		} else if (!fileName.endsWith(".zip")) {
			spartition(source, visitor, service);
			
		} else {
			try (Expander expander = new Expander()) {
				List<Path> paths = expander.expand(source);
				// This being parallel is probably not required in production
				// however it speeds up the large test by 20s.
				paths.stream().parallel().forEach(path->spartition(path, visitor, service));
				
				// Try to wait until the expanded files have been used
				// expander.close() cleans the temp directory.
				if (service!=null) service.awaitTermination(conf.getTimeout(), conf.getUnit());
			}
		}

		if (service!=null) {
			service.shutdown();
			boolean done = service.awaitTermination(conf.getTimeout(), conf.getUnit());
			if (!done) throw new PartitionException("Notification of parition visitors did not complete within timeout!");
		}
	}

	/**
	 * Gpartition.
	 *
	 * @param sourcePath the source path
	 * @param visitor the visitor
	 * @param service the service
	 * @throws PartitionException the partition exception
	 */
	private void gpartition(Path sourcePath, Consumer<Path> visitor, ExecutorService service) throws PartitionException {
		
		FileType fileType = getFileType(sourcePath);
		Path partition		  = null;
		BufferedWriter writer = null;
		try {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(sourcePath.toFile()))))) {
				
				int pcount = 0;
				String nextLine = null;
				while((nextLine = reader.readLine()) != null) {
					
					if (partitionComplete(pcount, nextLine, fileType)) { // We write until after the partition count but before the start of the next Gene.
						if (writer!=null) visit(writer, partition, visitor, service);
						
						// TODO if source is a zip, the path is the next entry...
						partition = newPartition(dir, conf, sourcePath);
						writer = Files.newBufferedWriter(partition);
						pcount = 0;
					}
	
					writer.write(nextLine);
					writer.newLine();
					++pcount;
				}
				
			} finally {
				visit(writer, partition, visitor, service);
			}
		} catch (IOException ne) {
			throw new PartitionException(ne);
		}
	}

	/**
	 * Spartition.
	 *
	 * @param sourcePath the source path
	 * @param visitor the visitor
	 * @param service the service
	 * @throws PartitionException the partition exception
	 */
	private void spartition(Path sourcePath, Consumer<Path> visitor, ExecutorService service) throws PartitionException {
		
		FileType fileType = getFileType(sourcePath);
		Path partition		  = null;
		BufferedWriter writer = null;
		try {
			try (Scanner scanner = new Scanner(sourcePath)) {
				
				int pcount = 0;
				while(scanner.hasNextLine()) {
					
					String nextLine = scanner.nextLine();
					if (partitionComplete(pcount, nextLine, fileType)) { // We write until after the partition count but before the start of the next Gene.
						if (writer!=null) visit(writer, partition, visitor, service);
						
						partition = newPartition(dir, conf, sourcePath);
						writer = Files.newBufferedWriter(partition);
						pcount = 0;
					}
	
					// It might actually be faster to 
					// use Files.write(...) and do the whole lot at once.
					writer.write(nextLine);
					writer.newLine();
					++pcount;
				}
				
			} finally {
				visit(writer, partition, visitor, service);
			}
		} catch (IOException ne) {
			throw new PartitionException(ne);
		}
	}

	/**
	 * Gets the file type.
	 *
	 * @param source the source
	 * @return the file type
	 * @throws PartitionException the partition exception
	 */
	private FileType getFileType(Path source) throws PartitionException {
			
		FileType type = FileType.UNKNOWN;
		String fileName = source.getFileName().toString().toLowerCase();
		if (fileName.endsWith(".gtf") || fileName.endsWith(".gtf.zip") || fileName.endsWith(".gtf.gz")) {
			type = FileType.GENE;
		} else if (fileName.endsWith(".gvf") || fileName.endsWith(".gvf.zip") || fileName.endsWith(".gvf.gz")) {
			type = FileType.VARIANT;
		}
		
		if (type == FileType.UNKNOWN) throw new PartitionException("Cannot determine type of file to partition!");
		return type;
	}

	/**
	 * Partition complete.
	 *
	 * @param pcount the pcount
	 * @param nextLine the next line
	 * @param ftype the ftype
	 * @return true, if successful
	 */
	private boolean partitionComplete(int pcount, String nextLine, FileType ftype) {
		
		if (pcount == 0) return true; // We do one at the start to get things moving.
		
		boolean newPartReq = pcount >= conf.getPartitionLines();
		
		if (newPartReq && ftype==FileType.GENE) { 
			// Check that we are the end of a Gene if we are a Gene file.
			// In this way we only write full gene sections which will work
			// correctly when the partitions are processed in parallel.
			String line = nextLine.trim();
			if (line.startsWith("#")) return false; // Do do not break on a comment
			
			String[] rec = line.split("\t");
			String type = rec[2];
			if (!"gene".equals(type.toLowerCase())) {
				return false; // We wait for a gene or the end of the file.
			}
		}
		
		return newPartReq;
	}

	/**
	 * Visit.
	 *
	 * @param writer the writer
	 * @param partition the partition
	 * @param visitor the visitor
	 * @param service the service
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private final void visit(BufferedWriter writer, Path partition, 
					   Consumer<Path> visitor, ExecutorService service) throws IOException {
		
		try {
			semaphore.acquire();
	
			if (writer!=null) {
				writer.flush();
				writer.close();
			}
			
			if (conf.getZipType() == ZipType.ZIP) {
				Path zip = partition.getParent().resolve(partition.getFileName()+".zip");
				try (InputStream in = new FileInputStream(partition.toFile());
						ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip.toFile()))){
					out.putNextEntry(new ZipEntry(partition.getFileName().toString()));
					IOUtils.copy(in, out);
				}
				Files.delete(partition);
				partition = zip;
				
			} else if (conf.getZipType() == ZipType.GZ) {
				Path gz = partition.getParent().resolve(partition.getFileName()+".gz");
				try (InputStream in = new FileInputStream(partition.toFile());
					 GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gz.toFile()))){
	
					IOUtils.copy(in, out);
				}
				Files.delete(partition);
				partition = gz;
			}
			
			if (service!= null && visitor!=null && Files.size(partition) > 0) {
				final Path part = partition;
				service.submit(()->{visitor.accept(part); semaphore.release();});
			}
		} catch (InterruptedException i) {
			throw new PartitionException(i);
		}

	}

	/**
	 * New partition.
	 *
	 * @param dir the dir
	 * @param conf the conf
	 * @param src the src
	 * @return the path
	 * @throws PartitionException the partition exception
	 */
	private final Path newPartition(Path dir, Configuration conf, Path src) throws PartitionException {
		
		
		String fileName = src.getFileName().toString().toLowerCase();
		String baseName = FilenameUtils.getBaseName(fileName);
		String ext = FilenameUtils.getExtension(fileName); // No .
		if (fileName.endsWith(".zip")) throw new PartitionException("The source file when partitioning should not end in .zip!");
		
		if (fileName.endsWith(".gz")) { // We write the original name without gz
			ext = FilenameUtils.getExtension(baseName); // No .
			baseName = FilenameUtils.getBaseName(baseName);
		}
		
		String partName = baseName+"_"+partitionCount+"."+ext;
		partitionCount++;
		
		Path partition = dir.resolve(partName);
		if (Files.exists(partition)) throw new PartitionException("The file "+partName+" already exists in dir "+dir+"."
				+ " Please ensure that the directory to create partitions is empty.");
		
		return partition;
	}

	/**
	 * Gets the dir.
	 *
	 * @return the dir
	 */
	public Path getDir() {
		return dir;
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return conf;
	}

}
