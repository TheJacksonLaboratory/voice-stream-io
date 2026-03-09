package org.jax.voice.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class to encapsulate the redirection of log messages to a file instead
 * is using System.out.
 * 
 * Includes concept of maximum lines in the file and zipping the file.
 * This somewhat mirrors the functionality of the Slf4j class 
 * which is intentional because we simply configure it with one -Dlog=<file>
 * which avoids all the shenanigans of slf4j conf for this CLI tool.
 * 
 * The command line used to run this tool is: java -Dlog=$log ... where
 * $log is the file to write to which may end with gz if it should be zipped.
 */
public class LogRedirector implements IPrintStream {

	// File cannot grow beyond this size
	private int maxLines = Integer.getInteger("lines", 100000);
	
	// When file grows beyond this size, remove this many lines
	private int removeAmount = Integer.getInteger("rinc", 1000);
	
	private int lineCount = 0;
	
	private PrintStream out;

	private boolean lineManagementActive=true;

	private Path output;
	
	public LogRedirector(PrintStream out) {
		this.out = out;
		this.lineManagementActive = false;
	}

	public LogRedirector(String path) {
		this(Paths.get(path));
	}

	public LogRedirector(Path path) {
		this.output = path;
		createPrintStream(path, true);
	}

	public LogRedirector(Path path, int max, int remove) {
		this.output = path;
		createPrintStream(path, true);
		this.maxLines = max;
		this.removeAmount = remove;
	}

	private void createPrintStream(Path path, boolean startNew) {
		
		try {
			StandardOpenOption[] options = startNew ? new StandardOpenOption[] {} : new StandardOpenOption[] {StandardOpenOption.APPEND};
			OutputStream out = Files.newOutputStream(path, options);
			if (path.getFileName().toString().toLowerCase().endsWith(".gz")) {
				out = new GZIPOutputStream(out);
			}
			this.out = new PrintStream(out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkLineCount() {
		
		if (lineCount <= maxLines) return; // Nothing to do
		
		try {
			out.close();
			
			List<String> lines = null;
			
			// Read lines without starting lines
			try (BufferedReader br = createBufferedReader(output)) {
				lines = br.lines().skip(removeAmount).toList();
			}
			
			// Rewrite file with remaining lines
			try (BufferedWriter br = createBufferedWriter(output)) {
				lines.stream().forEach(line->{
					try {
						br.write(line);
						br.newLine();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}
		
		} catch (Exception ne) {
			ne.printStackTrace(System.err);
			
		} finally {
			
			// Setup print stream again
			createPrintStream(this.output, false);
		}
	}

	static BufferedReader createBufferedReader(Path path) throws IOException {
		
		InputStream in = Files.newInputStream(path);
		if (path.getFileName().toString().toLowerCase().endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		return new BufferedReader(new InputStreamReader(in));
	}
	
	static BufferedWriter createBufferedWriter(Path path) throws IOException {
		
		OutputStream out = Files.newOutputStream(path);
		if (path.getFileName().toString().toLowerCase().endsWith(".gz")) {
			out = new GZIPOutputStream(out);
		}
		return new BufferedWriter(new OutputStreamWriter(out));
	}

	/**
	 * Synchronized action to print a line.
	 * If multiple threads call, each call is blocking to avoid
	 * conflict during line management
	 * @param s
	 */
	@Override
	public synchronized void println(String s) {
		out.println(s);
		if (!lineManagementActive) return;
		lineCount++;
		checkLineCount();
	}

	@Override
	public PrintStream getPrintStream() {
		return out;
	}

	/**
	 * @return the maxLines
	 */
	public int getMaxLines() {
		return maxLines;
	}

	/**
	 * @param maxLines the maxLines to set
	 */
	public void setMaxLines(int maxLines) {
		this.maxLines = maxLines;
	}

	/**
	 * @return the removeAmount
	 */
	public int getRemoveAmount() {
		return removeAmount;
	}

	/**
	 * @param removeAmount the removeAmount to set
	 */
	public void setRemoveAmount(int removeAmount) {
		this.removeAmount = removeAmount;
	}

	public Path getLog() {
		return output;
	}

}
