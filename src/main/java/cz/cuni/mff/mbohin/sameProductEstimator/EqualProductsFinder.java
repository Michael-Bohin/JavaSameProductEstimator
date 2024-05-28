package cz.cuni.mff.mbohin.sameProductEstimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import java.io.File;

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

        File directory = new File(loggingDirectory);
        boolean wasSuccessful = directory.mkdirs();
        File directory2 = new File(loggingDirectory);
        wasSuccessful = directory2.mkdirs();
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

    @SuppressWarnings("unused")
    public void sortProbableEqualProductsAsync() throws InterruptedException {
        EshopSubstrings kosikDict = new EshopSubstrings(kosikProducts);
        EshopSubstrings rohlikDict = new EshopSubstrings(rohlikProducts);
        EshopSubstrings tescoDict = new EshopSubstrings(tescoProducts);

        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            executor.submit(() -> generateMostProbableEqualProducts(kosikDict, rohlikDict));
            executor.submit(() -> generateMostProbableEqualProducts(kosikDict, tescoDict));
            executor.submit(() -> generateMostProbableEqualProducts(rohlikDict, tescoDict));

            executor.shutdown();
            boolean terminated = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    private void generateMostProbableEqualProducts(EshopSubstrings eshopA, EshopSubstrings eshopB) {
        // WORK TO DO
    }
}

