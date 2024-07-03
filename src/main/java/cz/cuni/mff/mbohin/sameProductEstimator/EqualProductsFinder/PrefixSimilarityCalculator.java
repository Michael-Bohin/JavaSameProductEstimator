package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

public class PrefixSimilarityCalculator extends SimilarityCalculator {
    /**
     * Calculates the similarity between two products based on the length of their common prefix. The product names are first normalized
     * by converting them to lowercase. The similarity ratio is determined by the length of the common prefix divided by the minimum length
     * of the two product names. This method provides a measure of how similar two product names are, based purely on the initial characters they share.
     *
     * @param product the first product for prefix similarity comparison
     * @param candidate the second product for prefix similarity comparison
     * @return the similarity ratio as a double, representing the proportion of the common prefix length to the shorter product name length
     */
    @Override
    public double calculate(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = product.name.toLowerCase();
        String parsedCandidateName = candidate.name.toLowerCase();

        int commonPrefixLength = commonPrefixLength(parsedProductName, parsedCandidateName);

        return (double) commonPrefixLength / Math.min(parsedProductName.length(), parsedCandidateName.length());
    }

    /**
     * Computes the length of the common prefix between two strings. This method iteratively compares characters
     * from the start of both strings and counts how many characters are identical until it encounters a mismatch.
     * It requires that neither string be null nor empty, throwing an IllegalArgumentException if this precondition is not met.
     *
     * @param parsedProductName the normalized name of the first product
     * @param parsedCandidateName the normalized name of the second product
     * @return the length of the common prefix shared by the two product names
     * @throws IllegalArgumentException if either input string is null or empty, indicating improper prior processing
     */
    private static int commonPrefixLength(String parsedProductName, String parsedCandidateName) {
        if (parsedProductName == null || parsedCandidateName == null || parsedProductName.isEmpty() || parsedCandidateName.isEmpty())
            throw new IllegalArgumentException("Critical error in code architecture detected. Parsed product names at this point may not be null or empty.");

        int prefixLength = 0;
        for (int i = 0; i < Math.min(parsedProductName.length(), parsedCandidateName.length()); i++) {
            if (parsedProductName.charAt(i) == parsedCandidateName.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }

        return prefixLength;
    }
}
