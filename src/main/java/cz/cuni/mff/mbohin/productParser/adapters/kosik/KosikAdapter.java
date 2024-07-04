package cz.cuni.mff.mbohin.productParser.adapters.kosik;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NutritionalValues;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema.KosikNutritionalValue;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema.KosikNutritionalValues;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema.KosikProduct;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema.Supplierinfo;
import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.UnitType;
import cz.cuni.mff.mbohin.productParser.adapters.Adapter;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema.KosikJsonProduct;

import java.math.BigDecimal;

/**
 * The KosikAdapter class extends the generic {@link Adapter} framework to specifically handle and process product data
 * from Kosik's e-shop. This class adapts Kosik's specific JSON schema into a unified format defined by the
 * {@link NormalizedProduct} class, ensuring that the product data can be seamlessly integrated and utilized across
 * various systems and platforms.
 *
 * <p>The adapter focuses on handling essential product attributes such as URLs, pricing, names, and detailed descriptions,
 * and it ensures that each product data attribute is adequately validated and parsed into a standardized form. Special attention
 * is given to handling nutritional values and storage conditions, which may involve complex data structures and require detailed parsing logic.</p>
 *
 * <p>This class provides concrete implementations for abstract methods defined in the {@link Adapter} class to:
 * - Validate critical product properties to ensure data integrity.
 * - Parse JSON data into a structured {@link NormalizedProduct} format.
 * - Convert product-related information like units and nutritional values into more generic, system-wide comprehensible formats.</p>
 *
 * <p>By leveraging detailed JSON data and transforming it into standardized formats, the KosikAdapter facilitates robust data integration
 * and helps maintain consistency and reliability in data handling and presentation across different e-commerce platforms.</p>
 *
 * @see Adapter
 * @see NormalizedProduct
 * @see KosikJsonProduct
 */
public class KosikAdapter extends Adapter<KosikJsonProduct> {
    /**
     * Constructs a new KosikAdapter.
     * Initializes the adapter with KosikJsonProduct as the type parameter class.
     */
    public KosikAdapter() {
        super(KosikJsonProduct.class);
    }

    @Override
    protected String getNameOf() {
        return "KosikAdapter";
    }

    @Override
    protected String getRelativeDataPath() {
        return RuntimeConfig.kosikProductDataRelativePath;
    }

    @Override
    protected Eshop getEshopType() {
        return Eshop.KOSIK;
    }

    @Override
    protected boolean anyCriticalPropertyIsNull(KosikJsonProduct product) {
        if (product == null || product.product == null)
            return true;

        KosikProduct p = product.product;
        return p.url == null || p.price == null || p.name == null;
    }

    @Override
    protected NormalizedProduct unsafeParseNormalizedProduct(KosikJsonProduct kosikProduct) {
        KosikProduct p = kosikProduct.product;
        String name = p.name;
        String url = "www.kosik.cz" + p.url;
        BigDecimal price = p.price;

        NormalizedProduct normalizedProduct = new NormalizedProduct(name, url, price, Eshop.KOSIK);
        normalizedProduct.producer = kosikProduct.product.detail.brand != null ? kosikProduct.product.detail.brand.name : null;
        normalizedProduct.description = kosikProduct.product.detail.description != null && kosikProduct.product.detail.description.length > 0 ? kosikProduct.product.detail.description[0].value : null;
        normalizedProduct.storageConditions = getStorageConditions(kosikProduct);
        normalizedProduct.unitType = safeRetrieveUnitType(kosikProduct);
        normalizedProduct.pieces = 1;
        normalizedProduct.weight = null;
        normalizedProduct.volume = null;
        normalizedProduct.nutritionalValues = toNormalized(kosikProduct.product.detail.nutritionalValues);

        return normalizedProduct;
    }

    /**
     * Extracts storage conditions from the product's supplier information.
     *
     * @param product the KosikJsonProduct to extract storage conditions from
     * @return the storage conditions if found, otherwise null
     */
    private static String getStorageConditions(KosikJsonProduct product) {
        Supplierinfo[] list = product.product.detail.supplierInfo;
        if (list == null)
            return null;

        for (Supplierinfo info : list)
            if ("Skladovací podmínky".equals(info.title))
                return info.value;

        return null;
    }

    /**
     * Safely retrieves the unit type from the KosikJsonProduct.
     *
     * @param jsonProduct the KosikJsonProduct to retrieve the unit type from
     * @return the corresponding UnitType, or null if none found
     */
    private static UnitType safeRetrieveUnitType(KosikJsonProduct jsonProduct) {
        String unitDesc = jsonProduct.product.unit;

        if (unitDesc == null)
            return null;

        if ("ks".equals(unitDesc))
            return UnitType.PIECES;

        return null;
    }

    /**
     * Converts Kosik nutritional values to a normalized format.
     *
     * @param values the Kosik nutritional values to convert
     * @return a NutritionalValues object with normalized data, or null if values are null
     */
    private static NutritionalValues toNormalized(KosikNutritionalValues values) {
        if (values == null || values.values == null)
            return null;

        int energetickaKJ = 0, energetickaKCAL = 0;
        BigDecimal tuky = BigDecimal.ZERO, mastneKyseliny = BigDecimal.ZERO, sacharidy = BigDecimal.ZERO, cukry = BigDecimal.ZERO, bilkoviny = BigDecimal.ZERO, sul = BigDecimal.ZERO, vlaknina = BigDecimal.ZERO;

        for (KosikNutritionalValue value : values.values) {
            switch (value.title) {
                case "Energetická hodnota":
                    if ("kJ".equals(value.unit))
                        energetickaKJ = parseStringToInt(value.value);
                    else if ("kcal".equals(value.unit))
                        energetickaKCAL = parseStringToInt(value.value);
                    break;
                case "Tuky":
                    tuky = new BigDecimal(value.value);
                    break;
                case "Z toho nasycené mastné kyseliny":
                    mastneKyseliny = new BigDecimal(value.value);
                    break;
                case "Sacharidy":
                    sacharidy = new BigDecimal(value.value);
                    break;
                case "Z toho cukry":
                    cukry = new BigDecimal(value.value);
                    break;
                case "Bílkoviny":
                    bilkoviny = new BigDecimal(value.value);
                    break;
                case "Sůl":
                    sul = new BigDecimal(value.value);
                    break;
                case "Vláknina":
                    vlaknina = new BigDecimal(value.value);
                    break;
            }
        }

        return new NutritionalValues(energetickaKJ, energetickaKCAL, tuky, mastneKyseliny, sacharidy, cukry, bilkoviny, sul, vlaknina);
    }

    /**
     * Parses a string to an integer, returning 0 if parsing fails.
     *
     * @param stringValue the string to parse
     * @return the parsed integer, or 0 if parsing fails
     */
    private static int parseStringToInt(String stringValue) {
        try {
            return Integer.parseInt(stringValue.split("\\.")[0]);
        } catch (NumberFormatException e) {
            return 0;  // Return default value if parsing fails, usually in our data set when running into a string that represents double value
        }
    }
}

