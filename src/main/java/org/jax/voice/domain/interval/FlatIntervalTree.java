package org.jax.voice.domain.interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * A Flat Interval Tree, built on primitive arrays rather than node objects.
 * Optimized for hundreds of millions of intervals per shard.
 *
 * Uses intervals [start, end) and overlap rule.
 *
 * @param <T> must implement IInterval (start(), end())
 */
public final class FlatIntervalTree implements Serializable {

    private static final long serialVersionUID = -8543160775136985945L;

    // Primitive arrays for cache locality
    private final int[] starts;     // sorted
    private final int[] ends;       // aligned with starts[]
    private final int[] maxEnd;     // prefix max of ends[]

    // Optional: payload handle for the caller
    private final Interval[] payload;

    /**
     * Constructs a shard from a list of intervals.
     * The list may contain hundreds of millions of intervals.
     */
    public FlatIntervalTree(List<Interval> intervals) {
        final int n = intervals.size();
        this.starts = new int[n];
        this.ends   = new int[n];
        this.maxEnd = new int[n];
        this.payload = (Interval[]) new Interval[n];

        // Load arrays
        for (int i = 0; i < n; i++) {
        	Interval c = intervals.get(i);
            starts[i] = c.start();
            ends[i]   = c.end();
            payload[i] = c;
        }

        // Sort by start using parallel sort for speed
        parallelSortByStart();

        // Build prefix maxEnd array
        int runningMax = 0;
        for (int i = 0; i < n; i++) {
            runningMax = Math.max(runningMax, ends[i]);
            maxEnd[i] = runningMax;
        }
    }

    /**
     * Returns a list of all intervals overlapping [qStart, qEnd] (inclusive).
     *
     * This matches the unit tests in FlatIntervalTreeTest:
     * overlap exists if:
     *   start <= qEnd && end >= qStart
     */
    public List<Interval> query(int qStart, int qEnd) {
        final List<Interval> out = new ArrayList<>();

        // Sanity: allow reversed queries by normalizing.
        if (qEnd < qStart) {
            int t = qStart;
            qStart = qEnd;
            qEnd = t;
        }

        // Find first index where starts[i] > qEnd, then step back.
        int idx = upperBound(starts, qEnd);
        idx--;

        // Scan backwards while intervals may still reach qStart.
        while (idx >= 0 && maxEnd[idx] >= qStart) {
            if (starts[idx] <= qEnd && ends[idx] >= qStart) {
                out.add(payload[idx]);
            }
            idx--;
        }

        return out;
    }

    /**
     * Parallel sort of intervals by start using ForkJoin and a primitive index array.
     */
    private void parallelSortByStart() {
        final int n = starts.length;

        // Index array [0..n)
        int[] idx = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i;

        // Parallel sort indices by starts[] key
        ForkJoinPool.commonPool().invoke(new SortTask(idx, starts, 0, n));

        // Apply permutation to all arrays
        int[] newStarts = new int[n];
        int[] newEnds   = new int[n];
        Interval[] newPayload = new Interval[n];

        for (int i = 0; i < n; i++) {
            int k = idx[i];
            newStarts[i] = starts[k];
            newEnds[i]   = ends[k];
            newPayload[i] = payload[k];
        }

        System.arraycopy(newStarts, 0, starts, 0, n);
        System.arraycopy(newEnds,   0, ends,   0, n);
        System.arraycopy(newPayload, 0, payload, 0, n);
    }

    /* ============================================================
     * Primitive parallel mergesort task on int[] indices
     * ============================================================ */
    private static class SortTask extends java.util.concurrent.RecursiveAction {
        private static final long serialVersionUID = 2569555577632516359L;
		private final int[] idx;
        private final int[] key;  // starts[]
        private final int lo, hi;
        private static final int THRESHOLD = 50_000;

        SortTask(int[] idx, int[] key, int lo, int hi) {
            this.idx = idx;
            this.key = key;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected void compute() {
            int size = hi - lo;

            if (size <= THRESHOLD) {
                quickSortByKey(idx, key, lo, hi - 1);
                return;
            }

            int mid = (lo + hi) >>> 1;
            SortTask left = new SortTask(idx, key, lo, mid);
            SortTask right = new SortTask(idx, key, mid, hi);

            invokeAll(left, right);

            merge(lo, mid, hi);
        }

        private void merge(int lo, int mid, int hi) {
            int[] temp = Arrays.copyOfRange(idx, lo, hi);

            int i = 0, j = mid - lo, k = lo;
            int leftLen = mid - lo;
            int rightLen = hi - mid;

            while (i < leftLen && j < leftLen + rightLen) {
                if (key[temp[i]] <= key[temp[j]]) {
                    idx[k++] = temp[i++];
                } else {
                    idx[k++] = temp[j++];
                }
            }

            while (i < leftLen)
                idx[k++] = temp[i++];
            while (j < leftLen + rightLen)
                idx[k++] = temp[j++];
        }
    }

    /**
     * In-place quicksort of idx[lo..hi] (inclusive) by key[idx[i]].
     */
    private static void quickSortByKey(int[] idx, int[] key, int lo, int hi) {
        int i = lo;
        int j = hi;
        int pivot = key[idx[(lo + hi) >>> 1]];

        while (i <= j) {
            while (key[idx[i]] < pivot) i++;
            while (key[idx[j]] > pivot) j--;
            if (i <= j) {
                int tmp = idx[i];
                idx[i] = idx[j];
                idx[j] = tmp;
                i++;
                j--;
            }
        }

        if (lo < j) quickSortByKey(idx, key, lo, j);
        if (i < hi) quickSortByKey(idx, key, i, hi);
    }

    /**
     * Returns the first index where array[i] > value.
     * Equivalent to upper_bound in C++.
     */
    private static int upperBound(int[] arr, int value) {
        int lo = 0, hi = arr.length;

        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (arr[mid] <= value)
                lo = mid + 1;
            else
                hi = mid;
        }
        return lo;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ends);
		result = prime * result + Arrays.hashCode(maxEnd);
		result = prime * result + Arrays.hashCode(payload);
		result = prime * result + Arrays.hashCode(starts);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FlatIntervalTree))
			return false;
		FlatIntervalTree other = (FlatIntervalTree) obj;
		return Arrays.equals(ends, other.ends) && Arrays.equals(maxEnd, other.maxEnd)
				&& Arrays.equals(payload, other.payload) && Arrays.equals(starts, other.starts);
	}

	public int size() {
		return payload.length;
	}
}