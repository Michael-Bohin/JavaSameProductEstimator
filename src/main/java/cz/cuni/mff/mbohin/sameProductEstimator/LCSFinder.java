package cz.cuni.mff.mbohin.sameProductEstimator;

public class LCSFinder {
    /**
     * Calculates the length of the longest common subsequence (LCS) between two strings. The LCS is the longest sequence of characters
     * that appear in the same order in both strings but not necessarily consecutively. This method uses dynamic programming to build
     * a table of LCS lengths for all substrings, which allows it to determine the LCS length for the entire strings efficiently.
     *
     * @param x the first string
     * @param y the second string
     * @return the length of the longest common subsequence between the two strings
     */
    public static int longestCommonSubsequence(String x, String y) {
        int m = x.length();
        int n = y.length();
        int[][] lcsLengthTable = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (x.charAt(i - 1) == y.charAt(j - 1)) {
                    lcsLengthTable[i][j] = lcsLengthTable[i - 1][j - 1] + 1;
                } else {
                    lcsLengthTable[i][j] = Math.max(lcsLengthTable[i - 1][j], lcsLengthTable[i][j - 1]);
                }
            }
        }

        return lcsLengthTable[m][n];
    }
}