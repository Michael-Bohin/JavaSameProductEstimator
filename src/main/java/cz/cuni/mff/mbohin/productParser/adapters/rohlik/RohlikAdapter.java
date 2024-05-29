package cz.cuni.mff.mbohin.productParser.adapters.rohlik;

import cz.cuni.mff.mbohin.productParser.adapters.rohlik.jsonSchema.RohlikJsonProduct;
import cz.cuni.mff.mbohin.productParser.adapters.rohlik.jsonSchema.Price;
import cz.cuni.mff.mbohin.productParser.adapters.Adapter;
import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.UnitType;

import java.math.BigDecimal;

/**
 * The RohlikAdapter class extends the generic {@link Adapter} to handle the specific requirements and data format of Rohlik's e-shop.
 * It processes Rohlik-specific JSON product data into a normalized format that can be used across various systems or platforms.
 * This adapter focuses on parsing critical product attributes such as name, URL, and price while managing potential additional details
 * like brand and storage conditions, which may require specialized handling due to their format (e.g., HTML content).
 *
 * <p>Implementations of abstract methods in this class ensure the integrity and usability of Rohlik's data by checking for null values in essential fields,
 * converting HTML descriptions to plain text where necessary, and accurately categorizing product units. The class uses {@link RohlikJsonProduct}
 * as its type parameter to facilitate type-safe data handling and parsing within the Rohlik e-commerce framework.</p>
 *
 * <p>This class specifically provides functionality to:
 * - Validate product data integrity.
 * - Parse JSON data to a normalized structure.
 * - Log and handle data relative to the Rohlik e-shop configuration.
 * - Convert product unit types from specific codes to a generalized enum format for better usability in a diverse system environment.</p>
 *
 * @see Adapter
 * @see NormalizedProduct
 * @see RohlikJsonProduct
 */
public class RohlikAdapter extends Adapter<RohlikJsonProduct> {

    public RohlikAdapter() {
        super(RohlikJsonProduct.class);
    }

    @Override
    protected String getNameOf() {
        return "RohlikAdapter";
    }

    @Override
    protected String getRelativeDataPath() {
        return RuntimeConfig.rohlikZipesRelativePath;
    }

    @Override
    protected Eshop getEshopType() {
        return Eshop.ROHLIK;
    }

    @Override
    protected boolean anyCriticalPropertyIsNull(RohlikJsonProduct product) {
        if (product == null || product.name == null || product.url == null || product.price == null)
            return true;

        Price p = product.price;
        return p.amount == null || p.currency == null;
    }

    @Override
    protected NormalizedProduct unsafeParseNormalizedProduct(RohlikJsonProduct rohlikProduct) {
        String name = rohlikProduct.name;
        String url = rohlikProduct.url;
        BigDecimal price = rohlikProduct.price.amount;

        NormalizedProduct normalizedProduct = new NormalizedProduct(name, url, price, Eshop.ROHLIK);
        normalizedProduct.producer = rohlikProduct.brand;
        normalizedProduct.description = rohlikProduct.htmlDescription; // zde bude potreba vyzkum jakym regexpem prevest z html na porovnatelny text
        normalizedProduct.storageConditions = null; // zde bude potreba vyzkum jakym regexpem vytahnout skladovaci podminky z htmlDescription, rohlik tuhle informaci nema v samostatnem fieldu
        normalizedProduct.unitType = parseUnitType(rohlikProduct);
        normalizedProduct.pieces = 1;
        normalizedProduct.weight = null;
        normalizedProduct.volume = null;
        normalizedProduct.nutritionalValues = null;

        return normalizedProduct;
    }

    private static UnitType parseUnitType(RohlikJsonProduct product) {
        if (product.unit == null)
            return null;

        if ("kg".equals(product.unit))
            return UnitType.WEIGHT;
        if ("ks".equals(product.unit))
            return UnitType.PIECES;
        if ("l".equals(product.unit))
            return UnitType.VOLUME;
        if ("krabička".equals(product.unit))
            return UnitType.KRABICKA;

        return UnitType.OSTATNI;
    }
}
