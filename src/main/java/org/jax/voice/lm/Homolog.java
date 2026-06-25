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
import java.util.Objects;

/**
 * Cross-species gene-{@code HOMOLOG}-gene (and ORTHOLOG) relation. Each file is
 * the outgoing view from one species: the mouse file maps mouse_gid →
 * (human_gid, source); the human file maps human_gid → (mouse_gid, source).
 *
 * <p>Both files are populated from the same source data
 * ({@code mm/Homolog.csv.gz} and {@code mm/Ortholog.csv.gz}; these are
 * mouse-side files even when describing the human partner). The species pass
 * writes them in pairs so each species gets symmetric outgoing lookups.
 *
 * <p>One file per species under {@code dataRoot/{hs|mm}/_species/homolog.bin}.
 *
 * <p>Source strings ({@code "Homologene"}, {@code "ENSEMBL"}, {@code "BAYLOR"},
 * etc.) are stored inline as UTF since cardinality is small and per-edge cost
 * is in the noise.
 *
 * <p>Wire format (big-endian):
 * <pre>
 *   magic     i32  = SpeciesFiles.MAGIC
 *   version   u8   = 1
 *   type      u8   = SpeciesFiles.TYPE_HOMOLOG
 *   species   i32   "this" species code; the partner is the other species
 *   geneCount i32   number of "from" gids that have at least one edge
 *   for each gid (ascending):
 *     gid       i32
 *     edgeCount i32
 *     for each edge:
 *       otherGid  i32   gid in the partner species
 *       source    UTF   "Homologene" / "ENSEMBL" / "BAYLOR" / ...
 * </pre>
 */
public final class Homolog {

    public record Edge(int otherGid, String source) {
    }

    private final int speciesCode;
    private final Map<Integer, List<Edge>> edgesByGid;

    private static final List<Edge> EMPTY = Collections.emptyList();

    public Homolog(int speciesCode, Map<Integer, List<Edge>> edgesByGid) {
        this.speciesCode = speciesCode;
        this.edgesByGid = edgesByGid;
    }

    public int speciesCode() {
        return speciesCode;
    }

    public int geneCount() {
        return edgesByGid.size();
    }

    /** Homolog/ortholog partners of this gene (cross-species). Empty list when none. */
    public List<Edge> edgesOf(int gid) {
        return edgesByGid.getOrDefault(gid, EMPTY);
    }

    public void write(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(SpeciesFiles.MAGIC);
            out.writeByte(SpeciesFiles.VERSION);
            out.writeByte(SpeciesFiles.TYPE_HOMOLOG);
            out.writeInt(speciesCode);
            out.writeInt(edgesByGid.size());
            int[] gids = edgesByGid.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
            for (int gid : gids) {
                List<Edge> edges = edgesByGid.get(gid);
                out.writeInt(gid);
                out.writeInt(edges.size());
                for (Edge e : edges) {
                    out.writeInt(e.otherGid());
                    out.writeUTF(Objects.requireNonNullElse(e.source(), ""));
                }
            }
        }
    }

    public static Homolog read(Path file) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            SpeciesFiles.checkHeader(in, SpeciesFiles.TYPE_HOMOLOG);
            int speciesCode = in.readInt();
            int geneCount = in.readInt();
            Map<Integer, List<Edge>> map = new HashMap<>(geneCount * 2);
            for (int i = 0; i < geneCount; i++) {
                int gid = in.readInt();
                int edgeCount = in.readInt();
                List<Edge> edges = new ArrayList<>(edgeCount);
                for (int j = 0; j < edgeCount; j++) {
                    int otherGid = in.readInt();
                    String source = in.readUTF();
                    edges.add(new Edge(otherGid, source));
                }
                map.put(gid, edges);
            }
            return new Homolog(speciesCode, map);
        }
    }
}
