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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

public class CodecTest {

    @Test
    public void roundTripEmpty() {
        int[] in = new int[0];
        byte[] encoded = Codec.encodeSortedDeltaVarint(in);
        int[] decoded = Codec.decodeSortedDeltaVarint(ByteBuffer.wrap(encoded));
        assertArrayEquals(in, decoded);
        assertEquals("empty list encodes to single zero byte", 1, encoded.length);
    }

    @Test
    public void roundTripSingle() {
        int[] in = {42};
        int[] decoded = Codec.decodeSortedDeltaVarint(ByteBuffer.wrap(
                Codec.encodeSortedDeltaVarint(in)));
        assertArrayEquals(in, decoded);
    }

    @Test
    public void roundTripSmallSorted() {
        int[] in = {1, 2, 3, 5, 8, 13, 21, 34};
        int[] decoded = Codec.decodeSortedDeltaVarint(ByteBuffer.wrap(
                Codec.encodeSortedDeltaVarint(in)));
        assertArrayEquals(in, decoded);
    }

    @Test
    public void roundTripLargeGapsAndBoundary() {
        int[] in = {0, 127, 128, 16383, 16384, 2_097_151, 2_097_152, Integer.MAX_VALUE - 1};
        int[] decoded = Codec.decodeSortedDeltaVarint(ByteBuffer.wrap(
                Codec.encodeSortedDeltaVarint(in)));
        assertArrayEquals(in, decoded);
    }

    @Test
    public void roundTripRandomManySorted() {
        Random rnd = new Random(7);
        int[] raw = new int[10_000];
        for (int i = 0; i < raw.length; i++) raw[i] = rnd.nextInt(20_000_000);
        Arrays.sort(raw);
        int w = 0;
        for (int i = 0; i < raw.length; i++) {
            if (i == 0 || raw[i] != raw[i - 1]) raw[w++] = raw[i];
        }
        int[] in = Arrays.copyOf(raw, w);

        byte[] encoded = Codec.encodeSortedDeltaVarint(in);
        int[] decoded = Codec.decodeSortedDeltaVarint(ByteBuffer.wrap(encoded));
        assertArrayEquals(in, decoded);

        double bytesPerId = (double) encoded.length / in.length;
        assertTrue("expected <5 bytes/id, got " + bytesPerId, bytesPerId < 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectUnsorted() {
        Codec.encodeSortedDeltaVarint(new int[] {5, 3, 7});
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectDuplicates() {
        Codec.encodeSortedDeltaVarint(new int[] {1, 1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectNegative() {
        Codec.encodeSortedDeltaVarint(new int[] {-1, 0, 1});
    }

    @Test
    public void decodeIntersectFiltersByBitmap() {
        int[] in = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        byte[] encoded = Codec.encodeSortedDeltaVarint(in);

        RoaringBitmap mask = new RoaringBitmap();
        mask.add(20);
        mask.add(50);
        mask.add(80);
        mask.add(999);

        int[] scratch = new int[in.length];
        int n = Codec.decodeIntersect(ByteBuffer.wrap(encoded), mask, scratch);
        assertArrayEquals(new int[] {20, 50, 80}, Arrays.copyOf(scratch, n));
    }

    @Test
    public void decodeIntersectEmptyMask() {
        int[] in = {10, 20, 30};
        byte[] encoded = Codec.encodeSortedDeltaVarint(in);
        int[] scratch = new int[3];
        int n = Codec.decodeIntersect(ByteBuffer.wrap(encoded), new RoaringBitmap(), scratch);
        assertEquals(0, n);
    }
}
