package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.config.RuntimeConfig;

import java.util.function.BiFunction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.Random;
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
                deleteTextFiles(directoryForCleanUp);
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
     * Asserts that the specified string parameter is indeed a directory, and deletes all files with a ".txt" extension.
     * It checks if the directory exists and is a directory, then finds and deletes all ".txt" files within.
     * If the path is not valid or not a directory, it prints an error message.
     *
     * @param directoryPath the path to the directory where text files will be deleted
     */
    public static void deleteTextFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    assert !file.isFile() || file.delete();
                }
            }
        } else {
            System.out.println("The provided path does not exist or is not a directory.");
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

        logStatsOfCandidates(equalCandidatesFrequencies, smallerEshop, largerEshop);
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
        logSortedCandidates("substringSimilarity", product, largerEshop, sortedCandidates);
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
        logSortedCandidates("prefixSimilarity", product, largerEshop, sortedCandidates);
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
        logSortedCandidates("LongestCommonSubsequenceSimilarity", product, largerEshop, sortedCandidates);
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
        logSortedCandidates("LengthAdjustedEditationDistance", product, largerEshop, sortedCandidates);
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


    private static void logStatsOfCandidates(TreeMap<Integer, Integer> equalCandidatesFrequencies, EshopSubstrings smallerEshop, EshopSubstrings largerEshop) {
        Eshop smallerName = smallerEshop.products.getFirst().eshop;
        Eshop largerName = largerEshop.products.getFirst().eshop;

        String filePath = loggingDirectory + "candidatesStats" + smallerName + "_to_" + largerName + ".txt";
        try (PrintWriter sw = new PrintWriter(filePath)) {
            sw.println("Equal candidates frequencies of " + smallerName + " -> " + largerName);

            int products = 0, candidatesSum = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("Format -- Equal candidates count : frequency\n");
            for (Map.Entry<Integer, Integer> kvp : equalCandidatesFrequencies.entrySet()) {
                sb.append(kvp.getKey()).append(" : ").append(kvp.getValue()).append("\n");
                products += kvp.getValue();
                candidatesSum += kvp.getKey() * kvp.getValue();
            }

            double averageCandidatesPerProduct = (double) candidatesSum / products;
            int numberOfProductPairs = smallerEshop.products.size() * largerEshop.products.size();
            double candidatesAllPairsRatioPercentage = ((double) candidatesSum / numberOfProductPairs) / 100;

            sw.println("Products from smaller eshop: " + formatWithSpaces(products) + " should be equal to " + formatWithSpaces(smallerEshop.products.size()));
            sw.println("Sum of all candidates: " + formatWithSpaces(candidatesSum));
            sw.println(String.format("Average candidates per product of smaller eshop: %.2f", averageCandidatesPerProduct));
            sw.println("Smaller eshop has " + formatWithSpaces(smallerEshop.products.size()) + " products and larger eshop has " + formatWithSpaces(largerEshop.products.size()) + " products.");
            sw.println("Meaning there are " + formatWithSpaces(numberOfProductPairs) + " possible pairs of equal products.");
            sw.println("ListEqualCandidates method managed to narrow down the candidate list to " + formatWithSpaces(candidatesSum));
            sw.println(String.format("Which is %.2f %% of possible pairs.\n\n", candidatesAllPairsRatioPercentage));

            sw.println(sb);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logStatsOfCandidates", e);
        }
    }

    private static String formatWithSpaces(int number) {
        return String.format("%,d", number);
    }

    /**
     * Logs the sorted list of candidate products based on their similarity to a product from a smaller e-shop.
     * This method constructs a directory path based on the product and similarity type, creates the directory if it does not exist,
     * and logs details of the similarity comparison into a file within this directory. Each entry includes the similarity score,
     * candidate product name, and URL. If the directory cannot be created or a file writing error occurs, it logs a severe error.
     *
     * @param similarityType the type of similarity by which the candidates are sorted (e.g., "substringSimilarity")
     * @param product the reference product from the smaller e-shop
     * @param largerEshop the larger e-shop containing candidate products
     * @param sortedCandidates a list of candidates sorted by the specified similarity type, each paired with their similarity score
     */
    public static void logSortedCandidates(String similarityType, NormalizedProduct product, EshopSubstrings largerEshop, List<SimilarityCandidatePair> sortedCandidates) {
        NormalizedProduct largerName = largerEshop.products.getFirst();
        String directoryPath = loggingDirectory + product.eshop + "_to_" + largerName.eshop + "/" + similarityType + "/";
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.log(Level.SEVERE, "Failed to create directory: " + directoryPath);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Equal candidates of ").append(product.name).append(", to be found at url: ").append(product.url).append("\n");

        for (SimilarityCandidatePair candidate : sortedCandidates) {
            sb.append(String.format("%.4f\t%s\t%s\n", candidate.similarity(), candidate.candidate().name, candidate.candidate().url));
        }

        String uniqueFilePath = ensureUniqueFilePath(directoryPath, product.inferredData.getUniqueFileName());

        try (FileWriter fw = new FileWriter(uniqueFilePath)) {
            fw.write(sb.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logSortedCandidates", e);
        }
    }

    /**
     * Ensures that the file path is unique within the specified directory by appending a random number to the filename if necessary.
     * It checks if a file with the given name already exists in the directory; if it does, it generates a new filename with a random number
     * appended to the base filename until a unique filename is obtained. The method finally returns the path of this unique file.
     *
     * @param directory the directory in which to check for uniqueness of the file
     * @param filename the initial filename (without an extension) to use for creating the file
     * @return the unique file path as a String
     */
    public static String ensureUniqueFilePath(String directory, String filename) {
        File file = new File(directory, filename + ".txt");
        while (file.exists()) {
            int randomNumber = new Random().nextInt(Integer.MAX_VALUE) + 1;
            file = new File(directory, filenameWithoutExtension(filename) + "_" + randomNumber + ".txt");
        }
        return file.getPath();
    }

    private static String filenameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }
}
