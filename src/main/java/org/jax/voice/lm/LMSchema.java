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

/**
 * On-disk schema constants and helpers shared between writer (voice-bulk-import)
 * and reader (voice-graph-service).
 *
 * Sub-database (DBI) names are stable strings stored inside the LMDB env; the
 * writer must {@code openDbi(name, MDB_CREATE)} with the same string the
 * reader will look up.
 *
 * Chromosome byte encoding: chromosomes are packed into a single byte inside
 * {@code peak_meta} (and intended for any future per-edge byte). Numbers 1–22
 * encode as their integer value; X/Y/MT use sentinel values; 0 means unknown.
 */
public final class LMSchema {

    private LMSchema() {
    }

    // -----------------------------------------------------------------------
    // Sub-database (DBI) names
    // -----------------------------------------------------------------------

    /** rsId (UTF-8 bytes) → uint32 vid (BE). */
    public static final String DBI_RSID_TO_VID = "rsid_to_vid";

    /** uint32 vid (BE) → rsId (UTF-8 bytes). */
    public static final String DBI_VID_TO_RSID = "vid_to_rsid";

    /**
     * uint32 vid (BE) → 8 bytes: start:i32 BE || end:i32 BE. Genomic position of the
     * variant. Optional — older LMDB envs built before schema 4.0.1 will not have this
     * DBI, so readers must use {@code tryOpenDbi} and tolerate its absence.
     */
    public static final String DBI_VID_TO_POSITION = "vid_to_position";

    /** uint32 peakId (BE) → packed 10-byte {@link PeakMeta} record. */
    public static final String DBI_PEAK_META = "peak_meta";

    /**
     * uint32 vid (BE) → delta+varint compressed sorted peak_id stream.
     * Forward direction: variant → peaks it overlaps.
     */
    public static final String DBI_PEAKOVERLAP_OUT_BY_V = "peakoverlap_out_by_v";

    /**
     * uint32 vid (BE) → varint(count) || count × (gid:i32, tissue_id:u16, slope:f32,
     * lod:f32, bp:i32, source_id:u8) — 19 bytes per edge. Variant → genes it regulates.
     */
    public static final String DBI_EQTL_OUT_BY_V = "eqtl_out_by_v";

    /**
     * uint32 gid (BE) → varint(count) || count × (vid:i32, tissue_id:u16, slope:f32,
     * lod:f32, bp:i32, source_id:u8). Gene ← variants regulating it. CIS ONLY: only
     * populated for variants on the same chromosome as the gene; trans-eQTLs (variant
     * and gene on different chromosomes) are not currently supported.
     */
    public static final String DBI_EQTL_IN_BY_G = "eqtl_in_by_g";

    /**
     * uint32 vid (BE) → delta+varint compressed sorted tid stream.
     * Forward direction: variant → transcripts it affects.
     */
    public static final String DBI_VARIANTEFFECT_OUT_BY_V = "varianteffect_out_by_v";

    /**
     * uint32 tid (BE) → delta+varint compressed sorted vid stream.
     * Reverse direction: transcript ← variants that affect it. CIS ONLY:
     * only transcripts on the same chromosome as the variant are stored here.
     */
    public static final String DBI_VARIANTEFFECT_IN_BY_T = "varianteffect_in_by_t";

    // -----------------------------------------------------------------------
    // Chromosome ↔ byte
    // -----------------------------------------------------------------------

    /** Sentinel value used in the chr byte when chromosome is unknown / blank. */
    public static final int CHR_UNKNOWN = 0;

    /** Sentinel value for chromosome X. */
    public static final int CHR_X = 23;

    /** Sentinel value for chromosome Y. */
    public static final int CHR_Y = 24;

    /** Sentinel value for mitochondrial DNA (M or MT). */
    public static final int CHR_MT = 25;

    /** Parse a chromosome string (e.g. "1", "22", "X", "MT") into the byte encoding. */
    public static int parseChrByte(String chr) {
        if (chr == null || chr.isEmpty()) return CHR_UNKNOWN;
        switch (chr) {
            case "X": return CHR_X;
            case "Y": return CHR_Y;
            case "M":
            case "MT": return CHR_MT;
            default:
                try {
                    return Integer.parseInt(chr);
                } catch (NumberFormatException nfe) {
                    return CHR_UNKNOWN;
                }
        }
    }

    /** Decode the chr byte back to its canonical string form. {@code ""} for unknown. */
    public static String chrFromByte(byte b) {
        int v = b & 0xFF;
        switch (v) {
            case CHR_X: return "X";
            case CHR_Y: return "Y";
            case CHR_MT: return "MT";
            case CHR_UNKNOWN: return "";
            default: return Integer.toString(v);
        }
    }

    // -----------------------------------------------------------------------
    // Species codes (NCBI taxon IDs) and folder names
    // -----------------------------------------------------------------------

    /** NCBI taxon id for Homo sapiens. */
    public static final int SPECIES_HS = 9606;

    /** NCBI taxon id for Mus musculus. */
    public static final int SPECIES_MM = 10090;

    /** Folder name for human data under dataRoot. */
    public static final String FOLDER_HS = "hs";

    /** Folder name for mouse data under dataRoot. */
    public static final String FOLDER_MM = "mm";

    /** Map a species folder name to its NCBI taxon id, or -1 if unrecognised. */
    public static int speciesCodeFor(String folder) {
        if (FOLDER_HS.equalsIgnoreCase(folder)) return SPECIES_HS;
        if (FOLDER_MM.equalsIgnoreCase(folder)) return SPECIES_MM;
        return -1;
    }

    /** Map a NCBI taxon id back to the folder name. */
    public static String folderFor(int speciesCode) {
        if (speciesCode == SPECIES_HS) return FOLDER_HS;
        if (speciesCode == SPECIES_MM) return FOLDER_MM;
        throw new IllegalArgumentException("unknown species code: " + speciesCode);
    }

    // -----------------------------------------------------------------------
    // Per-species file layout (under dataRoot/{hs|mm}/_species/)
    // -----------------------------------------------------------------------

    /** Sub-directory name (under each species folder) holding the species-level files. */
    public static final String SPECIES_DIR = "_species";

    /** File name for the gene index. */
    public static final String FILE_GENES = "genes.bin";

    /** File name for the transcript index. */
    public static final String FILE_TRANSCRIPTS = "transcripts.bin";

    /** File name for the gene-transcript Produces edges. */
    public static final String FILE_PRODUCES = "produces.bin";

    /**
     * File name for the cross-species homolog/ortholog edges. Each species
     * gets its own outgoing view of the same biology: the mouse file maps
     * mouse_gid → human_gid; the human file maps human_gid → mouse_gid.
     */
    public static final String FILE_HOMOLOG = "homolog.bin";

    /**
     * File name for the species-wide tissue dictionary. Populated by the EQTL pass
     * from the union of distinct (tissueName, tissueFileName, tissueGroup, uberon)
     * tuples across all EQTL CSVs in the species.
     */
    public static final String FILE_TISSUES = "tissues.bin";

    /**
     * File name for the species-wide EQTL source dictionary (e.g. "GTEx (eGenes)").
     * Populated by the EQTL pass. Distinct from the homolog "source" strings, which
     * are stored inline in {@link #FILE_HOMOLOG}.
     */
    public static final String FILE_SOURCES = "sources.bin";

    // -----------------------------------------------------------------------
    // File naming
    // -----------------------------------------------------------------------

    /**
     * Sanitise an arbitrary string (e.g. a featureType label) into a filename-safe
     * form. Used by both writer and reader so the bitmap files round-trip.
     */
    public static String safeFileName(String s) {
        return s.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
