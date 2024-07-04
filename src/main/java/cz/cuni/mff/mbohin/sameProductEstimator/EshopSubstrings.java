package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the mapping of substrings extracted from product names to their corresponding list of {@link NormalizedProduct} objects.
 * This class is designed to facilitate efficient searching and matching of products based on name parts within an e-commerce platform,
 * enhancing operations such as product comparison and duplication checks.
 *
 * <p>Upon initialization, this class constructs a dictionary where each key is a substring of a product name, and the value is a list of products
 * that contain that substring. Only substrings longer than two characters are considered to avoid overly common and less distinctive name parts.</p>
 *
 * <p>Utility functions within the class help populate and manage this dictionary by:</p>
 * <ul>
 * <li>Adding products to the dictionary by extracting and processing name substrings.</li>
 * <li>Logging statistics about the dictionary's size and the distribution of references to products across different substrings.</li>
 * </ul>
 *
 * <p>The class also provides insights into the efficiency and distribution of the substring indexing through console logs, helping
 * in understanding the spread and commonality of product names within the stored data.</p>
 *
 * @see NormalizedProduct
 */
public class EshopSubstrings {
    public List<NormalizedProduct> products;
    public final Map<String, List<NormalizedProduct>> substringsToProducts = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger("EshopSubstrings logger");

    /**
     * Initializes an EshopSubstrings instance with a list of normalized products.
     * Constructs a dictionary mapping substrings of product names to their respective product lists
     * and logs statistics about the dictionary's size and distribution.
     *
     * @param products the list of normalized products to be processed
     */
    public EshopSubstrings(List<NormalizedProduct> products) {
        this.products = products;
        for (NormalizedProduct product : products) {
            addSubstringsToDictionary(product);
        }

        consoleLogDictionarySizeStats();

        logEqualSubstringsMappingView();
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

    private void logEqualSubstringsMappingView() {
        String eshopName = products.getFirst().eshop.toString();
        createSubstringMappingDirectory(eshopName);
        StringBuilder substringWithInvalidFileName = new StringBuilder();

        for(String substring : substringsToProducts.keySet()) {
            StringBuilder sb = buildSubstringsMapping(substring);
            saveLogsToFile(sb, eshopName, substring, substringWithInvalidFileName);
        }

        String filePath = RuntimeConfig.substringsMappingDirectory + eshopName + "/invalidDubstrings/invalid.txt";
        createSubstringMappingDirectory(eshopName + "/invalidDubstrings");
        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write(substringWithInvalidFileName.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred with invalidDubstrings eshopSubstrings.logEqualSubstringsMappingView", e);
        }
    }

    private void createSubstringMappingDirectory(String eshopName) {
        File directory = new File(RuntimeConfig.substringsMappingDirectory + eshopName + "/");
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.log(Level.SEVERE, "Failed to create directory: " + RuntimeConfig.substringsMappingDirectory);
        }
    }

    private StringBuilder buildSubstringsMapping(String subString) {
        StringBuilder sb = new StringBuilder();
        for(NormalizedProduct product : substringsToProducts.get(subString)) {
            sb.append(product.name).append("\n");
        }
        return sb;
    }

    private void saveLogsToFile(StringBuilder sb, String eshopName, String substring, StringBuilder substringWithInvalidFileName) {
        String filePath = RuntimeConfig.substringsMappingDirectory + eshopName + "/" + substring + ".txt";
        try {
            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write(sb.toString());
            } catch (IOException e) {
                substringWithInvalidFileName.append(substring).append("\n");
            }
        } catch(InvalidPathException e) {
            substringWithInvalidFileName.append(substring).append("\n");
        }
    }
}
