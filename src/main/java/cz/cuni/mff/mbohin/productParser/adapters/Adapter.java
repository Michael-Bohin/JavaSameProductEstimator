package cz.cuni.mff.mbohin.productParser.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import cz.cuni.mff.mbohin.productParser.ProductParserLogger;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public abstract class Adapter<T> {

    protected abstract boolean anyCriticalPropertyIsNull(T product);

    protected abstract NormalizedProduct unsafeParseNormalizedProduct(T product);

    protected abstract String getNameOf();

    protected abstract String getRelativeDataPath();

    protected abstract Eshop getEshopType();

    public List<NormalizedProduct> getNormalizedProducts() throws IOException {
        String json = FileHandler.loadJsonFromPath(getRelativeDataPath());
        return parseNormalizedProducts(json);
    }

    public List<NormalizedProduct> getNormalizedProducts(String zipExtractPath) throws IOException {
        String json = FileHandler.loadJsonFromPath(getRelativeDataPath(), zipExtractPath);
        return parseNormalizedProducts(json);
    }

    private List<NormalizedProduct> parseNormalizedProducts(String json) throws IOException {
        List<T> jsonProducts = deserializeProducts(json);
        Pair<List<NormalizedProduct>, List<T>> productsPair = processProducts(jsonProducts);

        logProductCounts(productsPair.getKey(), jsonProducts.size(), productsPair.getValue().size());
        return productsPair.getKey();
    }

    private List<T> deserializeProducts(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<T>> typeRef = new TypeReference<>() {};
        return mapper.readValue(json, typeRef);
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
