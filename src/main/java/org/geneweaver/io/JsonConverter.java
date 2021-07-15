package org.geneweaver.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geneweaver.io.writer.JsonToCSVParser;

/**
 * Main method entry point for running conversions.
 * 
 * @author gerrim
 *
 */
public class JsonConverter {

	/**
	 * Arguments are:
	 * 1. Path from (json)
	 * 2. Path to (csv)
	 * 3. Optional delimiter default is "," enter "TAB" for tab character.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		
		if (args.length==1) {
			if ("-version".equals(args[0])) {
				String vers = JsonConverter.class.getPackage().getImplementationVersion();
				if (vers!=null) {
					System.out.println(vers);
				} else {
					System.out.println("1.3.1");
				}
				return;
			}
		}
		
		if (args.length!=2 && args.length!=3) {
			System.out.println("Invalid arguments! Required arguments are: 	 \n"
					+ "  * 1. Path from (json)\n"
					+ "	 * 2. Path to (csv)\n"
					+ "	 * 3. Optional delimiter. Default is \",\" enter \"TAB\" for tab character.");
			return;
		}
		
		Path json = Paths.get(args[0]);
		if (!Files.exists(json)) {
			System.out.println("File "+args[0]+" does not exist!");
			return;
		}
		
		Path csv = Paths.get(args[1]);

		JsonToCSVParser parser = new JsonToCSVParser();
		String delimiter = ",";
		if (args.length==3) delimiter = args[2];
		if (delimiter.equals("TAB")) delimiter = "\t";
		parser.setDelimiter(delimiter);
		
		try {
			parser.convert(json, csv);
		} catch (Exception ne) {
			System.out.println(ne.getMessage());
		}
	}
}
