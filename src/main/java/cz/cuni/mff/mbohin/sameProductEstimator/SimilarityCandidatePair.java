package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

public record SimilarityCandidatePair(double similarity, NormalizedProduct candidate) { }

