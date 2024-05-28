package cz.cuni.mff.mbohin.sameProductEstimator;

public class LCSFinder {

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