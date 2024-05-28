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

public class EqualProductsFinder {
    private final List<NormalizedProduct> kosikProducts;
    private final List<NormalizedProduct> rohlikProducts;
    private final List<NormalizedProduct> tescoProducts;
    private static final String loggingDirectory = "./out/equalProductsFinder/", resultDirectory = "./out/equalProductsFinder/results/";

    private static final Logger LOGGER = Logger.getLogger("logger");

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

    /// <summary>
    /// 1. Foreach eshop creates dictionaries substrings in names to list of references of products
    /// 2. Foreach eshop pair
    /// 3.		For eshop e with less products
    /// 4.			For each product in eshop e
    /// 5.				Creates & saves sorted list of most probable equal products
    /// </summary>
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

    /// <summary>
    /// Pick eshop e with lower number of products.
    /// For each product from eshop e generate list of most probable equal products.
    /// Foreach each product from smaller eshop sort the candidates by differet measures.
    ///		a. Ratio of equal substrings
    ///		b. Same longest prefix
    ///		c. Longest common subsequence
    ///		d. Editacni vzdalenost nazvu
    /// </summary>
    /// <param name="eshopA"></param>
    /// <param name="eshopB"></param>
    private static void generateMostProbableEqualProducts(EshopSubstrings eshopA, EshopSubstrings eshopB) {
        EshopSubstrings smallerEshop = eshopA.products.size() < eshopB.products.size() ? eshopA : eshopB;
        EshopSubstrings largerEshop = eshopA.products.size() >= eshopB.products.size() ? eshopA : eshopB;
        createLoggingDirectory(smallerEshop, largerEshop);

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

    private static void createLoggingDirectory(EshopSubstrings smallerEshop, EshopSubstrings largerEshop) {
        if (smallerEshop.products.isEmpty() || largerEshop.products.isEmpty())
            throw new IllegalArgumentException("Eshop product lists cannot be empty.");

        String directoryRoot = "./out/" + smallerEshop.products.getFirst().eshop.toString() + "To" + largerEshop.products.getFirst().eshop.toString() + "ProbableEqualProducts/";

        File directory = new File(directoryRoot);
        if (!directory.exists()) {
            boolean wasSuccessful = directory.mkdirs();
            if (!wasSuccessful) {
                System.out.println("Failed to create directory: " + directoryRoot);
            } else {
                System.out.println("Directory created successfully: " + directoryRoot);
            }
        } else {
            System.out.println("Directory already exists: " + directoryRoot);
        }
    }

    /// <summary>
    /// Create a preliminary list of possible equal products by creating a list of all products that have
    ///	at least one same substring in their name.
    /// </summary>
    /// <param name="product"></param>
    /// <param name="largerEshop"></param>
    /// <param name="outRoot"></param>
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

    /// <summary>
    /// Method splits product name on whitespaces into string array.
    /// The hashset of equal candidates is than created by adding all product references that contain
    /// at least one same substring in their name. This information can be looked up in linear time thanks
    /// to the substring dictionary in largerEshop class.
    ///
    /// Only substrings with length of at least three characters are considered, since substrings with
    /// one or two characters are more likely to connect semantically unrelated products.
    /// </summary>
    /// <param name="productName"></param>
    /// <param name="largerEshop"></param>
    /// <returns></returns>

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

    /// <summary>
    /// Input:
    /// product - one concrete product from smaller eshop
    /// candidates - n candidates of equal products from larger eshop
    ///
    /// Foreach pair (product, candidate i) method calculates similarity by equal substrings ratio which is defined as:
    ///
    /// substrings similarity = equal substrings count / Min( product.name.Split(' ').Length, candidate.name.Split(' ').Length )
    ///
    /// In words, we take number of equal substrings recieved after spliting product's name on whitespace and divide with
    /// the smaller number of substrings of both products.
    ///
    /// Output:
    /// Sorted list of equal candidates from larger eshop of normalized product of smaller eshop.
    /// Sorted by substrings similarity.
    ///
    /// </summary>
    /// <param name="product"></param>
    /// <param name="candidates"></param>
    /// <param name="largerEshop"></param>
    /// <returns></returns>
    private static void sortCandidatesBySubstring(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculateSubstringSimilarity);
        logSortedCandidates("substringSimilarity", product, largerEshop, sortedCandidates);
    }

    private static List<SimilarityCandidatePair> sortCandidates(NormalizedProduct product, HashSet<NormalizedProduct> candidates, BiFunction<NormalizedProduct, NormalizedProduct, Double> calculateSimilarity) {
        List<SimilarityCandidatePair> sortedCandidates = new ArrayList<>();
        for (NormalizedProduct candidate : candidates) {
            double similarity = calculateSimilarity.apply(product, candidate);
            sortedCandidates.add(new SimilarityCandidatePair(similarity, candidate));
        }
        sortedCandidates.sort((o1, o2) -> Double.compare(o2.similarity(), o1.similarity()));
        return sortedCandidates;
    }


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

