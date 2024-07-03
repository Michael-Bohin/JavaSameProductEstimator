package cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.sameProductEstimator.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

public class ProductPairingManager {
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
    public static List<String> formEshopPairsBasedOnSize(List<NormalizedProduct> kosikProducts, List<NormalizedProduct> rohlikProducts, List<NormalizedProduct> tescoProducts) {
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
     * Generates a list of potential matching products between a smaller and a larger e-shop based on substring analysis.
     * Each product in the smaller e-shop is compared against all products in the larger e-shop to determine a set of probable equal products.
     * This method also logs the frequency of equal candidates found for each product, helping in the analysis of data matching density.
     *
     * @param smallerEshop the e-shop with fewer products, from which products are compared
     * @param largerEshop the e-shop with more products, against which comparisons are made
     * @return a list of {@link ProductHashSetCandidatesPair} objects, each representing a product from the smaller e-shop
     *         and its set of potential matching products from the larger e-shop
     */
    public static List<ProductHashSetCandidatesPair> findEqualCandidatesOfProducts(EshopSubstrings smallerEshop, EshopSubstrings largerEshop) {
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
}

