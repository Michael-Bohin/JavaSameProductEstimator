package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.sameProductEstimator.LCSFinder;

/**
 * A {@link SimilarityCalculator} implementation that calculates the similarity between two products based on the longest common subsequence (LCS) of their names.
 * This class normalizes product names by removing whitespaces and converting to lowercase, computes the LCS, and normalizes the result by the minimum length of the two names.
 */
public class LongestCommonSubsequenceCalculator extends SimilarityCalculator {
    /**
     * Calculates the similarity between two products based on the longest common subsequence (LCS) of their names.
     * The names are first preprocessed by removing all whitespaces and converting them to lowercase. The LCS is computed,
     * and the similarity ratio is determined by dividing the LCS length by the minimum length of the two processed names.
     * This method provides a normalized measure of similarity that accounts for the longest sequence of characters that appear
     * in both names in the same order.
     *
     * @param product the first product for LCS similarity comparison
     * @param candidate the second product for LCS similarity comparison
     * @return the similarity ratio as a double, representing the length of LCS divided by the shortest name length
     */
    @Override
    public double calculate(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = removeWS(product.name).toLowerCase();
        String parsedCandidateName = removeWS(candidate.name).toLowerCase();

        int LCS = LCSFinder.longestCommonSubsequence(parsedProductName, parsedCandidateName);

        return (double)LCS / Math.min(parsedProductName.length(), parsedCandidateName.length());
    }
}
