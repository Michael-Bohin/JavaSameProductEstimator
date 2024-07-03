package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.config.RuntimeConfig;

import java.util.function.BiFunction;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.TreeMap;

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
    private static final String loggingDirectory = "./out/equalProductsFinder/";

    private static final Logger LOGGER = Logger.getLogger("EqualProductsFinder logger");

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

        prepareStateOfOutputDirectories(kosikProducts, rohlikProducts, tescoProducts);
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
     * Prepares the state of output directories by creating necessary directories and cleaning up old log files.
     * It sets up directories for each similarity type and e-shop pair to log the results of similarity comparisons.
     *
     * @param kosikProducts the list of normalized products from Kosik e-shop
     * @param rohlikProducts the list of normalized products from Rohlik e-shop
     * @param tescoProducts the list of normalized products from Tesco e-shop
     */
    private void prepareStateOfOutputDirectories(List<NormalizedProduct> kosikProducts, List<NormalizedProduct> rohlikProducts, List<NormalizedProduct> tescoProducts) {
        File directory = new File(loggingDirectory);
        boolean wasSuccessful = directory.mkdirs();
        File directory2 = new File(loggingDirectory);
        assert  directory2.mkdirs();

        List<String> similarityTypes = Arrays.asList(
            "substringSimilarity",
            "prefixSimilarity",
            "LongestCommonSubsequenceSimilarity",
            "LengthAdjustedEditationDistance"
        );

        List<String> eshopPairs = formEshopPairsBasedOnSize(kosikProducts, rohlikProducts, tescoProducts);

        for(var similarityType : similarityTypes) {
            for (var eshopPair : eshopPairs) {
                String directoryForCleanUp = loggingDirectory + eshopPair + "/" + similarityType + "/";
                LoggingManager.deleteTextFiles(directoryForCleanUp);
            }
        }
    }

    /**
     * Forms a list of e-shop pairs based on the size of their product lists.
     * This method compares the sizes of the product lists from Kosik, Rohlik, and Tesco e-shops,
     * and generates a list of strings representing the pairs of e-shops to be compared.
     * Each pair is formatted as "smallerEshop_to_largerEshop" based on the size of their product lists.
     *
     * @param kosikProducts the list of normalized products from Kosik e-shop
     * @param rohlikProducts the list of normalized products from Rohlik e-shop
     * @param tescoProducts the list of normalized products from Tesco e-shop
     * @return a list of strings representing pairs of e-shops to be compared, formatted as "smallerEshop_to_largerEshop"
     */
    private static List<String> formEshopPairsBasedOnSize(List<NormalizedProduct> kosikProducts, List<NormalizedProduct> rohlikProducts, List<NormalizedProduct> tescoProducts) {
        List<String> results = new ArrayList<>();
        results.add(compareTwoProductLists(kosikProducts, rohlikProducts));
        results.add(compareTwoProductLists(kosikProducts, tescoProducts));
        results.add(compareTwoProductLists(rohlikProducts, tescoProducts));
        return results;
    }

    private static String compareTwoProductLists(List<NormalizedProduct> firstList, List<NormalizedProduct> secondList) {
        if (firstList.size() < secondList.size()) {
            return firstList.getFirst().eshop + "_to_" + secondList.getFirst().eshop;
        } else {
            return secondList.getFirst().eshop + "_to_" + firstList.getFirst().eshop;
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

        List<ProductHashSetCandidatesPair> equalCandidatesOfProducts = findEqualCandidatesOfProducts(smallerEshop, largerEshop);

        int min = Math.min(RuntimeConfig.limitProcessedProducts, equalCandidatesOfProducts.size());
        for (int i = 0; i < min; i++) {
            ProductHashSetCandidatesPair productAndCandidates = equalCandidatesOfProducts.get(i);
            NormalizedProduct product = productAndCandidates.product();
            HashSet<NormalizedProduct> candidates = productAndCandidates.candidates();

            /**/sortCandidatesBySubstring(product, candidates, largerEshop);/**/
            /**/sortCandidatesByPrefix(product, candidates, largerEshop);/**/
            /**/sortCandidatesByLongestCommonSubsequence(product, candidates, largerEshop);/**/
            /**/sortCandidatesByEditDistance(product, candidates, largerEshop);/**/
        }
    }

    /**
     * Generates a list of potential matching products between a smaller and a larger e-shop based on substring analysis.
     * Each product in the smaller e-shop is compared against all products in the larger e-shop to determine a set of probable equal products.
     * This method also logs the frequency of equal candidates found for each product, helping in the analysis of data matching density.
     *
     * @param smallerEshop the e-shop with fewer products, from which products are compared
     * @param largerEshop the e-shop with more products, against which comparisons are made
     * @return a list of {@link ProductHashSetCandidatesPair} objects, each representing a product from the smaller e-shop
     *         and its set of potential matching products from the larger e-shop
     */
    private static List<ProductHashSetCandidatesPair> findEqualCandidatesOfProducts(EshopSubstrings smallerEshop, EshopSubstrings largerEshop) {
        TreeMap<Integer, Integer> equalCandidatesFrequencies = new TreeMap<>();
        List<ProductHashSetCandidatesPair> equalCandidatesOfProducts = new ArrayList<>();

        for (NormalizedProduct product : smallerEshop.products) {
            HashSet<NormalizedProduct> equalCandidates = listEqualCandidates(product, largerEshop);

            equalCandidatesFrequencies.putIfAbsent(equalCandidates.size(), 0);
            equalCandidatesFrequencies.put(equalCandidates.size(), equalCandidatesFrequencies.get(equalCandidates.size()) + 1);

            equalCandidatesOfProducts.add(new ProductHashSetCandidatesPair(product, equalCandidates));
        }

        LoggingManager.logStatsOfCandidates(equalCandidatesFrequencies, smallerEshop, largerEshop);
        return equalCandidatesOfProducts;
    }

    /**
     * Splits the product name into an array of strings based on whitespace and creates a HashSet of equal candidates.
     * It adds all product references that share at least one substring in their names, which can be efficiently checked
     * using the substring dictionary in the largerEshop class. Only substrings of at least three characters are considered,
     * as shorter substrings often connect semantically unrelated products.
     *
     * @param product the product for which to find equal candidates
     * @param largerEshop the e-shop class containing the substring dictionary
     * @return a HashSet containing all probable equal products
     */
    private static HashSet<NormalizedProduct> listEqualCandidates(NormalizedProduct product, EshopSubstrings largerEshop) {
        HashSet<NormalizedProduct> equalCandidates = new HashSet<>();

        // Assuming product.InferredData.getLowerCaseNameParts() returns List<String>
        for (String part : product.inferredData.getLowerCaseNameParts()) {
            if (part.length() > 2) {
                List<NormalizedProduct> value = largerEshop.substringsToProducts.get(part);
                if (value != null) {
                    equalCandidates.addAll(value);
                }
            }
        }

        return equalCandidates;
    }

    /**
     * Calculates the similarity between a product from a smaller e-shop and multiple candidate products from a larger e-shop based on the ratio of equal substrings.
     * The similarity by equal substrings ratio is defined as the count of equal substrings divided by the minimum number of substrings obtained by splitting
     * the names of both the product and the candidate. This method sorts the candidates from the larger e-shop based on the calculated substring similarity.
     *
     * @param product the product from the smaller e-shop
     * @param candidates a set of candidate products from the larger e-shop
     * @param largerEshop the larger e-shop class providing additional context
     */
    private static void sortCandidatesBySubstring(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculateSubstringSimilarity);
        LoggingManager.logSortedCandidates("substringSimilarity", product, largerEshop, sortedCandidates);
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

    /**
     * Calculates the similarity between two products based on the ratio of shared substrings.
     * The similarity is defined as the count of equal substrings that both products have, divided by the
     * smaller total number of substrings from either product. This method throws an exception if no common
     * substrings are found, indicating a potential misuse or critical error in code architecture.
     *
     * @param product the first product for similarity comparison
     * @param candidate the second product for similarity comparison
     * @return the calculated similarity ratio as a double
     * @throws IllegalArgumentException if there are no common substrings between the two products
     */
    private static double calculateSubstringSimilarity(NormalizedProduct product, NormalizedProduct candidate) {
        HashSet<String> productSubstrings = getSubstringsSet(product);
        HashSet<String> candidateSubstrings = getSubstringsSet(candidate);

        int sameSubstringsCount = 0;
        for (String substring : productSubstrings) {
            if (candidateSubstrings.contains(substring)) {
                sameSubstringsCount++;
            }
        }

        if (sameSubstringsCount == 0) {
            throw new IllegalArgumentException("In this part of the code, only products with at least one same substring may be called. Critical error in code architecture detected!");
        }

        int minSubstringCount = Math.min(productSubstrings.size(), candidateSubstrings.size());
        return (double) sameSubstringsCount / minSubstringCount;
    }

    private static HashSet<String> getSubstringsSet(NormalizedProduct product) {
        return new HashSet<>(product.inferredData.getLowerCaseNameParts());
    }

    /**
     * Sorts a set of candidate products from a larger e-shop based on their common prefix similarity with a given product from a smaller e-shop.
     * The similarity is calculated by first normalizing the product names (removing whitespaces and converting to lowercase), then determining
     * the length of the common prefix between each pair of product names, and finally dividing this length by the
     * shorter of the two name lengths. This method returns a sorted list of candidates based on this common prefix similarity.
     *
     * @param product the product from the smaller e-shop
     * @param candidates a set of candidate products from the larger e-shop
     * @param largerEshop the e-shop containing the candidates
     */
    public static void sortCandidatesByPrefix(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculatePrefixSimilarity);
        LoggingManager.logSortedCandidates("prefixSimilarity", product, largerEshop, sortedCandidates);
    }

    /**
     * Calculates the similarity between two products based on the length of their common prefix. The product names are first normalized
     * by converting them to lowercase. The similarity ratio is determined by the length of the common prefix divided by the minimum length
     * of the two product names. This method provides a measure of how similar two product names are, based purely on the initial characters they share.
     *
     * @param product the first product for prefix similarity comparison
     * @param candidate the second product for prefix similarity comparison
     * @return the similarity ratio as a double, representing the proportion of the common prefix length to the shorter product name length
     */
    public static double calculatePrefixSimilarity(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = product.name.toLowerCase();
        String parsedCandidateName = candidate.name.toLowerCase();

        int commonPrefixLength = commonPrefixLength(parsedProductName, parsedCandidateName);

        return (double) commonPrefixLength / Math.min(parsedProductName.length(), parsedCandidateName.length());
    }

    private static String removeWS(String s) {
        return s.replaceAll("\\s+", "");
    }

    /**
     * Computes the length of the common prefix between two strings. This method iteratively compares characters
     * from the start of both strings and counts how many characters are identical until it encounters a mismatch.
     * It requires that neither string be null nor empty, throwing an IllegalArgumentException if this precondition is not met.
     *
     * @param parsedProductName the normalized name of the first product
     * @param parsedCandidateName the normalized name of the second product
     * @return the length of the common prefix shared by the two product names
     * @throws IllegalArgumentException if either input string is null or empty, indicating improper prior processing
     */
    private static int commonPrefixLength(String parsedProductName, String parsedCandidateName) {
        if (parsedProductName == null || parsedCandidateName == null || parsedProductName.isEmpty() || parsedCandidateName.isEmpty())
            throw new IllegalArgumentException("Critical error in code architecture detected. Parsed product names at this point may not be null or empty.");

        int prefixLength = 0;
        for (int i = 0; i < Math.min(parsedProductName.length(), parsedCandidateName.length()); i++) {
            if (parsedProductName.charAt(i) == parsedCandidateName.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }

        return prefixLength;
    }

    /**
     * Sorts a list of candidate products from a larger e-shop based on their common prefix similarity with a given product from a smaller e-shop.
     * The similarity is calculated by first normalizing the product names (removing whitespaces and converting to lowercase),
     * then determining the length of the common prefix between each pair of product names, and finally dividing this length by the
     * shorter of the two name lengths. The method returns a sorted list of candidates based on this common prefix similarity.
     *
     * @param product the product from the smaller e-shop
     * @param candidates a set of candidate products from the larger e-shop
     * @param largerEshop the e-shop containing the candidates
     */
    private static void sortCandidatesByLongestCommonSubsequence(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculateLCS);
        LoggingManager.logSortedCandidates("LongestCommonSubsequenceSimilarity", product, largerEshop, sortedCandidates);
    }

    /**
     * Calculates the similarity between two products based on the longest common subsequence (LCS) of their names.
     * The names are first preprocessed by removing all whitespaces and converting them to lowercase. The LCS is computed,
     * and the similarity ratio is determined by dividing the LCS length by the minimum length of the two processed names.
     * This method provides a normalized measure of similarity that accounts for the longest sequence of characters that appear
     * in both names in the same order.
     *
     * @param product the first product for LCS similarity comparison
     * @param candidate the second product for LCS similarity comparison
     * @return the similarity ratio as a double, representing the length of LCS divided by the shortest name length
     */
    private static double calculateLCS(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = removeWS(product.name).toLowerCase();
        String parsedCandidateName = removeWS(candidate.name).toLowerCase();

        int LCS = LCSFinder.longestCommonSubsequence(parsedProductName, parsedCandidateName);

        return (double)LCS / Math.min(parsedProductName.length(), parsedCandidateName.length());
    }

    /**
     * Sorts a list of candidate products from a larger e-shop based on their length-adjusted edit distance similarity with a given product from a smaller e-shop.
     * The similarity is calculated by normalizing the product names (removing whitespaces and converting to lowercase), then computing the edit distance,
     * and adjusting it by subtracting the absolute difference in length between the two names. This method returns a sorted list of candidates based on this
     * adjusted edit distance.
     *
     * @param product the product from the smaller e-shop
     * @param candidates a set of candidate products from the larger e-shop
     * @param largerEshop the e-shop containing the candidates
     */
    private static void sortCandidatesByEditDistance(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculateLengthAdjustedEditDistance);
        LoggingManager.logSortedCandidates("LengthAdjustedEditationDistance", product, largerEshop, sortedCandidates);
    }

    /**
     * Calculates the length-adjusted edit distance similarity between two products. The product names are first normalized by removing
     * whitespaces and converting to lowercase. The edit distance is then adjusted by subtracting the absolute difference in name lengths.
     * This adjusted value is normalized by dividing by the minimum length of the two names, yielding a similarity score that accounts for
     * name length discrepancies.
     *
     * @param product the first product for similarity comparison
     * @param candidate the second product for similarity comparison
     * @return the normalized length-adjusted edit distance as a double, providing a similarity measure between the two products
     */
    private static double calculateLengthAdjustedEditDistance(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = removeWS(product.name).toLowerCase();
        String parsedCandidateName = removeWS(candidate.name).toLowerCase();

        int editDistance = LevenshteinDistance.lengthAdjustedEditDistance(parsedProductName, parsedCandidateName);
        int minLength = Math.min(parsedProductName.length(), parsedCandidateName.length());

        return (double) (minLength - editDistance) / minLength;
    }
}
