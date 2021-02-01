# Gene Weaver Stream IO #

## Introduction
This project is designed to be used as a library for reading different file formats. It is intended
to be consumable as a maven artifact from artifactory or maven central. AbstractReader is returned
from ReaderFactory which finds the correct format for the user (or Readers can be used in
concrete form). The differences between this IO implementation and many other readers for the formats 
which it supports (gtf, gvf, bed, etc.) is:
1. It is stream based, designed to read the file into a stream of domain objects. 
2. It is designed to have flat map operations on the stream for connecting objects.
3. The domain objects are pre-tagged with neo4j annotations to  ake it easy to perform transactions.
4. The domain objects are designed to not form an in-memory complex graph to reduce dependencies and increase speed.
5. Multi-threaded operations are supported for some Readers e.g. parallel streams.

## Example Usage
``` Java

# The reader can have type Entity or a concrete class like 'Variant' if every line is the same type.
AbstractReader<NamedEntity> reader = ReaderFactory.getReader("Homo sapiens", new File(...));

# Get a connector. The Reader will have a default connector in most cases
Function<NamedEntity, Stream<Entity> connector = ... # e.g. GeneConnector  or reader.defaultConnector()

# Do something with the stream of objects.
reader.stream()								# Create a stream of types, e.g. Entity
	.flatMap(entity->connector.apply(entity))			# Use the connector to figure out what is connected to what e.g. Gene to Transcript via Produces
	.forEach(System.out::println); 				# Do something with each item. For instance BufferedWriters may be open to pipe objects found to file(s)

```

## Developing
This is a pure maven project. Please check out using git and then use common maven commands such as:
* mvn verify							# Run tests
* mvn package -DskipTests=true		# Make jars, no tests
* mvn deploy							# Set artifacts to repository for other projects to use.
If you are deploying a new version, do not forget to change the version number which the jar is using.