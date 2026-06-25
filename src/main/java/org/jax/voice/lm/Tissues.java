/*-
 * Copyright (c) 2026 Jackson Laboratory
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License  Version 2.0, January 2004
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 */
package org.jax.voice.lm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Species-wide dictionary of EQTL tissue tuples. Each distinct
 * {@code (tissueName, tissueFileName, tissueGroup, uberon)} tuple gets a stable
 * uint16 id assigned in encounter order. Stored under
 * {@code dataRoot/{hs|mm}/_species/tissues.bin}.
 *
 * <p>Used to dictionary-encode the {@code tissue_id} slot of every EQTL edge so
 * the 4 tissue strings are not duplicated millions of times in adjacency
 * values. The {@code WHERE tissue CONTAINS …} substring filter runs over the
 * tuple list at query time (small N, microseconds).
 *
 * <p>Wire format (big-endian):
 * <pre>
 *   magic     i32  = SpeciesFiles.MAGIC
 *   version   u8   = 1
 *   type      u8   = SpeciesFiles.TYPE_TISSUES
 *   species   i32
 *   count     i32  number of distinct tuples
 *   for each id in 0..count-1:
 *     tissueName     UTF
 *     tissueFileName UTF
 *     tissueGroup    UTF
 *     uberon         UTF
 * </pre>
 *
 * <p>Strings are normalised: CSV null/empty become "" before dictionary lookup
 * (see {@link #normalize(String)}).
 */
public final class Tissues {

    /** A distinct tissue tuple. All four fields are normalised to "" if absent. */
    public record Entry(String tissueName, String tissueFileName, String tissueGroup, String uberon) {
    }

    private final int speciesCode;
    private final Entry[] byId;
    private final Map<Entry, Integer> idByEntry;

    public Tissues(int speciesCode, Entry[] entries) {
        this.speciesCode = speciesCode;
        this.byId = entries;
        this.idByEntry = new HashMap<>(entries.length * 2);
        for (int i = 0; i < entries.length; i++) {
            idByEntry.put(entries[i], i);
        }
    }

    public int speciesCode() {
        return speciesCode;
    }

    public int size() {
        return byId.length;
    }

    public Entry get(int id) {
        return byId[id];
    }

    /** Lookup. Returns -1 if absent. */
    public int idOf(Entry e) {
        Integer i = idByEntry.get(e);
        return i == null ? -1 : i;
    }

    /**
     * Normalise a CSV cell value into the form used as a tuple field. Empty or
     * literal "null" become "".
     */
    public static String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) return "";
        return t;
    }

    /**
     * Substring search across all four fields, case-insensitive, OR semantics
     * (matches CypherBuilder's tissue WHERE clauses). Returns ids in ascending
     * order; empty array if no match.
     */
    public int[] idsContaining(String needle) {
        if (needle == null || needle.isEmpty()) return EMPTY;
        String lower = needle.toLowerCase();
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < byId.length; i++) {
            Entry e = byId[i];
            if (containsIC(e.tissueName(), lower)
                    || containsIC(e.tissueFileName(), lower)
                    || containsIC(e.tissueGroup(), lower)
                    || containsIC(e.uberon(), lower)) {
                hits.add(i);
            }
        }
        int[] out = new int[hits.size()];
        for (int i = 0; i < out.length; i++) out[i] = hits.get(i);
        return out;
    }

    private static boolean containsIC(String haystack, String lowerNeedle) {
        if (haystack == null || haystack.isEmpty()) return false;
        return haystack.toLowerCase().contains(lowerNeedle);
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(SpeciesFiles.MAGIC);
            out.writeByte(SpeciesFiles.VERSION);
            out.writeByte(SpeciesFiles.TYPE_TISSUES);
            out.writeInt(speciesCode);
            out.writeInt(byId.length);
            for (Entry e : byId) {
                out.writeUTF(e.tissueName());
                out.writeUTF(e.tissueFileName());
                out.writeUTF(e.tissueGroup());
                out.writeUTF(e.uberon());
            }
        }
    }

    public static Tissues read(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            SpeciesFiles.checkHeader(in, SpeciesFiles.TYPE_TISSUES);
            int speciesCode = in.readInt();
            int n = in.readInt();
            Entry[] entries = new Entry[n];
            for (int i = 0; i < n; i++) {
                String name = in.readUTF();
                String file2 = in.readUTF();
                String group = in.readUTF();
                String uberon = in.readUTF();
                entries[i] = new Entry(name, file2, group, uberon);
            }
            return new Tissues(speciesCode, entries);
        }
    }

    private static final int[] EMPTY = new int[0];
}
