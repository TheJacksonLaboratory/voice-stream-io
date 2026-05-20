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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Packed per-peak record stored as the value in {@link LMSchema#DBI_PEAK_META}.
 *
 * Wire layout (big-endian, total {@value #BYTES} bytes):
 * <pre>
 *   offset 0  u8   featureTypeByte   id assigned at build time, indexes into the
 *                                    per-env featureType dictionary (which lives
 *                                    in schema-version.json)
 *   offset 1  u8   chrByte           per {@link LMSchema#parseChrByte}
 *   offset 2  i32  start             1-based genomic start
 *   offset 6  i32  end               1-based genomic end
 * </pre>
 *
 * Shared between writer (voice-bulk-import) and reader (voice-graph-service)
 * so the byte layout has exactly one definition.
 */
public record PeakMeta(byte featureTypeByte, byte chrByte, int start, int end) {

    /** Size of the packed wire representation, in bytes. */
    public static final int BYTES = 10;

    /** Pack this record to a fresh {@code byte[]} ready to write as an LMDB value. */
    public byte[] pack() {
        ByteBuffer b = ByteBuffer.allocate(BYTES).order(ByteOrder.BIG_ENDIAN);
        b.put(featureTypeByte);
        b.put(chrByte);
        b.putInt(start);
        b.putInt(end);
        return b.array();
    }

    /**
     * Unpack a {@link PeakMeta} from a buffer. The buffer's position is advanced
     * by {@value #BYTES} bytes. The buffer is set to big-endian byte order as a
     * side effect.
     */
    public static PeakMeta unpack(ByteBuffer src) {
        src.order(ByteOrder.BIG_ENDIAN);
        byte ft = src.get();
        byte chr = src.get();
        int start = src.getInt();
        int end = src.getInt();
        return new PeakMeta(ft, chr, start, end);
    }
}
