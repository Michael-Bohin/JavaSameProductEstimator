package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import java.util.HashSet;

/**
 * A record that pairs a normalized product with a set of candidate products.
 * This is used to store and manage potential matches for a given product.
 *
 * @param product the normalized product
 * @param candidates the set of candidate products that are potential matches
 */
public record ProductHashSetCandidatesPair(NormalizedProduct product, HashSet<NormalizedProduct> candidates) { }
