package edu.assignment44.experiments;

import edu.assignment44.dp.DynamicProgramming;
import edu.assignment44.matching.StringMatchers;
import edu.assignment44.suffix.KasaiLcp;
import edu.assignment44.suffix.SuffixArrays;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Reproducible experiments for the assignment's fixed workload sizes. */
public final class ExperimentRunner {
    private static final int REPETITIONS = 5;
    private static final int WARMUP_REPETITIONS = 5;
    private static final long SEED = 20261108L;
    private static final char[] ALPHABET = {'a', 'b', 'c', 'd'};
    private static final int[] DP_SIZES = {100, 500, 1_000};
    private static final int[] MATCH_SIZES = {10_000, 50_000, 100_000};
    private static final int[] KASAI_SIZES = {1_000, 5_000, 10_000};
    private static volatile Object blackhole;

    private ExperimentRunner() {
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("results") : Path.of(args[0]);
        Files.createDirectories(output);
        runDynamicProgramming(output.resolve("dynamic-programming.csv"));
        runStringMatching(output.resolve("string-matching.csv"));
        runKasai(output.resolve("kasai.csv"));
        System.out.println("Wrote experiment tables to " + output.toAbsolutePath());
    }

    private static void runDynamicProgramming(Path file) throws IOException {
        StringBuilder csv = new StringBuilder("algorithm,input_size,average_ns,repetitions\n");
        Random random = new Random(SEED);
        for (int size : DP_SIZES) {
            String first = randomString(random, size, ALPHABET);
            String second = randomString(random, size, ALPHABET);
            appendTiming(csv, "LCS", size, () -> DynamicProgramming.lcsLength(first, second));
            appendTiming(csv, "Edit distance", size, () -> DynamicProgramming.editDistance(first, second));
        }
        Files.writeString(file, csv, StandardCharsets.UTF_8);
    }

    private static void runStringMatching(Path file) throws IOException {
        StringBuilder csv = new StringBuilder(
                "algorithm,scenario,text_length,pattern_length,average_ns,character_comparisons,occurrences,repetitions\n");
        for (int size : MATCH_SIZES) {
            String text = randomString(new Random(SEED + 31L * size), size, ALPHABET);
            int patternLength = 16;
            String present = text.substring(size / 3, size / 3 + patternLength);
            String absent = "z".repeat(patternLength);
            appendMatchingRows(csv, text, present, "present");
            appendMatchingRows(csv, text, absent, "absent");
        }
        Files.writeString(file, csv, StandardCharsets.UTF_8);
    }

    private static void appendMatchingRows(StringBuilder csv, String text, String pattern, String scenario) {
        List<Integer> expected = StringMatchers.naive(text, pattern);
        List<Integer> kmpResult = StringMatchers.kmp(text, pattern);
        List<Integer> rkResult = StringMatchers.rabinKarp(text, pattern);
        if (!expected.equals(kmpResult) || !expected.equals(rkResult)) {
            throw new IllegalStateException("String matchers disagreed on " + scenario + " workload");
        }

        appendMatchingRow(csv, "Naive", text, pattern, scenario, expected.size(),
                () -> StringMatchers.naive(text, pattern));
        appendMatchingRow(csv, "KMP", text, pattern, scenario, expected.size(),
                () -> StringMatchers.kmp(text, pattern));
        appendMatchingRow(csv, "Rabin-Karp", text, pattern, scenario, expected.size(),
                () -> StringMatchers.rabinKarp(text, pattern));
    }

    private static void appendMatchingRow(StringBuilder csv, String algorithm, String text, String pattern,
                                          String scenario, int occurrences, BenchmarkCall call) {
        double average = averageTime(call::run);
        StringMatchers.SearchStats stats = new StringMatchers.SearchStats();
        switch (algorithm) {
            case "Naive" -> StringMatchers.naive(text, pattern, stats);
            case "KMP" -> StringMatchers.kmp(text, pattern, stats);
            case "Rabin-Karp" -> StringMatchers.rabinKarp(text, pattern, stats);
            default -> throw new IllegalArgumentException("Unknown matcher: " + algorithm);
        }
        csv.append(String.format(Locale.ROOT, "%s,%s,%d,%d,%.2f,%d,%d,%d%n",
                algorithm, scenario, text.length(), pattern.length(), average,
                stats.characterComparisons(), occurrences, REPETITIONS));
    }

    private static void runKasai(Path file) throws IOException {
        StringBuilder csv = new StringBuilder("algorithm,input_size,average_ns,character_comparisons,repetitions\n");
        for (int size : KASAI_SIZES) {
            String text = randomString(new Random(SEED + 97L * size), size, ALPHABET);
            int[] suffixArray = SuffixArrays.build(text);
            double average = averageTime(() -> KasaiLcp.build(text, suffixArray));
            KasaiLcp.Stats stats = new KasaiLcp.Stats();
            KasaiLcp.build(text, suffixArray, stats);
            csv.append(String.format(Locale.ROOT, "Kasai,%d,%.2f,%d,%d%n",
                    size, average, stats.characterComparisons(), REPETITIONS));
        }
        Files.writeString(file, csv, StandardCharsets.UTF_8);
    }

    private static void appendTiming(StringBuilder csv, String algorithm, int size, BenchmarkCall call) {
        double average = averageTime(call::run);
        csv.append(String.format(Locale.ROOT, "%s,%d,%.2f,%d%n", algorithm, size, average, REPETITIONS));
    }

    private static double averageTime(BenchmarkCall operation) {
        for (int i = 0; i < WARMUP_REPETITIONS; i++) {
            blackhole = operation.run();
        }
        long total = 0L;
        for (int i = 0; i < REPETITIONS; i++) {
            long start = System.nanoTime();
            blackhole = operation.run();
            total += System.nanoTime() - start;
        }
        return total / (double) REPETITIONS;
    }

    private static String randomString(Random random, int length, char[] alphabet) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = alphabet[random.nextInt(alphabet.length)];
        }
        return new String(chars);
    }

    @FunctionalInterface
    private interface BenchmarkCall {
        Object run();
    }
}
