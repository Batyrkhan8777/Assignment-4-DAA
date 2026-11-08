package edu.assignment44.suffix;

import java.util.Arrays;
import java.util.Objects;

/** Prefix-doubling helper used to supply suffix arrays to Kasai's algorithm. */
public final class SuffixArrays {
    private SuffixArrays() {
    }

    /** Builds a suffix array in O(n log^2 n) time with comparison sorting. */
    public static int[] build(String text) {
        Objects.requireNonNull(text, "text");
        int n = text.length();
        Integer[] order = new Integer[n];
        int[] rank = new int[n];
        int[] nextRank = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
            rank[i] = text.charAt(i);
        }
        for (int width = 1; width < n; width *= 2) {
            final int offset = width;
            final int[] currentRank = rank;
            Arrays.sort(order, (left, right) -> {
                int byFirstHalf = Integer.compare(currentRank[left], currentRank[right]);
                if (byFirstHalf != 0) {
                    return byFirstHalf;
                }
                int leftTail = left + offset < n ? currentRank[left + offset] : -1;
                int rightTail = right + offset < n ? currentRank[right + offset] : -1;
                return Integer.compare(leftTail, rightTail);
            });

            nextRank[order[0]] = 0;
            for (int i = 1; i < n; i++) {
                int previous = order[i - 1];
                int current = order[i];
                boolean different = rank[previous] != rank[current]
                        || rankAt(rank, previous + offset, n) != rankAt(rank, current + offset, n);
                nextRank[current] = nextRank[previous] + (different ? 1 : 0);
            }
            int[] swap = rank;
            rank = nextRank;
            nextRank = swap;
            if (rank[order[n - 1]] == n - 1 || width > n / 2) {
                break;
            }
        }
        int[] suffixArray = new int[n];
        for (int i = 0; i < n; i++) {
            suffixArray[i] = order[i];
        }
        return suffixArray;
    }

    private static int rankAt(int[] ranks, int index, int length) {
        return index < length ? ranks[index] : -1;
    }
}
