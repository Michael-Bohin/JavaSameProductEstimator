package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import java.util.HashSet;

record ProductHashSetCandidatesPair(NormalizedProduct product, HashSet<NormalizedProduct> candidates) { }
