package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import java.util.HashMap;
import java.util.Map;

public class SimilarityCalculatorsFactory {
    public static Map<String, SimilarityCalculator> getSimilarityCalculators() {
        Map<String, SimilarityCalculator> calculators = new HashMap<>();
        calculators.put("substringSimilarity", new SubstringSimilarityCalculator());
        calculators.put("prefixSimilarity", new PrefixSimilarityCalculator());
        calculators.put("LongestCommonSubsequenceSimilarity", new LongestCommonSubsequenceCalculator());
        calculators.put("LengthAdjustedEditationDistance", new LengthAdjustedEditDistanceCalculator());
        return calculators;
    }
}

