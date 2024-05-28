package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import java.util.List;
class ProductListCandidatesPair {
    private final NormalizedProduct product;
    private final List<NormalizedProduct> candidates;

    public ProductListCandidatesPair(NormalizedProduct product, List<NormalizedProduct> candidates) {
        this.product = product;
        this.candidates = candidates;
    }

    public NormalizedProduct getProduct() {
        return product;
    }

    public List<NormalizedProduct> getCandidates() {
        return candidates;
    }
}
