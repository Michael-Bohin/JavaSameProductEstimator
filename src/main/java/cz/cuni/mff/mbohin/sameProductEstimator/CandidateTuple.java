package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

record CandidateTuple(double similarityMeasure, NormalizedProduct candidate) {
}
