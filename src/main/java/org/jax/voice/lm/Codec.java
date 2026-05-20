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

import org.roaringbitmap.RoaringBitmap;

/**
 * Wire format for adjacency values: a count-prefixed stream of delta-varint
 * encoded non-negative int ids. Caller must supply a sorted, deduplicated
 * int[] to encode.
 *
 * Dense for sorted IDs because consecutive deltas are typically small and
 * varint encoding compresses small values into 1–2 bytes each.
 *
 * Read paths take a ByteBuffer whose position is advanced by every read.
 * LMDB hands back direct ByteBuffers pointing into the mmap; callers do not
 * need to copy bytes out before decoding.
 *
 * Single definition shared between voice-bulk-import (writer) and
 * voice-graph-service (reader) — see {@link org.jax.voice.lm}.
 */
public final class Codec {

    private Codec() {
    }

    /** Encode a sorted, deduplicated int[] as {@code varint(count) || varint(delta)*}. */
    public static byte[] encodeSortedDeltaVarint(int[] sortedIds) {
        if (sortedIds == null) throw new NullPointerException("sortedIds");
        byte[] buf = new byte[5 + 5 * sortedIds.length];
        int pos = writeVarint(buf, 0, sortedIds.length);
        int prev = 0;
        for (int i = 0; i < sortedIds.length; i++) {
            int id = sortedIds[i];
            if (id < 0) throw new IllegalArgumentException("negative id at " + i + ": " + id);
            int delta = id - prev;
            if (delta < 0) throw new IllegalArgumentException("ids not sorted at " + i);
            if (i > 0 && delta == 0) throw new IllegalArgumentException("duplicate id at " + i);
            pos = writeVarint(buf, pos, delta);
            prev = id;
        }
        byte[] out = new byte[pos];
        System.arraycopy(buf, 0, out, 0, pos);
        return out;
    }

    /** Decode the full stream into a fresh int[]. */
    public static int[] decodeSortedDeltaVarint(ByteBuffer src) {
        int count = readVarint(src);
        int[] out = new int[count];
        int prev = 0;
        for (int i = 0; i < count; i++) {
            prev += readVarint(src);
            out[i] = prev;
        }
        return out;
    }

    /**
     * Single-pass decode + bitmap filter. Walks the stream once and writes only
     * those ids that pass {@code mask.contains(id)} into {@code out}. Returns
     * the number of matches written; caller can {@code Arrays.copyOf(out, n)}
     * if a right-sized array is desired.
     */
    public static int decodeIntersect(ByteBuffer src, RoaringBitmap mask, int[] out) {
        int count = readVarint(src);
        int prev = 0;
        int written = 0;
        for (int i = 0; i < count; i++) {
            prev += readVarint(src);
            if (mask.contains(prev)) {
                out[written++] = prev;
            }
        }
        return written;
    }

    /** Read the count prefix and advance the buffer position past it. */
    public static int readCount(ByteBuffer src) {
        return readVarint(src);
    }

    static int writeVarint(byte[] buf, int pos, int value) {
        if (value < 0) throw new IllegalArgumentException("negative value: " + value);
        int v = value;
        while ((v & ~0x7F) != 0) {
            buf[pos++] = (byte) ((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        buf[pos++] = (byte) v;
        return pos;
    }

    /** Read an unsigned varint, advancing the buffer position. Public so adjacency
     *  decoders in other modules (e.g. LMSearchService) can reuse the same wire format. */
    public static int readVarint(ByteBuffer src) {
        int v = 0;
        int shift = 0;
        while (true) {
            byte b = src.get();
            v |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return v;
            shift += 7;
            if (shift > 28) throw new IllegalStateException("varint exceeds 5 bytes");
        }
    }
}
