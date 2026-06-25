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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The Gene-{@code PRODUCES}-Transcript relation. Stored as gene-grouped tid
 * lists since the V2V_EQTL2TRANS query walks the forward direction
 * ({@code gid → [tid]}). The reverse direction ({@code tid → gid}) lives on
 * {@link TranscriptIndex#gid(int)} so a separate file is not needed.
 *
 * <p>One file per species under {@code dataRoot/{hs|mm}/_species/produces.bin}.
 *
 * <p>Wire format (big-endian):
 * <pre>
 *   magic     i32  = SpeciesFiles.MAGIC
 *   version   u8   = 1
 *   type      u8   = SpeciesFiles.TYPE_PRODUCES
 *   geneCount i32  number of genes that have at least one transcript
 *   for each gene (in gid ascending order):
 *     gid           i32
 *     tidCount      i32
 *     tids          i32 × tidCount   (sorted ascending)
 * </pre>
 */
public final class Produces {

    /** Sorted tid array per gid; absent gids return an empty array. */
    private final Map<Integer, int[]> tidsByGid;

    private static final int[] EMPTY = new int[0];

    public Produces(Map<Integer, int[]> tidsByGid) {
        this.tidsByGid = tidsByGid;
    }

    /** Transcripts produced by a gene. Returns an empty array if the gid has none. */
    public int[] tidsOf(int gid) {
        int[] t = tidsByGid.get(gid);
        return t == null ? EMPTY : t;
    }

    public int geneCount() {
        return tidsByGid.size();
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(SpeciesFiles.MAGIC);
            out.writeByte(SpeciesFiles.VERSION);
            out.writeByte(SpeciesFiles.TYPE_PRODUCES);
            out.writeInt(tidsByGid.size());
            int[] gids = tidsByGid.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
            for (int gid : gids) {
                int[] tids = tidsByGid.get(gid);
                int[] sorted = tids.clone();
                Arrays.sort(sorted);
                out.writeInt(gid);
                out.writeInt(sorted.length);
                for (int tid : sorted) {
                    out.writeInt(tid);
                }
            }
        }
    }

    public static Produces read(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            SpeciesFiles.checkHeader(in, SpeciesFiles.TYPE_PRODUCES);
            int geneCount = in.readInt();
            Map<Integer, int[]> map = new HashMap<>(geneCount * 2);
            for (int i = 0; i < geneCount; i++) {
                int gid = in.readInt();
                int tidCount = in.readInt();
                int[] tids = new int[tidCount];
                for (int j = 0; j < tidCount; j++) tids[j] = in.readInt();
                map.put(gid, tids);
            }
            return new Produces(map);
        }
    }
}
