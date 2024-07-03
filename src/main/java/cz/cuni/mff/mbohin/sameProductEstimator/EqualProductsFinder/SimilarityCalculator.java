package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

public abstract class SimilarityCalculator {
    abstract double calculate(NormalizedProduct product, NormalizedProduct candidate);

    protected String removeWS(String s) {
        return s.replaceAll("\\s+", "");
    }
}