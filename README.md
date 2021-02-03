# Gene Weaver Stream IO #

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
``` Java

// The reader can have type Entity or a concrete class like 'Variant' if every line is the same type.
StreamReader<Gene> reader = ReaderFactory.getReader(new ReaderRequest("Homo sapiens", new File(...));

// Optionally get a connector. The Reader will have a default connector in most cases or make your own
Function<NamedEntity, Stream<Entity> connector = ... // e.g. GeneConnector  or reader.defaultConnector()

// Optionally create a filter, not all objects do we want perhaps
Predicate<Gene> filter = g->"protein_coding".equalsIgnoreCase(g.getBioType());

// When writing domain objects to csv we can override delimiter per object or system wide:
System.setProperty("delimiter", ",");

// For the sake of a demonstration, we pipe the gene to a CSV file.
try (BufferedWriter writer = new Files.newBufferedWriter("~/mygenes.csv")) {
	
	writer.write(new Gene().getHeader());
	writer.newLine();

	// Do something with the stream of objects.
	reader.stream()								// Create a stream of types, e.g. Entity
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

## Developing
This is a pure maven project. Please check out using git and then use common maven commands such as:
* mvn verify							# Run tests
* mvn package -DskipTests=true		# Make jars, no tests
* mvn deploy							# Set artifacts to repository for other projects to use.
If you are deploying a new version, do not forget to change the version number which the jar is using.

If you are planning on running the tests you will need to clone the large test data repository:

```
git clone https://bitbucket.org/geneweaver/gweaver-test-data.git
```