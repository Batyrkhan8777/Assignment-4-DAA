package edu.assignment44.suffix;

import java.util.Objects;

/** Kasai's linear-time construction of the LCP array from a suffix array. */
public final class KasaiLcp {
    private KasaiLcp() {
    }

    /** Optional counter for character comparisons in the Kasai scan. */
    public static final class Stats {
        private long characterComparisons;

        public long characterComparisons() {
            return characterComparisons;
        }
    }

    /**
     * Builds lcp where lcp[r] is the common-prefix length of suffixes sa[r-1] and sa[r].
     * The first entry is zero. {@code suffixArray} must be a permutation of all suffix starts.
     */
    public static int[] build(String text, int[] suffixArray) {
        return build(text, suffixArray, null);
    }

    public static int[] build(String text, int[] suffixArray, Stats stats) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(suffixArray, "suffixArray");
        int n = text.length();
        if (suffixArray.length != n) {
            throw new IllegalArgumentException("Suffix array length must equal text length");
        }
        int[] lcp = new int[n];
        int[] rank = new int[n];
        for (int r = 0; r < n; r++) {
            rank[suffixArray[r]] = r;
        }

        int common = 0;
        for (int start = 0; start < n; start++) {
            int r = rank[start];
            if (r == 0) {
                common = 0;
                continue;
            }
            int previousStart = suffixArray[r - 1];
            while (start + common < n && previousStart + common < n) {
                if (stats != null) {
                    stats.characterComparisons++;
                }
                if (text.charAt(start + common) != text.charAt(previousStart + common)) {
                    break;
                }
                common++;
            }
            lcp[r] = common;
            if (common > 0) {
                common--;
            }
        }
        return lcp;
    }
}
