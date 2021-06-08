![](https://travis-ci.com/geneweaver/gweaver-stream-io.svg?branch=master)


#  ![](./icons/GW2-logo-blue.png) Gene Weaver Stream IO #


## Introduction
This project is designed to be used as a library for reading different file formats. It is intended
to be consumable as a maven artifact from artifactory or maven central. StreamReader is returned
from ReaderFactory which finds the correct format for the user. The differences between this IO 
implementation and many other readers for the formats which it supports (gtf, gvf, bed, xls, etc.) is:
1. It is stream based, designed to read the file into a stream of domain objects.
2. It does not hold any file's data in memory so may be used for large files.
3. It is designed to have flat map operations on the stream for connecting objects.
4. The domain objects are pre-tagged with neo4j annotations to make it easy to perform transactions.
5. Domain objects are designed to not form an in-memory complex graph to reduce dependencies and increase speed.
6. Multi-threaded operations are supported for some Readers e.g. parallel streams.

## Example Usage

### Example - Count active lines in a gzipped bed file
``` java
	StreamReader<NamedEntity> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", new File("Hs_EPDnew_006_hg381.bed.gz")));
	assertEquals(29598, reader.stream().count());	
```

### Example - Process Genes and Transcripts from a gtf file.
``` Java

// The reader can have type Entity or a concrete class like 'Variant' if every line is the same type.
StreamReader<Gene> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", new File("hs.gtf"));

// Optionally get a connector. The Reader will have a default connector in most cases or make your own
// In this case the connector for gtf connects Genes with Transcripts
Function<NamedEntity, Stream<Entity>> connector = reader.defaultConnector()

// Optionally create a filter, not all objects do we want perhaps
Predicate<Gene> filter = g->"protein_coding".equalsIgnoreCase(g.getBioType());

// When writing domain objects to csv we can override delimiter per object or system wide:
System.setProperty("delimiter", ",");

// For the sake of a demonstration, we pipe the gene to a CSV file.
try (BufferedWriter writer = new Files.newBufferedWriter("~/mygenes.csv")) {
	
	writer.write(new Gene().getHeader());
	writer.newLine();

	// Do something with the stream of objects.
	reader.stream()							// Create a stream of types, e.g. Entity
		.filter(filter)							// We only want a certain biotype
		.flatMap(entity->connector.apply(entity))			// Use the connector to figure out what is connected to what e.g. Gene to Transcript via Produces
		.forEach(n->{  							// Do something with each item. For instance BufferedWriters may be open to pipe objects found to file(s). Here we dump all into one file, just as an example
				writer.write(n.getClass().getSimpleName().toUpperCase());
				writer.write(",");
				writer.write(n.toCSV());
				writer.newLine();
			});	 				
}
```

### Example - Process all files in eQTL tar using stream also map their rsId and tissue attributes as you go
``` java
// GTEx version 8
StreamReader<EQTL> reader = ReaderFactory.getReader(new ReaderRequest(new File("GTEx_Analysis_v8_eQTL.tar")));
File lookup = new File("GTEx_Analysis_2017-06-05_v8_WholeGenomeSeq_838Indiv_Analysis_Freeze.lookup_table.txt.gz");
File samples = new File("GTEx_Analysis_v8_Annotations_SampleAttributesDS.txt");

try (EQTLFunction<EQTL, EQTL> func = new EQTLFunction<EQTL, EQTL>(lookup, samples);
	BufferedWriter w = Files.newBufferedWriter(new File("eqtls.tsv");) {
	w.write("eqtlVariantId\tchr\trsId\ttissueName");
	w.newLine();

	// Set the tissue and correct rsId in the EQTL object
	// Write to custom file in this example.
	long count = reader.stream()
				 .map(func::apply)
				 .filter(e->e.getRsId()!=null)
				 .forEach(e -> {
					w.write(e.getEqtlVariantId());
					w.write('\t');
					w.write(e.getChr());
					w.write("\t");
					w.write(e.getRsId());
					w.write("\t");
					w.write(e.getTissueName());
					w.newLine();
				 });
	// There will be 72686455 EQTLs processed.
}
```
### Example - Count chromatin interactions in ChIA-PET File
``` java
	AbstractXlsReader<ChromatinInteraction, ?> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", new File("NIHMS345629-supplement-02.xls.gz")));
	reader.setSheetIndex(2);
	reader.setConcreteClass(Anchor.class);
	assertEquals(14604, reader.stream().count());	
```

Other examples see tests in: src/test/java/org/geneweaver/io/reader

## Reusing
This artifact is indexed on Maven Central:

``` maven
<!-- https://mvnrepository.com/artifact/org.geneweaver/gweaver-stream-io -->
<dependency>
    <groupId>org.geneweaver</groupId>
    <artifactId>gweaver-stream-io</artifactId>
    <version>1.3.0</version>
</dependency>
```

``` groovy
// https://mvnrepository.com/artifact/org.geneweaver/gweaver-stream-io
implementation group: 'org.geneweaver', name: 'gweaver-stream-io', version: '1.3.0'
```

## Developing
This is a pure maven project. Please check out using git and then use common maven commands such as:
* mvn verify										# Run tests
* mvn package -DskipTests=true					# Make jars, no tests
* mvn deploy										# Set artifacts to repository for other projects to use.
* mvn clean release:prepare release:perform  	# FULL RELEASE. optional:  -DskipTests=true -Djacoco.skip=true
If you are deploying a new version, do not forget to change the version number which the jar is using.

If you are planning on running the tests you will need to clone the large test data repository:

```
git clone https://bitbucket.org/geneweaver/gweaver-test-data.git
```