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
package org.jax.gweaver.io.reader;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanMap;
import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.GeneticEntity;
import org.jax.gweaver.domain.Variant;
import org.jax.gweaver.domain.VariantEffect;
import org.jax.gweaver.io.connector.VariantConnector;

// TODO: Auto-generated Javadoc
/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 *
 */
class VariantReader<N extends GeneticEntity> extends LineIteratorReader<N>{
	
	/** The Constant VARIANTS. */
	// TODO Are all invariant types a Variant or are some ignored?
	public static final Collection<String> VARIANTS = Arrays.asList("snv", "deletion", "insertion", "indel", "substitution");


	/**
	 * Create the reader by setting its data
	 * 
	 * @param reader
	 * @throws ReaderException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public VariantReader<N> init(ReaderRequest request) throws ReaderException {
		super.setup(request);
		setDelimiter("\t+"); // Must be a tab only
		return this;
	}

	/**
	 * Creates the.
	 *
	 * @param line the line
	 * @return the n
	 * @throws ReaderException the reader exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
        
		String[] rec = line.split(getDelimiter());
		Variant bean = new Variant();
         
        try {
			BeanMap d = new BeanMap(bean);
			populate(d, rec);
			
	        Map<String,String> attributes = parseAttributes(rec[8]);
	        d.put("id", attributes.get("ID"));
	        d.put("rsId", attributes.get("Dbxref").toString().split(":")[1]);
	        d.put("dbxRef", attributes.get("Dbxref"));
	        d.put("altAllele", attributes.get("Variant_seq"));
	        d.put("refAllele", attributes.get("Reference_seq"));
	        if (request.isIncludeAll() ) { // Variant effects are used for connections. If we are not 
	        						   // writing connections, then we do not need to parse them.
	        	d.put("variantEffect", createVariantEffects(bean, attributes.get("Variant_effect")));
	        }
	        
        } catch (IllegalArgumentException ne) {
        	throw new ReaderException("The line "+line+" of bean type "+bean.getClass().getSimpleName()+" cannot be parsed ", ne);
        }
        
        return (N)bean;
	}

	/**
	 * Creates the variant effects.
	 *
	 * @param bean the bean
	 * @param varEff the var eff
	 * @return the sets the
	 */
	private Set<VariantEffect> createVariantEffects(Variant bean, String varEff) {
		if (varEff == null) return null;
		if (varEff.isEmpty()) return null;

		Set<VariantEffect> effects = new HashSet<>();
		for (String line : varEff.split(",")) {
			String[] vals = line.split(" ");
			// @see https://github.com/The-Sequence-Ontology/Specifications/blob/master/gvf.md
			// sequence_variant index feature_type feature_ID 
			VariantEffect effect = new VariantEffect();
			effect.setSequenceVariant(vals[0]);
			effect.setIndex(Integer.parseInt(vals[1]));
			effect.setFeatureType(vals[2]);
			effect.setFeatureId(vals[3]);
			effects.add(effect);
		}
		return effects;
	}

	/**
	 * Gets the assignment char.
	 *
	 * @return the assignment char
	 */
	protected String getAssignmentChar() {
		return "=";
	}

	@Override
	public <U extends Entity> Function<N, Stream<U>> getDefaultConnector() {
		return new VariantConnector<>();
	}

}