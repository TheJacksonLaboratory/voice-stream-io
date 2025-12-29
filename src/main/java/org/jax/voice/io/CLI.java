package org.jax.voice.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jax.voice.io.reader.ReaderException;
import org.jax.voice.io.writer.JsonToCSVParser;

import com.fasterxml.jackson.core.JsonParseException;

public class CLI {

	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		try {
			CommandLine cmd = parse(args);
			if (cmd==null) return;
			run(cmd, System.getenv());
		} finally {
			System.exit(0); // Just in case demons started up, we force exit.
		}
	}
	
	/**
	 * Method for main and unit tests to call.
	 * @param args
	 * @param env
	 * @throws PartitionException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParseException 
	 * @throws ReaderException 
	 */
	private static void run(CommandLine cmd, Map<String, String> env) throws Exception {
		
		if (!checkEnv(env)) return;
		
		if (cmd.hasOption("convert")) {
			convert(cmd);
		}
	}

	private static void convert(CommandLine cmd) throws JsonParseException, IOException {
		
		Path json = Paths.get(cmd.getOptionValue('i'));
		if (!Files.exists(json)) throw new IllegalArgumentException("File does not exist: "+json);
		
		Path csv = Paths.get(cmd.getOptionValue('o'));
		String delim = cmd.getOptionValue('d', ",");
		if (delim.equals("TAB")) delim = "\t";
		
		JsonToCSVParser parser = new JsonToCSVParser();
		parser.setDelimiter(delim);
		parser.convert(json, csv);
	}

	private static boolean checkEnv(Map<String, String> env) {
		// We do not require a particular environment at the moment.
		// TODO Later we may need to check it... 
		return true;
	}

	/**
	 * Just deals with parsing the args.
	 * @param args
	 * @return
	 * @throws ParseException 
	 */
	private static CommandLine parse(String[] args) throws ParseException {

		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Importer", options);
			throw e;
		}
		return cmd;
	}

	private static Options getOptions() {
		Options options = new Options();

		// Mode switches
		Option convert = new Option("convert", false, "Convert files switch. Use this to convert a json file created with Neo4j query to csv.");
		convert.setRequired(true); // We always force the switch in case of future commands.
		options.addOption(convert);
		
		// Command line values
		Option input = new Option("i", "input", true, "The input file which we convert (e.g. json) .");
		input.setRequired(true);
		options.addOption(input);

		Option output = new Option("o", "output", true, "The output file which we write (e.g. for convert a csv table).");
		output.setRequired(true);
		options.addOption(output);

		Option delim = new Option("d", "delimiter", true, "The delimiter to use. Default is ',' and 'TAB' can be used for a tab character.");
		delim.setRequired(false);
		options.addOption(delim);

		return options;
	}
	
	/**
	 * Test method
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParseException 
	 * @throws ReaderException 
	 */
	public static void testRun(String... args) throws Exception {
		testRun(args, System.getenv());
	}

	/**
	 * Just to test the command line.
	 * @param args
	 * @param env
	 * @throws PartitionException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ParseException 
	 * @throws ReaderException 
	 */
	static void testRun(String[] args, Map<String, String> env) throws Exception {
		CommandLine cmd = parse(args);
		if (cmd==null) throw new IOException("Invalid command line!");
		run(cmd, env);
	}

	/**
	 * Get an env, a system or a default.
	 * @param env
	 * @param prop
	 * @param def
	 * @return value or default
	 */
	public static String get(String env, String prop, String def) {
		String venv = System.getenv(env);
		if (venv!=null && !venv.trim().isEmpty()) return venv;
		return System.getProperty(prop, def);
	}

}
