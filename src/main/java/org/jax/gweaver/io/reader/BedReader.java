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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BeanMap;
import org.jax.gweaver.domain.Entity;
import org.jax.gweaver.domain.NamedEntity;
import org.jax.gweaver.domain.Region;
import org.jax.gweaver.domain.Region.Strand;
import org.jax.gweaver.domain.Track;
import org.jax.gweaver.io.connector.TrackConnector;

/**
 * Bed file format @see https://m.ensembl.org/info/website/upload/bed.html
 * @see https://en.wikipedia.org/wiki/BED_(file_format)#:~:text=is%20widely%20used.-,Description,coordinates%20of%20the%20sequences%20considered.
 * @author gerrim
 *
 * @param <N>
 */
public class BedReader<N extends NamedEntity> extends AbstractReader<N> {

	/**
	 * Instantiates a new gene reader.
	 * 
	 * @param species
	 * @param file
	 * @throws IOException
	 */
	public BedReader(String species, File file) throws IOException {
		super(species, file);
		setDelimiter("\\s+");
	}
	
	/**
	 * Instantiates a new gene reader.
	 *
	 * @param species the species
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BedReader(String species, InputStream in) throws IOException {
		super(species, in); // Genes are not that dense maybe one gene / 10 lines
		setDelimiter("\\s+");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
		
		N ret;
		if (line.startsWith("track ")) {
        	String ln = line.substring(5); // Remove track
			Map<String,String> attr = parseQuotedAttributes(ln);
			
			Track track = new Track();
			BeanMap d = new BeanMap(track);
			d.put("name", attr.get("name"));
			d.put("type", attr.get("type"));
			d.put("graphType", attr.get("graphType"));
			d.put("description", attr.get("description"));
			if (attr.containsKey("priority")) d.put("priority", attr.get("priority"));
			if (attr.containsKey("color")) {
				String[] col = attr.get("color").split(",");
				track.setColor(new int[] {Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2])});
			}
			if (attr.containsKey("useScore")) d.put("useScore", attr.get("useScore"));
			if (attr.containsKey("itemRgb")) {
				String val = attr.get("itemRgb");
				track.setItemRgb("on".equals(val));
			}
			ret = (N)track;
			
		} else {
			String[] rec = line.split(getDelimiter());
			Region region = new Region();
			BeanMap d = new BeanMap(region);
			
			d.put("chrom", rec[0]);
			d.put("start", rec[1]);
			d.put("end",   rec[2]);
			if (rec.length>3) d.put("name",  rec[3]);
			if (rec.length>4) d.put("score", rec[4]);
			if (rec.length>5) d.put("strand", Strand.from(rec[5]));
			if (rec.length>6) d.put("thickStart",  rec[6]);
			if (rec.length>7) d.put("thickEnd",    rec[7]);
			if (rec.length>8) {
				String[] col = rec[8].split(",");
				region.setItemRgb(new int[] {Integer.parseInt(col[0]), Integer.parseInt(col[1]), Integer.parseInt(col[2])});
			}
			if (rec.length>9) d.put("blockCount",  rec[9]);
			if (rec.length>10) d.put("blockSizes",  rec[10]);
			if (rec.length>11) d.put("blockStarts", rec[11]);
			
			ret = (N)region;
		}
		
		ret.setSpecies(getSpecies());
		return ret;
	}
	
	@Override
	protected String getAssignmentChar() {
		return "=";
	}

	@Override
	public <U extends Entity> Function<N, Stream<U>> getDefaultConnector() {
		Function<N, Stream<U>> func = new TrackConnector<N, U>();
		return func;
	}

}
