package cz.cuni.mff.mbohin.sameProductEstimator;

import java.util.Map;
import java.util.TreeMap;

/**
 * The Statistics class is responsible for calculating and formatting statistical information
 * about the frequency of equal candidates found between two e-shops. It provides a method
 * to generate a detailed report of these statistics in a human-readable format.
 */
public class Statistics {
    private final TreeMap<Integer, Integer> equalCandidatesFrequencies;
    private final int smallerEshopSize;
    private final int largerEshopSize;

    /**
     * Constructs a Statistics instance with the given frequency map and sizes of the two e-shops.
     *
     * @param equalCandidatesFrequencies a TreeMap where keys are the number of equal candidates and values are their frequencies
     * @param smallerEshopSize the number of products in the smaller e-shop
     * @param largerEshopSize the number of products in the larger e-shop
     */
    public Statistics(TreeMap<Integer, Integer> equalCandidatesFrequencies, int smallerEshopSize, int largerEshopSize) {
        this.equalCandidatesFrequencies = equalCandidatesFrequencies;
        this.smallerEshopSize = smallerEshopSize;
        this.largerEshopSize = largerEshopSize;
    }

    /**
     * Generates a formatted string containing detailed statistical information about the frequency of
     * equal candidates. This includes the frequency distribution, average candidates per product, and
     * the percentage of candidates relative to the total possible pairs.
     *
     * @return a formatted string with the statistical information
     */
    public String getFormattedStatistics() {
        StringBuilder sb = new StringBuilder();
        int products = 0, candidatesSum = 0;

        sb.append("Format -- Equal candidates count : frequency\n");
        for (Map.Entry<Integer, Integer> kvp : equalCandidatesFrequencies.entrySet()) {
            sb.append(kvp.getKey()).append(" : ").append(kvp.getValue()).append("\n");
            products += kvp.getValue();
            candidatesSum += kvp.getKey() * kvp.getValue();
        }

        double averageCandidatesPerProduct = (double) candidatesSum / products;
        int numberOfProductPairs = smallerEshopSize * largerEshopSize;
        double candidatesAllPairsRatioPercentage = ((double) candidatesSum / numberOfProductPairs) * 100;

        sb.append("Products from smaller eshop: ").append(formatWithSpaces(products))
                .append(" should be equal to ").append(formatWithSpaces(smallerEshopSize)).append("\n");
        sb.append("Sum of all candidates: ").append(formatWithSpaces(candidatesSum)).append("\n");
        sb.append(String.format("Average candidates per product of smaller eshop: %.2f", averageCandidatesPerProduct)).append("\n");
        sb.append("Smaller eshop has ").append(formatWithSpaces(smallerEshopSize)).append(" products and larger eshop has ")
                .append(formatWithSpaces(largerEshopSize)).append(" products.\n");
        sb.append("Meaning there are ").append(formatWithSpaces(numberOfProductPairs)).append(" possible pairs of equal products.\n");
        sb.append("ListEqualCandidates method managed to narrow down the candidate list to ").append(formatWithSpaces(candidatesSum)).append("\n");
        sb.append(String.format("Which is %.2f %% of possible pairs.\n\n", candidatesAllPairsRatioPercentage)).append("\n");

        return sb.toString();
    }

    private String formatWithSpaces(int number) {
        return String.format("%,d", number);
    }
}

