package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EshopSubstrings {
    public List<NormalizedProduct> products;
    public final Map<String, List<NormalizedProduct>> substringsToProducts = new HashMap<>();

    public EshopSubstrings(List<NormalizedProduct> products) {
        this.products = products;
        for (NormalizedProduct product : products) {
            addSubstringsToDictionary(product);
        }

        consoleLogDictionarySizeStats();
    }

    private void addSubstringsToDictionary(NormalizedProduct product) {
        for (String part : product.inferredData.getLowerCaseNameParts()) {
            if (part.length() > 2) {
                addPartToDictionary(part, product);
            }
        }
    }

    private void addPartToDictionary(String part, NormalizedProduct product) {
        if (substringsToProducts.containsKey(part)) {
            substringsToProducts.get(part).add(product);
        } else {
            List<NormalizedProduct> list = new ArrayList<>();
            list.add(product);
            substringsToProducts.put(part, list);
        }
    }

    private void consoleLogDictionarySizeStats() {
        System.out.println("Constructed dictionary of product names substrings to list of product references of eshop " + products.getFirst().eshop);
        System.out.println("Dictionary contains " + substringsToProducts.size() + " keys.");

        int counter = 0;
        for (List<NormalizedProduct> productsWithSameSubstrings : substringsToProducts.values()) {
            counter += productsWithSameSubstrings.size();
        }

        System.out.println("Sum of all product references " + counter);
        System.out.printf("Average references per one substring %.2f%n", (double) counter / substringsToProducts.size());
        System.out.printf("Average number of ws split substrings per product %.2f%n", (double) substringsToProducts.size() / products.size());
    }
}
