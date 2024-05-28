package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

import java.util.Comparator;


class CandidateTuple {
    private double similarityMeasure;
    private NormalizedProduct candidate;

    public CandidateTuple(double similarityMeasure, NormalizedProduct candidate) {
        this.similarityMeasure = similarityMeasure;
        this.candidate = candidate;
    }

    public double getSimilarityMeasure() {
        return similarityMeasure;
    }

    public NormalizedProduct getCandidate() {
        return candidate;
    }
}

