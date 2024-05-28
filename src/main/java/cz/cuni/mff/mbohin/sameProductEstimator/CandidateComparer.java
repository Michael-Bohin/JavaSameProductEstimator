package cz.cuni.mff.mbohin.sameProductEstimator;

import java.util.Comparator;

public class CandidateComparer implements Comparator<CandidateTuple> {
    @Override
    public int compare(CandidateTuple x, CandidateTuple y) {
        return Double.compare(y.similarityMeasure(), x.similarityMeasure());  // Descending order
    }
}
