package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.sameProductEstimator.*;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * The EqualProductsFinder class is responsible for identifying and sorting probable equal products
 * between multiple e-shops (Kosik, Rohlik, and Tesco). This class uses various similarity measures
 * such as substring similarity, prefix similarity, longest common subsequence, and length-adjusted edit distance
 * to compare and match products across these e-shops.
 * It initializes by asserting that the provided lists of normalized products belong to their e-shops and
 * prepares the output directories for logging. The core functionalities are:
 * - Asynchronously processing and sorting probable equal products between the e-shops using multithreading.
 * - Comparing products from smaller e-shops against larger e-shops to optimize the matching process.
 * - Logging the results of the similarity comparisons for further analysis.
 * The class also provides methods to clean up old log files, calculate various similarity metrics,
 * and ensure unique file paths for logging results.
 * Example usage:
 * <pre>
 * {@code
 * List<NormalizedProduct> kosikProducts = ...;
 * List<NormalizedProduct> rohlikProducts = ...;
 * List<NormalizedProduct> tescoProducts = ...;
 * EqualProductsFinder finder = new EqualProductsFinder(kosikProducts, rohlikProducts, tescoProducts);
 * finder.sortProbableEqualProductsAsync();
 * }
 * </pre>
 *
 * Note: The class assumes that product names are normalized using specific adapters of eshops.
 * @see NormalizedProduct
 * @see Eshop
 * @see EshopSubstrings
 * @see ProductHashSetCandidatesPair
 * @see SimilarityCandidatePair
 * @see LCSFinder
 * @see LevenshteinDistance
 */
@SuppressWarnings("unused")
public class EqualProductsFinder {
    private final List<NormalizedProduct> kosikProducts;
    private final List<NormalizedProduct> rohlikProducts;
    private final List<NormalizedProduct> tescoProducts;
    /**
     * Constructs an EqualProductsFinder instance with lists of normalized products from Kosik, Rohlik, and Tesco e-shops.
     * This constructor ensures that all products in each list belong to their respective e-shops and prepares the output
     * directories for logging the results of the similarity comparisons.
     *
     * @param kosikProducts the list of normalized products from Kosik e-shop
     * @param rohlikProducts the list of normalized products from Rohlik e-shop
     * @param tescoProducts the list of normalized products from Tesco e-shop
     * @throws IllegalArgumentException if any product in the provided lists does not belong to its specified e-shop
     */
    public EqualProductsFinder(List<NormalizedProduct> kosikProducts, List<NormalizedProduct> rohlikProducts, List<NormalizedProduct> tescoProducts) {
        assertAllProductsAreFromSameEshop(kosikProducts, Eshop.KOSIK);
        assertAllProductsAreFromSameEshop(rohlikProducts, Eshop.ROHLIK);
        assertAllProductsAreFromSameEshop(tescoProducts, Eshop.TESCO);

        this.kosikProducts = kosikProducts;
        this.rohlikProducts = rohlikProducts;
        this.tescoProducts = tescoProducts;
        System.out.println("Normalized products have been loaded to same product estimator.\n");

        LoggingManager.prepareStateOfOutputDirectories(kosikProducts, rohlikProducts, tescoProducts);
    }

    /**
     * Ensures that all products in the specified list belong to the given e-shop.
     *
     * @param products the list of normalized products to check
     * @param eshop the expected e-shop for all products in the list
     * @throws IllegalArgumentException if any product in the list does not belong to the specified e-shop
     */
    private void assertAllProductsAreFromSameEshop(List<NormalizedProduct> products, Eshop eshop) {
        for (NormalizedProduct product : products) {
            if (product.eshop != eshop)
                throw new IllegalArgumentException("Product is expected to be normalized from eshop " + eshop + ", but instead it is from " + product.eshop + ".");
        }
    }

    /**
     * Asynchronously processes and sorts probable equal products between multiple e-shops using multithreading.
     * This method initializes dictionaries for each e-shop and utilizes a fixed thread pool to concurrently execute the sorting of probable
     * equal products between Kosik, Rohlik, and Tesco e-shops. Each pair of e-shops is processed in a separate thread to enhance performance.
     * The method ensures that all threads complete their execution before returning. It uses a thread pool of size three to match the three tasks,
     * ensuring that each task can run concurrently without waiting for thread availability.
     *
     * @throws InterruptedException if the thread execution is interrupted while waiting for completion
     */
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

    /**
     * Identifies the e-shop with fewer products and generates a list of most probable equal products for each product in this smaller e-shop.
     * It then sorts the list of candidates from the larger e-shop based on different measures to find the best matches. The sorting criteria include:
     * a. Ratio of equal substrings.
     * b. Same longest prefix.
     * c. Longest common subsequence.
     * d. Edit distance of product names.
     * This method aims to optimize product matching across e-shops by focusing on the smaller inventory to reduce computational demand and enhance accuracy.
     *
     * @param eshopA the first e-shop to compare
     * @param eshopB the second e-shop to compare
     */
    private static void generateMostProbableEqualProducts(EshopSubstrings eshopA, EshopSubstrings eshopB) {
        EshopSubstrings smallerEshop = eshopA.products.size() < eshopB.products.size() ? eshopA : eshopB;
        EshopSubstrings largerEshop = eshopA.products.size() >= eshopB.products.size() ? eshopA : eshopB;
        List<ProductHashSetCandidatesPair> equalCandidatesOfProducts = ProductPairingManager.findEqualCandidatesOfProducts(smallerEshop, largerEshop);

        for (Map.Entry<String, SimilarityCalculator> entry : SimilarityCalculatorsFactory.getSimilarityCalculators().entrySet()) {
            for (int i = 0; i < Math.min(RuntimeConfig.limitProcessedProducts, equalCandidatesOfProducts.size()); i++) {
                ProductHashSetCandidatesPair productAndCandidates = equalCandidatesOfProducts.get(i);
                NormalizedProduct product = productAndCandidates.product();
                HashSet<NormalizedProduct> candidates = productAndCandidates.candidates();

                List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, entry.getValue()::calculate);
                LoggingManager.logSortedCandidates(entry.getKey(), product, largerEshop, sortedCandidates);
            }
        }
    }

    /**
     * Sorts a set of candidate products based on their similarity to a given product. The similarity is calculated using a specified
     * function that compares the product and each candidate. The method returns a list of candidates paired with their similarity scores,
     * sorted in descending order of similarity.
     *
     * @param product the reference product from which similarity is measured
     * @param candidates a set of candidate products to be compared with the reference product
     * @param calculateSimilarity a function that computes the similarity between two products, returning a double value
     * @return a list of SimilarityCandidatePair objects, each containing a candidate and its similarity score, sorted by similarity in descending order
     */
    private static List<SimilarityCandidatePair> sortCandidates(NormalizedProduct product, HashSet<NormalizedProduct> candidates, BiFunction<NormalizedProduct, NormalizedProduct, Double> calculateSimilarity) {
        List<SimilarityCandidatePair> sortedCandidates = new ArrayList<>();
        for (NormalizedProduct candidate : candidates) {
            double similarity = calculateSimilarity.apply(product, candidate);
            sortedCandidates.add(new SimilarityCandidatePair(similarity, candidate));
        }
        sortedCandidates.sort((o1, o2) -> Double.compare(o2.similarity(), o1.similarity()));
        return sortedCandidates;
    }
}
