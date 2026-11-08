package edu.assignment44.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Naive, Knuth-Morris-Pratt, and Rabin-Karp substring search implementations. */
public final class StringMatchers {
    private static final long RADIX = 256L;
    private static final long MODULUS = 1_000_000_007L;

    private StringMatchers() {
    }

    /** Mutable optional counter. Pass {@code null} to avoid instrumentation overhead. */
    public static final class SearchStats {
        private long characterComparisons;

        public long characterComparisons() {
            return characterComparisons;
        }

        private void compared() {
            characterComparisons++;
        }
    }

    /** Returns every match start. The empty pattern matches at each boundary, 0 through n. */
    public static List<Integer> naive(String text, String pattern) {
        return naive(text, pattern, null);
    }

    public static List<Integer> naive(String text, String pattern, SearchStats stats) {
        requireInputs(text, pattern);
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                matches.add(i);
            }
            return matches;
        }
        if (m > n) {
            return matches;
        }
        for (int start = 0; start <= n - m; start++) {
            int j = 0;
            while (j < m) {
                compared(stats);
                if (text.charAt(start + j) != pattern.charAt(j)) {
                    break;
                }
                j++;
            }
            if (j == m) {
                matches.add(start);
            }
        }
        return matches;
    }

    /** Returns every match start using a prefix/LPS table. */
    public static List<Integer> kmp(String text, String pattern) {
        return kmp(text, pattern, null);
    }

    public static List<Integer> kmp(String text, String pattern, SearchStats stats) {
        requireInputs(text, pattern);
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                matches.add(i);
            }
            return matches;
        }
        if (m > n) {
            return matches;
        }

        int[] lps = prefixTable(pattern);
        int matched = 0;
        for (int i = 0; i < n; i++) {
            boolean textCharacterConsumed = false;
            while (matched > 0) {
                compared(stats);
                if (text.charAt(i) == pattern.charAt(matched)) {
                    matched++;
                    textCharacterConsumed = true;
                    break;
                }
                matched = lps[matched - 1];
            }
            if (!textCharacterConsumed) {
                compared(stats);
                if (text.charAt(i) == pattern.charAt(matched)) {
                    matched++;
                }
            }
            if (matched == m) {
                matches.add(i - m + 1);
                matched = lps[matched - 1];
            }
        }
        return matches;
    }

    private static int[] prefixTable(String pattern) {
        int[] lps = new int[pattern.length()];
        int prefixLength = 0;
        for (int i = 1; i < pattern.length();) {
            if (pattern.charAt(i) == pattern.charAt(prefixLength)) {
                lps[i++] = ++prefixLength;
            } else if (prefixLength > 0) {
                prefixLength = lps[prefixLength - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    /** Returns every match start using a rolling hash and direct verification on hash hits. */
    public static List<Integer> rabinKarp(String text, String pattern) {
        return rabinKarp(text, pattern, null);
    }

    public static List<Integer> rabinKarp(String text, String pattern, SearchStats stats) {
        requireInputs(text, pattern);
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                matches.add(i);
            }
            return matches;
        }
        if (m > n) {
            return matches;
        }

        long highOrderPower = 1L;
        for (int i = 1; i < m; i++) {
            highOrderPower = (highOrderPower * RADIX) % MODULUS;
        }
        long patternHash = 0L;
        long windowHash = 0L;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * RADIX + pattern.charAt(i)) % MODULUS;
            windowHash = (windowHash * RADIX + text.charAt(i)) % MODULUS;
        }

        for (int start = 0; start <= n - m; start++) {
            if (patternHash == windowHash && equalsAt(text, pattern, start, stats)) {
                matches.add(start);
            }
            if (start < n - m) {
                long withoutLeading = (windowHash - text.charAt(start) * highOrderPower) % MODULUS;
                if (withoutLeading < 0) {
                    withoutLeading += MODULUS;
                }
                windowHash = (withoutLeading * RADIX + text.charAt(start + m)) % MODULUS;
            }
        }
        return matches;
    }

    private static boolean equalsAt(String text, String pattern, int start, SearchStats stats) {
        for (int j = 0; j < pattern.length(); j++) {
            compared(stats);
            if (text.charAt(start + j) != pattern.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    private static void compared(SearchStats stats) {
        if (stats != null) {
            stats.compared();
        }
    }

    private static void requireInputs(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
    }
}
