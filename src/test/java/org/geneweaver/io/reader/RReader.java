package org.geneweaver.io.reader;

import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

import org.renjin.serialization.RDataReader;
import org.renjin.sexp.SEXP;

/**
 * @see https://groups.google.com/g/renjin-dev/c/qnVfH1HQ2Ps?pli=1
 * @author gerrim
 *
 */
public class RReader {

	
	public static void main(String[] args) throws Exception {
		
		try (RDataReader reader = new RDataReader(new GZIPInputStream(new FileInputStream("/Volumes/jax-data/data/variant-orthology/ensembl-102/DOHeartNew.rdata")))) {
			
			SEXP results = reader.readFile();

			System.out.println(results);
		}
	}
}
