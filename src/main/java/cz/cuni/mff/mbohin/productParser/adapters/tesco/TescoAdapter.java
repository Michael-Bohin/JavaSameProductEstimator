package cz.cuni.mff.mbohin.productParser.adapters.tesco;

import cz.cuni.mff.mbohin.productParser.adapters.tesco.jsonSchema.Product;
import cz.cuni.mff.mbohin.productParser.adapters.tesco.jsonSchema.TescoJsonProduct;
import cz.cuni.mff.mbohin.productParser.adapters.Adapter;
import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.UnitType;

import java.math.BigDecimal;

/**
 * An implementation of the {@link Adapter} class specific to Tesco's e-shop. The TescoAdapter handles the parsing
 * and normalization of product data from Tesco-specific JSON structures into a common format defined by the {@link NormalizedProduct} class.
 * This class is designed to facilitate the integration of Tesco product data into a larger system that may handle multiple types of e-shop data formats.
 *
 * <p>The TescoAdapter extends the generic functionality provided by the {@link Adapter} abstract class and implements all necessary abstract methods
 * to cater to the specific requirements of Tesco product data. These implementations include methods to check for critical null fields,
 * parse product details into a normalized structure, and provide identifiers for logging and data handling purposes specific to Tesco.</p>
 *
 * <p>This adapter utilizes JSON parsing to convert raw product data from Tesco into structured data ready for various applications such as e-commerce platforms,
 * price comparison tools, or inventory management systems. It ensures that all critical components of the data such as product name, ID, and price are validated and
 * properly handled to maintain data integrity and usability.</p>
 */
public class TescoAdapter extends Adapter<TescoJsonProduct> {

    /**
     * Constructs a new TescoAdapter.
     * TescoJsonProduct.class reference is passed to its generic predecessor to be used in Jackson JSON deserialization.
     */
    public TescoAdapter() {
        super(TescoJsonProduct.class);
    }

    @Override
    protected String getNameOf() {
        return "TescoAdapter";
    }

    @Override
    protected String getRelativeDataPath() {
        return RuntimeConfig.tescoProductDataRelativePath;
    }

    @Override
    protected Eshop getEshopType() {
        return Eshop.TESCO;
    }

    @Override
    protected boolean anyCriticalPropertyIsNull(TescoJsonProduct tescoProduct) {
        if (tescoProduct == null || tescoProduct.product == null) {
            return true;
        }

        Product p = tescoProduct.product;
        return p.title == null || p.id == null || p.price == null;
    }

    @Override
    protected NormalizedProduct unsafeParseNormalizedProduct(TescoJsonProduct tescoProduct) {
        Product p = tescoProduct.product;
        String name = p.title;
        String url = "https://nakup.itesco.cz/groceries/cs-CZ/products/" + p.id;
        BigDecimal price = p.price;  // Assuming BigDecimal is used for currency in Java

        NormalizedProduct normalizedProduct = new NormalizedProduct(name, url, price, Eshop.TESCO);
        normalizedProduct.setProducer(null);  // information is absent in the webscraped data
        normalizedProduct.setDescription(p.shortDescription);
        normalizedProduct.setStorageConditions(null);  // information is absent in the webscraped data
        normalizedProduct.unitType = parseUnitType(tescoProduct);
        normalizedProduct.setPieces(1);
        // normalizedProduct.setWeight(null);
        // normalizedProduct.setVolume(null);
        normalizedProduct.nutritionalValues = null;  // nutritional values need to be implemented

        return normalizedProduct;
    }

    private UnitType parseUnitType(TescoJsonProduct product) {
        if (product.product.unitOfMeasure == null) {
            return null;
        }

        String unit = product.product.unitOfMeasure;
        if ("kg".equals(unit)) {
            return UnitType.WEIGHT;
        }
        return UnitType.OSTATNI;
    }
}

