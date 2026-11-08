package edu.assignment44;

import edu.assignment44.dp.DynamicProgramming;
import edu.assignment44.matching.StringMatchers;
import edu.assignment44.suffix.KasaiLcp;
import edu.assignment44.suffix.SuffixArrays;

import java.util.Arrays;
import java.util.List;

/** Dependency-free correctness checks for the required algorithms and edge cases. */
public final class AlgorithmTests {
    private static int checks;

    private AlgorithmTests() {
    }

    public static void main(String[] args) {
        testLcs();
        testEditDistance();
        testStringMatchers();
        testKasai();
        System.out.println("All " + checks + " checks passed.");
    }

    private static void testLcs() {
        equal(4, DynamicProgramming.lcsLength("AGGTAB", "GXTXAYB"), "LCS example");
        equal(3, DynamicProgramming.lcsLength("abcde", "ace"), "LCS subsequence");
        equal(4, DynamicProgramming.lcsLength("same", "same"), "LCS identical");
        equal(0, DynamicProgramming.lcsLength("abc", "XYZ"), "LCS different");
        equal(0, DynamicProgramming.lcsLength("", "abc"), "LCS empty input");
    }

    private static void testEditDistance() {
        equal(0, DynamicProgramming.editDistance("kitten", "kitten"), "edit identical");
        equal(6, DynamicProgramming.editDistance("", "kitten"), "edit one empty");
        equal(3, DynamicProgramming.editDistance("kitten", "sitting"), "edit mixed operations");
        equal(1, DynamicProgramming.editDistance("cat", "cut"), "edit substitution");
        equal(1, DynamicProgramming.editDistance("cat", "cart"), "edit insertion");
        equal(1, DynamicProgramming.editDistance("cart", "cat"), "edit deletion");
    }

    private static void testStringMatchers() {
        assertMatches("abcde", "abc", List.of(0), "match at beginning");
        assertMatches("xxabcxx", "abc", List.of(2), "match in middle");
        assertMatches("xxabc", "abc", List.of(2), "match at end");
        assertMatches("abababa", "aba", List.of(0, 2, 4), "overlapping matches");
        assertMatches("abcdefgh", "xyz", List.of(), "pattern absent");
        assertMatches("abc", "longer", List.of(), "pattern longer than text");
        assertMatches("abc", "", List.of(0, 1, 2, 3), "empty pattern boundaries");
        assertMatches("", "", List.of(0), "both strings empty");
        assertMatches("\u0000\u0100", "\u0001\u0000", List.of(), "Rabin-Karp hash collision");
    }

    private static void assertMatches(String text, String pattern, List<Integer> expected, String label) {
        equal(expected, StringMatchers.naive(text, pattern), label + " (naive)");
        equal(expected, StringMatchers.kmp(text, pattern), label + " (KMP)");
        equal(expected, StringMatchers.rabinKarp(text, pattern), label + " (Rabin-Karp)");
    }

    private static void testKasai() {
        String text = "banana";
        int[] suffixArray = {5, 3, 1, 0, 4, 2};
        int[] expected = {0, 1, 3, 0, 0, 2};
        arrayEqual(expected, KasaiLcp.build(text, suffixArray), "manually checked banana LCP");
        arrayEqual(new int[0], KasaiLcp.build("", new int[0]), "empty LCP");
        arrayEqual(suffixArray, SuffixArrays.build(text), "suffix array helper");
        arrayEqual(new int[]{0}, SuffixArrays.build("x"), "single suffix array");
    }

    private static void equal(int expected, int actual, String label) {
        checks++;
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void equal(Object expected, Object actual, String label) {
        checks++;
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void arrayEqual(int[] expected, int[] actual, String label) {
        checks++;
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(label + ": expected " + Arrays.toString(expected)
                    + ", got " + Arrays.toString(actual));
        }
    }
}
