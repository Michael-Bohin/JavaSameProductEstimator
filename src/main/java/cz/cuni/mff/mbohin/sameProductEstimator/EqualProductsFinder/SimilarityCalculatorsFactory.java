package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating and providing a map of different similarity calculators.
 * The calculators are identified by their similarity type names.
 */
public class SimilarityCalculatorsFactory {
    /**
     * Creates and returns a map of similarity calculators.
     * The map keys are the names of the similarity types and the values are the corresponding calculator instances.
     *
     * @return a map of similarity calculators
     */
    public static Map<String, SimilarityCalculator> getSimilarityCalculators() {
        Map<String, SimilarityCalculator> calculators = new HashMap<>();
        calculators.put("substringSimilarity", new SubstringSimilarityCalculator());
        calculators.put("prefixSimilarity", new PrefixSimilarityCalculator());
        calculators.put("LongestCommonSubsequenceSimilarity", new LongestCommonSubsequenceCalculator());
        calculators.put("LengthAdjustedEditationDistance", new LengthAdjustedEditDistanceCalculator());
        return calculators;
    }
}

