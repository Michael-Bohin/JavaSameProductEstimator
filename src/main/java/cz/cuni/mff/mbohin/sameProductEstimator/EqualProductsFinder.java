package cz.cuni.mff.mbohin.sameProductEstimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;

public class EqualProductsFinder {
    private final List<NormalizedProduct> kosikProducts;
    private final List<NormalizedProduct> rohlikProducts;
    private final List<NormalizedProduct> tescoProducts;
    private static final String loggingDirectory = "./out/equalProductsFinder/";
    private static final String resultDirectory = "./out/equalProductsFinder/results/";

    public EqualProductsFinder(List<NormalizedProduct> kosikProducts, List<NormalizedProduct> rohlikProducts, List<NormalizedProduct> tescoProducts) {
        assertAllProductsAreFromSameEshop(kosikProducts, Eshop.KOSIK);
        assertAllProductsAreFromSameEshop(rohlikProducts, Eshop.ROHLIK);
        assertAllProductsAreFromSameEshop(tescoProducts, Eshop.TESCO);
        this.kosikProducts = kosikProducts;
        this.rohlikProducts = rohlikProducts;
        this.tescoProducts = tescoProducts;

        System.out.println("Normalized products have been loaded to same product estimator.");
        createDirectory(loggingDirectory);
        createDirectory(resultDirectory);
    }

    private void assertAllProductsAreFromSameEshop(List<NormalizedProduct> products, Eshop eshop) {
        for (NormalizedProduct product : products) {
            if (product.eshop != eshop)
                throw new IllegalArgumentException("Product is expected to be normalized from eshop " + eshop + ", but instead it is from " + product.eshop + ".");
        }
    }

    private void createDirectory(String path) {
        // WORK TO DO
    }

    public void sortProbableEqualProducts() {
        EshopSubstrings kosikDict = new EshopSubstrings(kosikProducts);
        EshopSubstrings rohlikDict = new EshopSubstrings(rohlikProducts);
        EshopSubstrings tescoDict = new EshopSubstrings(tescoProducts);

        generateMostProbableEqualProducts(kosikDict, rohlikDict);
        generateMostProbableEqualProducts(kosikDict, tescoDict);
        generateMostProbableEqualProducts(rohlikDict, tescoDict);
    }

    private void generateMostProbableEqualProducts(EshopSubstrings eshopA, EshopSubstrings eshopB) {
        // WORK TO DO
    }
}