    /// <summary>
    /// Input:
    /// product - one concrete product from smaller eshop
    /// candidates - n candidates of equal products from larger eshop
    ///
    /// Foreach pair (product, candidate i) method calculates similarity by common prefix length ratio which is defined as:
    ///
    /// string productName = product.name.RemoveWS().ToLower()
    /// string candidateName = (candidate i).name.RemoveWS().ToLower()
    /// common prefix similarity = CommonPrefixLength( productName,  candidateName) / Math.Min(productName.Length, candidateName.Length)
    ///
    /// In words, we first parse the names by removing whitespaces and make all characters lower case.
    /// Then we divide the length of common prefix by smaller length out of both names.
    ///
    /// Output:
    /// Sorted list of equal candidates from larger eshop of normalized product of smaller eshop.
    /// Sorted by  common prefix similarity.
    ///
    /// </summary>
    /// <param name="product"></param>
    /// <param name="candidates"></param>
    /// <param name="largerEshop"></param>
    /// <returns></returns>
    public static void sortCandidatesByPrefix(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculatePrefixSimilarity);
        logSortedCandidates("prefixSimilarity", product, largerEshop, sortedCandidates);
    }

    public static double calculatePrefixSimilarity(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = product.name.toLowerCase();
        String parsedCandidateName = candidate.name.toLowerCase();

        int commonPrefixLength = commonPrefixLength(parsedProductName, parsedCandidateName);

        return (double) commonPrefixLength / Math.min(parsedProductName.length(), parsedCandidateName.length());
    }

    private static String removeWS(String s) {
        return s.replaceAll("\\s+", "");
    }

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

    /// <summary>
    /// Input:
    /// product - one concrete product from smaller eshop
    /// candidates - n candidates of equal products from larger eshop
    ///
    /// Foreach pair (product, candidate i) method calculates similarity by longest common subsequence ratio which is defined as:
    ///
    /// LCS ratio = LCS length / Min( product.name.RemoveWS().Length, candidate.name.RemoveWS().Length )
    ///
    /// In words, we take the length of LCS of parsed product names and divide with
    /// the smaller number of substrings of both products.
    ///
    /// Output:
    /// Sorted list of equal candidates from larger eshop of normalized product of smaller eshop.
    /// Sorted by substrings similarity.
    ///
    /// </summary>
    /// <param name="product"></param>
    /// <param name="candidates"></param>
    /// <param name="largerEshop"></param>
    /// <returns></returns>
    private static void sortCandidatesByLongestCommonSubsequence(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculateLCS);
        logSortedCandidates("LongestCommonSubsequenceSimilarity", product, largerEshop, sortedCandidates);
    }

    private static double calculateLCS(NormalizedProduct product, NormalizedProduct candidate) {
        String parsedProductName = removeWS(product.name).toLowerCase();
        String parsedCandidateName = removeWS(candidate.name).toLowerCase();

        int LCS = LCSFinder.longestCommonSubsequence(parsedProductName, parsedCandidateName);

        return (double)LCS / Math.min(parsedProductName.length(), parsedCandidateName.length());
    }
    /// <summary>
    /// Input:
    /// product - one concrete product from smaller eshop
    /// candidates - n candidates of equal products from larger eshop
    ///
    /// Foreach pair (product, candidate i) method calculates similarity by length adjusted editn distance which is defined as:
    ///
    /// string productName = product.name.RemoveWS().ToLower()
    /// string candidateName = (candidate i).name.RemoveWS().ToLower()
    /// length adjusted edit distance = EditationDistance(productName, candidateName) - Math.Abs(productName - candidateName)
    ///
    /// Output:
    /// Sorted list of equal candidates from larger eshop of normalized product of smaller eshop.
    /// Sorted by length adjusted editation distance.
    ///
    /// </summary>
    /// <param name="product"></param>
    /// <param name="candidates"></param>
    /// <param name="largerEshop"></param>
    /// <returns></returns>
    private static void sortCandidatesByEditDistance(NormalizedProduct product, HashSet<NormalizedProduct> candidates, EshopSubstrings largerEshop) {
        List<SimilarityCandidatePair> sortedCandidates = sortCandidates(product, candidates, EqualProductsFinder::calculateLengthAdjustedEditDistance);
        logSortedCandidates("LengthAdjustedEditationDistance", product, largerEshop, sortedCandidates);
    }
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

            sw.println(sb.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logStatsOfCandidates", e);
        }
    }

    private static String formatWithSpaces(int number) {
        return String.format("%,d", number);
    }

    public static void logSortedCandidates(String similarityType, NormalizedProduct product, EshopSubstrings largerEshop, List<SimilarityCandidatePair> sortedCandidates) {
        NormalizedProduct largerName = largerEshop.products.getFirst();
        String directoryPath = loggingDirectory + product.eshop + "_to_" + largerName.eshop + "/" + similarityType + "/";
        File directory = new File(directoryPath);
        assert directory.mkdirs();

        StringBuilder sb = new StringBuilder();
        sb.append("Equal candidates of ").append(product.name).append(", to be found at url: ").append(product.url).append("\n");

        for (SimilarityCandidatePair candidate : sortedCandidates) {
            sb.append(String.format("%.4f\t%s\t%s\n", candidate.similarity(), candidate.candidate(), candidate.candidate().url));
        }

        String uniqueFilePath = ensureUniqueFilePath(directoryPath, product.inferredData.getUniqueFileName());

        try (FileWriter fw = new FileWriter(new File(uniqueFilePath))) {
            fw.write(sb.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logSortedCandidates", e);
        }
    }

    public static String ensureUniqueFilePath(String directory, String filename) {
        File file = new File(directory, filename + ".txt");
        while (file.exists()) {
            int randomNumber = new Random().nextInt();
            file = new File(directory, filenameWithoutExtension(filename) + "_" + randomNumber + ".txt");
        }
        return file.getPath();
    }

    private static String filenameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }
}

