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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Shared header constants for the per-species binary files under
 * {@code dataRoot/{hs|mm}/_species/}. Each file begins with the same 6-byte
 * preamble: 4-byte magic, 1-byte version, 1-byte type.
 */
public final class SpeciesFiles {

    private SpeciesFiles() {
    }

    /** Four-byte magic "VLM1" identifying a voice LMDB-companion species file. */
    public static final int MAGIC = 0x564C4D31;

    /** Current file-format version. Bump if any wire layout changes. */
    public static final byte VERSION = 1;

    public static final byte TYPE_GENES = 1;
    public static final byte TYPE_TRANSCRIPTS = 2;
    public static final byte TYPE_PRODUCES = 3;
    public static final byte TYPE_HOMOLOG = 4;
    public static final byte TYPE_TISSUES = 5;
    public static final byte TYPE_SOURCES = 6;

    /** Read and validate the 6-byte header. */
    public static void checkHeader(DataInputStream in, byte expectedType) throws IOException {
        int magic = in.readInt();
        if (magic != MAGIC) {
            throw new IOException(String.format("bad magic: 0x%08x (expected 0x%08x)", magic, MAGIC));
        }
        byte version = in.readByte();
        if (version != VERSION) {
            throw new IOException("unsupported version: " + version + " (expected " + VERSION + ")");
        }
        byte type = in.readByte();
        if (type != expectedType) {
            throw new IOException("type mismatch: got " + type + " expected " + expectedType);
        }
    }
}
