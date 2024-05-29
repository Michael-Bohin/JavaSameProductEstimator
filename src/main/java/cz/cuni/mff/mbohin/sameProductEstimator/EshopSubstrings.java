package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the mapping of substrings extracted from product names to their corresponding list of {@link NormalizedProduct} objects.
 * This class is designed to facilitate efficient searching and matching of products based on name parts within an e-commerce platform,
 * enhancing operations such as product comparison and duplication checks.
 *
 * <p>Upon initialization, this class constructs a dictionary where each key is a substring of a product name, and the value is a list of products
 * that contain that substring. Only substrings longer than two characters are considered to avoid overly common and less distinctive name parts.</p>
 *
 * <p>Utility functions within the class help populate and manage this dictionary by:
 * <ul>
 * <li>Adding products to the dictionary by extracting and processing name substrings.</li>
 * <li>Logging statistics about the dictionary's size and the distribution of references to products across different substrings.</li>
 * </ul>
 * </p>
 *
 * <p>The class also provides insights into the efficiency and distribution of the substring indexing through console logs, helping
 * in understanding the spread and commonality of product names within the stored data.</p>
 *
 * @see NormalizedProduct
 */
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
        System.out.printf("Average number of ws split substrings per product %.2f%n \n", (double) substringsToProducts.size() / products.size());
    }
}
