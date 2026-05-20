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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Species-wide gene catalog. One file per species under
 * {@code dataRoot/{hs|mm}/_species/genes.bin}.
 *
 * <p>Genes are addressed by a dense uint32 {@code gid} assigned during the
 * species pass; the assignment is stable within a build and used everywhere
 * downstream (eqtl edges in the per-chr envs, homolog edges, etc.). gid 0 is
 * the first gene added; gids are contiguous.
 *
 * <p>Wire format ({@link SpeciesFiles#MAGIC} prefix, big-endian throughout):
 * <pre>
 *   magic     i32  = SpeciesFiles.MAGIC
 *   version   u8   = 1
 *   type      u8   = SpeciesFiles.TYPE_GENES
 *   species   i32  NCBI taxon id (9606 or 10090)
 *   count     i32  number of genes
 *   for each gene (in gid order, 0..count-1):
 *     geneId    UTF  ENSG... / ENSMUSG...
 *     geneName  UTF
 *     chrByte   u8   per LMSchema chr encoding
 *     start     i32
 *     end       i32
 *     strand    u8   ASCII '+' / '-' / '.'
 * </pre>
 */
public final class GeneIndex {

    private final int speciesCode;
    private final String[] geneIdByGid;
    private final String[] geneNameByGid;
    private final byte[] chrByGid;
    private final int[] startByGid;
    private final int[] endByGid;
    private final byte[] strandByGid;

    // Built on construction
    private final Map<String, Integer> gidByGeneId;
    private final Map<String, List<Integer>> gidsByGeneName;

    public GeneIndex(int speciesCode, String[] geneIds, String[] geneNames,
                     byte[] chrs, int[] starts, int[] ends, byte[] strands) {
        int n = geneIds.length;
        if (geneNames.length != n || chrs.length != n || starts.length != n
                || ends.length != n || strands.length != n) {
            throw new IllegalArgumentException("inconsistent array lengths");
        }
        this.speciesCode = speciesCode;
        this.geneIdByGid = geneIds;
        this.geneNameByGid = geneNames;
        this.chrByGid = chrs;
        this.startByGid = starts;
        this.endByGid = ends;
        this.strandByGid = strands;

        this.gidByGeneId = new HashMap<>(n * 2);
        this.gidsByGeneName = new HashMap<>(n * 2);
        for (int gid = 0; gid < n; gid++) {
            gidByGeneId.put(geneIds[gid], gid);
            gidsByGeneName.computeIfAbsent(geneNames[gid], k -> new ArrayList<>(1)).add(gid);
        }
    }

    public int speciesCode() {
        return speciesCode;
    }

    public int size() {
        return geneIdByGid.length;
    }

    public String geneId(int gid) {
        return geneIdByGid[gid];
    }

    public String geneName(int gid) {
        return geneNameByGid[gid];
    }

    public byte chrByte(int gid) {
        return chrByGid[gid];
    }

    public String chr(int gid) {
        return LMSchema.chrFromByte(chrByGid[gid]);
    }

    public int start(int gid) {
        return startByGid[gid];
    }

    public int end(int gid) {
        return endByGid[gid];
    }

    public byte strandByte(int gid) {
        return strandByGid[gid];
    }

    /** Resolve an ENSG/ENSMUSG identifier to its gid, or -1 if absent. */
    public int gidOf(String geneId) {
        Integer g = gidByGeneId.get(geneId);
        return g == null ? -1 : g;
    }

    /** All gids whose geneName matches (case-sensitive). Empty list when none. */
    public List<Integer> gidsOfGeneName(String geneName) {
        return gidsByGeneName.getOrDefault(geneName, Collections.emptyList());
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(SpeciesFiles.MAGIC);
            out.writeByte(SpeciesFiles.VERSION);
            out.writeByte(SpeciesFiles.TYPE_GENES);
            out.writeInt(speciesCode);
            out.writeInt(size());
            for (int gid = 0; gid < size(); gid++) {
                out.writeUTF(geneIdByGid[gid]);
                out.writeUTF(geneNameByGid[gid]);
                out.writeByte(chrByGid[gid]);
                out.writeInt(startByGid[gid]);
                out.writeInt(endByGid[gid]);
                out.writeByte(strandByGid[gid]);
            }
        }
    }

    public static GeneIndex read(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            SpeciesFiles.checkHeader(in, SpeciesFiles.TYPE_GENES);
            int speciesCode = in.readInt();
            int n = in.readInt();
            String[] geneIds = new String[n];
            String[] geneNames = new String[n];
            byte[] chrs = new byte[n];
            int[] starts = new int[n];
            int[] ends = new int[n];
            byte[] strands = new byte[n];
            for (int gid = 0; gid < n; gid++) {
                geneIds[gid] = in.readUTF();
                geneNames[gid] = in.readUTF();
                chrs[gid] = in.readByte();
                starts[gid] = in.readInt();
                ends[gid] = in.readInt();
                strands[gid] = in.readByte();
            }
            return new GeneIndex(speciesCode, geneIds, geneNames, chrs, starts, ends, strands);
        }
    }
}
