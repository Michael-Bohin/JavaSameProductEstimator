package cz.cuni.mff.mbohin.productParser.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;

import cz.cuni.mff.mbohin.productParser.ProductParserLogger;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a framework for adapting product json schemas of online eshops data into a normalized format across different e-shops.
 * This abstract class defines a template for processing and normalizing data specific to each e-shop by implementing several
 * abstract methods that handle critical property checks, parsing, and data retrieval. It leverages generics to handle any type
 * of product model while ensuring type safety and reducing code redundancy. The class also handles deserialization of JSON data
 * into product models, management of data normalization, and logging of the results.
 *
 * <p>Each concrete adapter class must define the specific behaviors for:
 * - Checking critical properties of the product model to ensure they are not null.
 * - Parsing the product model into a standardized format used across different e-shops.
 * - Retrieving the name of the adapter, which typically corresponds to the e-shop's name.
 * - Defining the relative path to the data source.
 * - Determining the type of e-shop represented by the adapter.</p>
 *
 * @param <T> the type of product model that this adapter will process
 */
public abstract class Adapter<T> {
    private final Class<T> typeParameterClass;
    private static final Logger LOGGER = Logger.getLogger("Adapter<T> logger");

    /**
     * Constructs an Adapter instance for the specified type of product model.
     *
     * @param typeParameterClass the class type of the product model
     */
    public Adapter(Class<T> typeParameterClass) {

        this.typeParameterClass = typeParameterClass;
    }

    /**
     * Checks if any critical property of the given product is null.
     *
     * @param product the product to check
     * @return true if any critical property is null, false otherwise
     */
    protected abstract boolean anyCriticalPropertyIsNull(T product);

    /**
     * Parses the given product into a normalized product format.
     * This method is not safe and should be used with caution.
     *
     * @param product the product to parse
     * @return the normalized product
     */
    protected abstract NormalizedProduct unsafeParseNormalizedProduct(T product);

    /**
     * Retrieves the name of the adapter.
     *
     * @return the name of the adapter
     */
    protected abstract String getNameOf();

    /**
     * Retrieves the relative path to the data source.
     *
     * @return the relative data path
     */
    protected abstract String getRelativeDataPath();

    /**
     * Retrieves the type of e-shop represented by the adapter.
     *
     * @return the e-shop type
     */
    protected abstract Eshop getEshopType();

    /**
     * Retrieves the list of normalized products by loading and parsing JSON data from the data source.
     *
     * @return the list of normalized products
     * @throws IOException if an I/O error occurs during data loading
     */
    public List<NormalizedProduct> getNormalizedProducts() throws IOException {
        String json = FileHandler.loadJsonFromPath(getRelativeDataPath());
        return parseNormalizedProducts(json);
    }

    /**
     * Retrieves the list of normalized products by loading and parsing JSON data from a specified zip extract path.
     *
     * @param zipExtractPath the path to the zip extract directory
     * @return the list of normalized products
     * @throws IOException if an I/O error occurs during data loading
     */
    @SuppressWarnings("unused")
    public List<NormalizedProduct> getNormalizedProducts(String zipExtractPath) throws IOException {
        String json = FileHandler.loadJsonFromPath(getRelativeDataPath(), zipExtractPath);
        return parseNormalizedProducts(json);
    }

    private List<NormalizedProduct> parseNormalizedProducts(String json) {
        List<T> jsonProducts = deserializeProducts(json);
        Pair<List<NormalizedProduct>, List<T>> productsPair = processProducts(jsonProducts);

        logProductCounts(productsPair.getKey(), jsonProducts.size(), productsPair.getValue().size());
        return productsPair.getKey();
    }

    /**
     * Deserializes JSON data into a list of product models.
     *
     * @param json the JSON data to deserialize
     * @return the list of deserialized product models
     */
    public List<T> deserializeProducts(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unused") // json, new TypeReference<>(){}
            List<T> deserializedProducts = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass));
            return deserializedProducts;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logStatsOfCandidates", e);
            return null; // or handle the exception as per your requirement
        }
    }

    private Pair<List<NormalizedProduct>, List<T>> processProducts(List<T> products) {
        List<NormalizedProduct> normalizedProducts = new ArrayList<>();
        List<T> invalidProducts = new ArrayList<>();

        for (T product : products) {
            Pair<Boolean, NormalizedProduct> result = tryGetNormalized(product);
            if (result.getKey()) {
                normalizedProducts.add(result.getValue());
            } else {
                invalidProducts.add(product);
            }
        }

        return new Pair<>(normalizedProducts, invalidProducts);
    }

    private Pair<Boolean, NormalizedProduct> tryGetNormalized(T product) {
        if (anyCriticalPropertyIsNull(product)) {
            return new Pair<>(false, null);
        } else {
            NormalizedProduct normalizedProduct = unsafeParseNormalizedProduct(product);
            return new Pair<>(true, normalizedProduct);
        }
    }

    private void logProductCounts(List<NormalizedProduct> normalizedProducts, int total, int invalidCount) {
        ProductParserLogger.log(normalizedProducts, getEshopType());

        System.out.println(getNameOf());
        System.out.println(total);
        System.out.println("Normalized products: " + normalizedProducts.size());
        System.out.println("Invalid products: " + invalidCount);
        System.out.println(normalizedProducts.size() + " + " + invalidCount + " = " + total + "\n");
    }
}
