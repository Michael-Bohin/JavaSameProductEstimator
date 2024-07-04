package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

/**
 * Abstract base class for calculating similarity between products.
 * Provides a method to remove whitespace from strings and defines
 * an abstract method for similarity calculation to be implemented by subclasses.
 */
public abstract class SimilarityCalculator {
    abstract double calculate(NormalizedProduct product, NormalizedProduct candidate);

    protected String removeWS(String s) {
        return s.replaceAll("\\s+", "");
    }
}