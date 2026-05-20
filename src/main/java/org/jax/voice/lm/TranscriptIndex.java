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
 * Species-wide transcript catalog. One file per species under
 * {@code dataRoot/{hs|mm}/_species/transcripts.bin}.
 *
 * <p>Each transcript carries its parent gid so the "PRODUCES⁻¹" direction
 * (transcript → gene) is a single array lookup; no separate file required.
 *
 * <p>Wire format ({@link SpeciesFiles#MAGIC} prefix, big-endian throughout):
 * <pre>
 *   magic     i32  = SpeciesFiles.MAGIC
 *   version   u8   = 1
 *   type      u8   = SpeciesFiles.TYPE_TRANSCRIPTS
 *   species   i32
 *   count     i32
 *   for each transcript (in tid order):
 *     transcriptId   UTF  ENST... / ENSMUST...
 *     gid            i32  parent gene's gid in this species's GeneIndex
 *     chrByte        u8
 *     start          i32
 *     end            i32
 * </pre>
 */
public final class TranscriptIndex {

    private final int speciesCode;
    private final String[] transcriptIdByTid;
    private final int[] gidByTid;
    private final byte[] chrByTid;
    private final int[] startByTid;
    private final int[] endByTid;
    private final Map<String, Integer> tidByTranscriptId;

    public TranscriptIndex(int speciesCode, String[] transcriptIds, int[] gids,
                           byte[] chrs, int[] starts, int[] ends) {
        int n = transcriptIds.length;
        if (gids.length != n || chrs.length != n || starts.length != n || ends.length != n) {
            throw new IllegalArgumentException("inconsistent array lengths");
        }
        this.speciesCode = speciesCode;
        this.transcriptIdByTid = transcriptIds;
        this.gidByTid = gids;
        this.chrByTid = chrs;
        this.startByTid = starts;
        this.endByTid = ends;
        this.tidByTranscriptId = new HashMap<>(n * 2);
        for (int tid = 0; tid < n; tid++) {
            tidByTranscriptId.put(transcriptIds[tid], tid);
        }
    }

    public int speciesCode() {
        return speciesCode;
    }

    public int size() {
        return transcriptIdByTid.length;
    }

    public String transcriptId(int tid) {
        return transcriptIdByTid[tid];
    }

    /** The gid of the gene that produces this transcript. */
    public int gid(int tid) {
        return gidByTid[tid];
    }

    public byte chrByte(int tid) {
        return chrByTid[tid];
    }

    public int start(int tid) {
        return startByTid[tid];
    }

    public int end(int tid) {
        return endByTid[tid];
    }

    /** Resolve an ENST/ENSMUST identifier to its tid, or -1 if absent. */
    public int tidOf(String transcriptId) {
        Integer t = tidByTranscriptId.get(transcriptId);
        return t == null ? -1 : t;
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(SpeciesFiles.MAGIC);
            out.writeByte(SpeciesFiles.VERSION);
            out.writeByte(SpeciesFiles.TYPE_TRANSCRIPTS);
            out.writeInt(speciesCode);
            out.writeInt(size());
            for (int tid = 0; tid < size(); tid++) {
                out.writeUTF(transcriptIdByTid[tid]);
                out.writeInt(gidByTid[tid]);
                out.writeByte(chrByTid[tid]);
                out.writeInt(startByTid[tid]);
                out.writeInt(endByTid[tid]);
            }
        }
    }

    public static TranscriptIndex read(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            SpeciesFiles.checkHeader(in, SpeciesFiles.TYPE_TRANSCRIPTS);
            int speciesCode = in.readInt();
            int n = in.readInt();
            String[] transcriptIds = new String[n];
            int[] gids = new int[n];
            byte[] chrs = new byte[n];
            int[] starts = new int[n];
            int[] ends = new int[n];
            for (int tid = 0; tid < n; tid++) {
                transcriptIds[tid] = in.readUTF();
                gids[tid] = in.readInt();
                chrs[tid] = in.readByte();
                starts[tid] = in.readInt();
                ends[tid] = in.readInt();
            }
            return new TranscriptIndex(speciesCode, transcriptIds, gids, chrs, starts, ends);
        }
    }
}
