package cz.cuni.mff.mbohin.productParser.adapters.tesco;

import cz.cuni.mff.mbohin.productParser.adapters.tesco.jsonSchema.Product;
import cz.cuni.mff.mbohin.productParser.adapters.tesco.jsonSchema.TescoJsonProduct;
import cz.cuni.mff.mbohin.productParser.adapters.Adapter;
import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.UnitType;

import java.math.BigDecimal;

public class TescoAdapter extends Adapter<TescoJsonProduct> {

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
        normalizedProduct.setWeight(null);
        normalizedProduct.setVolume(null);
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

