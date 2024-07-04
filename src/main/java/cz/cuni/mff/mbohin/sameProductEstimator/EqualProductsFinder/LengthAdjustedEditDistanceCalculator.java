package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.sameProductEstimator.LevenshteinDistance;

/**
 * A {@link SimilarityCalculator} implementation that calculates the length-adjusted edit distance similarity between two products.
 * This class normalizes product names by removing whitespaces and converting to lowercase, computes the edit distance,
 * adjusts it by subtracting the absolute difference in name lengths, and normalizes the result.
 */
public class LengthAdjustedEditDistanceCalculator extends SimilarityCalculator {
    /**
     * Calculates the length-adjusted edit distance similarity between two products. The product names are first normalized by removing
     * whitespaces and converting to lowercase. The edit distance is then adjusted by subtracting the absolute difference in name lengths.
     * This adjusted value is normalized by dividing by the minimum length of the two names, yielding a similarity score that accounts for
     * name length discrepancies.
     *
     * @param product the first product for similarity comparison
     * @param candidate the second product for similarity comparison
     * @return the normalized length-adjusted edit distance as a double, providing a similarity measure between the two products
     */
    @Override
    public double calculate(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = removeWS(product.name).toLowerCase();
        String parsedCandidateName = removeWS(candidate.name).toLowerCase();

        int editDistance = LevenshteinDistance.lengthAdjustedEditDistance(parsedProductName, parsedCandidateName);
        int minLength = Math.min(parsedProductName.length(), parsedCandidateName.length());

        return (double) (minLength - editDistance) / minLength;
    }
}
