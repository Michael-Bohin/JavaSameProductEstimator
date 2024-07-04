package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

import java.util.HashSet;

/**
 * A {@link SimilarityCalculator} implementation that calculates the similarity between two products based on the ratio of shared substrings.
 * This class normalizes product names by converting them to lowercase, splits them into substrings, and computes the similarity ratio based on the count of equal substrings.
 */
public class SubstringSimilarityCalculator extends SimilarityCalculator {
    /**
     * Calculates the similarity between two products based on the ratio of shared substrings.
     * The similarity is defined as the count of equal substrings that both products have, divided by the
     * smaller total number of substrings from either product. This method throws an exception if no common
     * substrings are found, indicating a potential misuse or critical error in code architecture.
     *
     * @param product the first product for similarity comparison
     * @param candidate the second product for similarity comparison
     * @return the calculated similarity ratio as a double
     * @throws IllegalArgumentException if there are no common substrings between the two products
     */
    @Override
    public double calculate(NormalizedProduct product, NormalizedProduct candidate) {
        HashSet<String> productSubstrings = getSubstringsSet(product);
        HashSet<String> candidateSubstrings = getSubstringsSet(candidate);

        int sameSubstringsCount = 0;
        for (String substring : productSubstrings) {
            if (candidateSubstrings.contains(substring)) {
                sameSubstringsCount++;
            }
        }

        if (sameSubstringsCount == 0) {
            throw new IllegalArgumentException("In this part of the code, only products with at least one same substring may be called. Critical error in code architecture detected!");
        }

        int minSubstringCount = Math.min(productSubstrings.size(), candidateSubstrings.size());
        return (double) sameSubstringsCount / minSubstringCount;
    }

    private static HashSet<String> getSubstringsSet(NormalizedProduct product) {
        return new HashSet<>(product.inferredData.getLowerCaseNameParts());
    }
}
