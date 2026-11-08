package edu.assignment44.dp;

import java.util.Objects;

/** Dynamic-programming algorithms for pairs of strings. */
public final class DynamicProgramming {
    private DynamicProgramming() {
    }

    /** Returns the length of a longest common subsequence of {@code first} and {@code second}. */
    public static int lcsLength(String first, String second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        int[][] dp = new int[first.length() + 1][second.length() + 1];
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[first.length()][second.length()];
    }

    /** Returns the Levenshtein distance using insertions, deletions, and substitutions. */
    public static int editDistance(String first, String second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        int[][] dp = new int[first.length() + 1][second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int substitutionCost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + substitutionCost);
            }
        }
        return dp[first.length()][second.length()];
    }
}
