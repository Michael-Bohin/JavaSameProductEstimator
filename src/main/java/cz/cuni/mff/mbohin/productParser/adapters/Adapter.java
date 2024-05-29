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

    protected abstract boolean anyCriticalPropertyIsNull(T product);

    protected abstract NormalizedProduct unsafeParseNormalizedProduct(T product);

    protected abstract String getNameOf();

    protected abstract String getRelativeDataPath();

    protected abstract Eshop getEshopType();

    private static final Logger LOGGER = Logger.getLogger("Adapter<T> logger");

    public Adapter(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    public List<NormalizedProduct> getNormalizedProducts() throws IOException {
        String json = FileHandler.loadJsonFromPath(getRelativeDataPath());
        return parseNormalizedProducts(json);
    }

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
