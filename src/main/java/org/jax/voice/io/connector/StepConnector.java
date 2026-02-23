package org.jax.voice.io.connector;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jax.voice.domain.AbstractEntity;
import org.jax.voice.domain.Contact;
import org.jax.voice.domain.Entity;
import org.jax.voice.domain.Gene;
import org.jax.voice.domain.Located;
import org.jax.voice.domain.Species;
import org.jax.voice.domain.Step;
import org.jax.voice.domain.Variant;
import org.jax.voice.io.IPrintStream;
import org.jax.voice.io.reader.ReaderRequest;
import org.neo4j.ogm.session.Session;

import com.google.common.collect.Sets;

/**
 * Class to create step file connections.
 * This class parses the special tss file and the variant file which come from CCSI.
 * Then we are able to look up locations in each step file as we do when we parse peaks
 * by location. This is not a fast process.
 * 
 * @author gerrim
 *
 */
public class StepConnector extends AbstractOverlapConnector<Step,Contact>  {

	/**
	 * Some of the input files are heterogeneous and
	 * we only want one entity such as "Gene" from the file.
	 */
	private final Class<?> clazz;

	private Path parentDirectory;
	
	/**
	 * Used when mapping the step file.
	 */
	public StepConnector() {
		super("Mus musculus");
		clazz  = null;
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setMode(OverlapRecordMode.DATABASE); // We only want to link to steps if they overlap the variant.
	}

	/**
	 * Used when caching the data sources.
	 * @param clazz
	 */
	public StepConnector(String species,Class<?> clazz) {
		this(species,clazz, clazz.getSimpleName()); // Or variants, we have to process both.
		setMode(OverlapRecordMode.DATABASE); // We only want to link to steps if they overlap the variant.
	}

	/**
	 * Create an overlap connector setting the base file name. 
	 * The database is sharded by file so this
	 * @param databaseFileName
	 */
	public StepConnector(String species, Class<?> clazz, String databaseFileName) {
		super(species);
		this.clazz = clazz;
		setTableName(System.getProperty("gweaver.mappingdb.tableName","REGIONS"));
		setFileName(databaseFileName);
		setMode(OverlapRecordMode.DATABASE); // We only want to link to steps if they overlap the variant.
	}
	
	/**
	 * Override for readers which read file formats whose objects
	 * do not fit a normal read and need mapping to use with the connector.
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Located coerce(Object e) {
		if (clazz==Variant.class && e instanceof Map) {
			return Entity.coerce((Map<String,Object>)e, new Variant());
		}
		if (clazz==Gene.class && e instanceof Gene) {
			fixId((Gene)e);
		}
		return (Located)e;
	}
	
	@Override
	protected void configure(ReaderRequest request) {
		if (clazz == Variant.class) {
			request.setDelimiter("\t");
			request.setIncludeAll(false);
		}
	}

	/**
	 * Override to filter class
	 * @param l
	 * @return true if class type is valid.
	 */
	protected boolean isValidClass(Object l) {
		return l.getClass()==clazz;
	}

	/**
	 * Method which gets the connections between Gene and Variant
	 * using this dataset which are known as CONTACT
	 */
	@Override
	public Stream<Contact> stream(Step step, Session session, IPrintStream log) {
		
		Located start = Located.at(step.getChr1(), step.getStart1(), step.getEnd1());		
		Set<String> geneIds = lookup(start, Gene.class, "ens", log);

		Located end = Located.at(step.getChr2(), step.getStart2(), step.getEnd2());		
		Set<String> rsIds = lookup(end, Variant.class, "rs", log);
		
		if (geneIds.isEmpty() || rsIds.isEmpty()) {
			return null;
		}
		return expand(step, geneIds, rsIds);
	}
	
	private Stream<Contact> expand(Step step, Set<String> geneIds, Set<String> rsIds) {
		Set<List<String>> combs = Sets.cartesianProduct(Arrays.asList(geneIds, rsIds));
		return combs.stream()
				    .map(ids->createContact(step, ids, geneIds, rsIds));
	}

	private Contact createContact(Step step, List<String> ids, Set<String> geneIds, Set<String> rsIds) {
		
		Contact contact = Contact.of(step);
		contact.setGeneId(ids.get(0));
		contact.setRsId(ids.get(1));
		contact.setChr(step.getChr1());
		contact.setSpecies(Species.code(species));
		return contact;
	}

	private Set<String> lookup(Located loc, Class<?> type, String prefix, IPrintStream log) {
		
		setFileName(type.getSimpleName());
		setLocation(getParentDirectory()); // Sorts out paths to databases
		
		String shardName = oservice.getShardName(loc.getChr(), loc.getStart());
		
		if (shardName!=null) {
	 		try {
				PreparedStatement lookup = getSelectStatement(loc.getChr(), shardName, log);
				if (lookup==null) { // Not all peaks have reasonable chromosomes.
					return null;
				}
				
				int a = Math.min(loc.getStart(), loc.getEnd());
				lookup.setInt(1, a);
				int b = Math.max(loc.getStart(), loc.getEnd());
				lookup.setInt(2, b);

				Set<String> usedIds = new LinkedHashSet<>();
				try (ResultSet res = lookup.executeQuery()) {
					if (log!=null) log.println("Found "+res.getFetchSize()+" step overlaps.");
					while(res.next()) {
						String id = res.getString(1);
						if (usedIds.contains(id)) {
							logger.info("Encountered duplicate id: "+id);
							if (log!=null) log.println("Encountered duplicate id: "+id);
							continue;
						}
						
						if (prefix !=null && !id.toLowerCase().startsWith(prefix)) {
							throw new IllegalArgumentException("The id '"+id+"' does not start with expected prefix "+prefix+" (case insensitive)!");
						}
						usedIds.add(id);
					}
				}
				return usedIds;
			
	 		} catch (RuntimeException runtime) {
	 			throw runtime;
	 		} catch (Exception ne) {
				logger.warn("Cannot map "+loc, ne);
			}
		}
		
		return Collections.emptySet();

	}

	public static Gene fixId(Gene g) {
		String geneId = g.getGeneId();
		if (geneId.contains(".")) {
			geneId = geneId.substring(0, geneId.indexOf('.'));
			g.setGeneId(geneId);
		}
		return g;
	}

	/**
	 * @return the parentDirectory
	 */
	public Path getParentDirectory() {
		return parentDirectory;
	}

	/**
	 * @param parentDirectory the parentDirectory to set
	 */
	public void setParentDirectory(Path parentDirectory) {
		this.parentDirectory = parentDirectory;
	}

	@Override
	protected Located createIntersectionObject(Object id, int start, int end) {
		// Does nothing in this case because we override stream(...)
		return null;
	}

	@Override
	public <T extends AbstractEntity> T create(Located loc, Variant variant) {
		// Does nothing in this case because we override stream(...)
		return null;
	}
}
