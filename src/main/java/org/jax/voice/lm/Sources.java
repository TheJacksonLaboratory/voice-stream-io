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
import java.util.HashMap;
import java.util.Map;

/**
 * Species-wide dictionary of EQTL source strings (e.g. {@code "GTEx (eGenes)"}).
 * Each distinct source gets a stable u8 id assigned in encounter order.
 * Stored under {@code dataRoot/{hs|mm}/_species/sources.bin}.
 *
 * <p>Capped at 256 distinct values (u8). The vocabulary is tiny in practice
 * (~5 sources), so the cap is safety, not a real constraint.
 *
 * <p>This dictionary is for EQTL sources only; homolog source strings live
 * inline in {@link Homolog} and use a different vocabulary.
 *
 * <p>Wire format (big-endian):
 * <pre>
 *   magic     i32  = SpeciesFiles.MAGIC
 *   version   u8   = 1
 *   type      u8   = SpeciesFiles.TYPE_SOURCES
 *   species   i32
 *   count     i32   ≤ 256
 *   for each id in 0..count-1:
 *     name    UTF
 * </pre>
 */
public final class Sources {

    private final int speciesCode;
    private final String[] byId;
    private final Map<String, Integer> idByName;

    public Sources(int speciesCode, String[] names) {
        if (names.length > 256) {
            throw new IllegalArgumentException("too many sources for u8: " + names.length);
        }
        this.speciesCode = speciesCode;
        this.byId = names;
        this.idByName = new HashMap<>(names.length * 2);
        for (int i = 0; i < names.length; i++) {
            idByName.put(names[i], i);
        }
    }

    public int speciesCode() {
        return speciesCode;
    }

    public int size() {
        return byId.length;
    }

    public String get(int id) {
        return byId[id];
    }

    /** Lookup. Returns -1 if absent. */
    public int idOf(String name) {
        Integer i = idByName.get(name);
        return i == null ? -1 : i;
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(SpeciesFiles.MAGIC);
            out.writeByte(SpeciesFiles.VERSION);
            out.writeByte(SpeciesFiles.TYPE_SOURCES);
            out.writeInt(speciesCode);
            out.writeInt(byId.length);
            for (String s : byId) {
                out.writeUTF(s);
            }
        }
    }

    public static Sources read(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            SpeciesFiles.checkHeader(in, SpeciesFiles.TYPE_SOURCES);
            int speciesCode = in.readInt();
            int n = in.readInt();
            String[] names = new String[n];
            for (int i = 0; i < n; i++) {
                names[i] = in.readUTF();
            }
            return new Sources(speciesCode, names);
        }
    }
}
