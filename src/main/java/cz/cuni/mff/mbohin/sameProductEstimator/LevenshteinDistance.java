package cz.cuni.mff.mbohin.sameProductEstimator;

public class LevenshteinDistance {

    /**
     * Computes the length-adjusted edit distance between two strings. The edit distance is the minimum number of operations
     * required to transform one string into the other, with operations being insertions, deletions, or substitutions. This
     * method adjusts the raw edit distance by subtracting the absolute difference in the lengths of the two strings, ensuring
     * that the edit distance does not unfairly penalize strings of different lengths. The method also checks for a possible
     * negative result, which would indicate an error in the logic, throwing an IllegalStateException in such a case.
     *
     * @param x the first string
     * @param y the second string
     * @return the adjusted edit distance between the two strings
     * @throws IllegalStateException if the result of the length adjustment is negative, indicating a flaw in the method's logic
     */
    public static int lengthAdjustedEditDistance(String x, String y) {
        int n = x.length();
        int m = y.length();
        int[][] editDistanceTable = new int[n + 1][m + 1];

        // Vyplnovani editDistanceTable[][] je v bottom up smeru
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                if (i == 0) // je-li prvni string prazdny, jediny zpusob je vlozeni znaku druheho stringu
                    editDistanceTable[i][j] = j;

                else if (j == 0) // opacne je-li druhy string prazdny, postupujeme odebiranim znaku prvniho stringu
                    editDistanceTable[i][j] = i;

                else if (x.charAt(i - 1) == y.charAt(j - 1)) // je-li posledni znak stejny, ignoruj posledni znak a skoc na zbytek obou stringu
                    editDistanceTable[i][j] = editDistanceTable[i - 1][j - 1];

                else // jsou-posledni znaky ruzne, zvaz vsechny tri moznosti a vyber minimum
                    editDistanceTable[i][j] = 1 + Math.min(Math.min(editDistanceTable[i - 1][j],    // Odebrani
                                    editDistanceTable[i][j - 1]),                                   // Vlozeni
                            editDistanceTable[i - 1][j - 1]);                                       // Prepsani
            }
        }

        int result = editDistanceTable[n][m] - Math.abs(n - m); // od vysledku odecteme rozdil delek stringu v absolutni hodnote

        // celkem prirozene je zrejme, ze rozdil delek muze byt nanejvys editacni vzdalenost,
        // ale z principu defenzivniho programovani se zabijeme pokud by tento
        // invariant neplatil (muze nastat jen pri bugu v kodu)
        if (result < 0)
            throw new IllegalStateException("Impossible result, there is an error in the code.");

        return result;
    }
}

