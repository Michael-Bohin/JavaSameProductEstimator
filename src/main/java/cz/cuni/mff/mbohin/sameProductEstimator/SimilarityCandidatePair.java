package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

/**
 * A record that pairs a similarity score with a candidate product.
 * This is used to store and manage the similarity score of a potential match for a given product.
 *
 * @param similarity the similarity score between the product and the candidate
 * @param candidate the candidate product being compared
 */
public record SimilarityCandidatePair(double similarity, NormalizedProduct candidate) { }

