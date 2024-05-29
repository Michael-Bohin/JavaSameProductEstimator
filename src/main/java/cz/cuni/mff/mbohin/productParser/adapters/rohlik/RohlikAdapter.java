package cz.cuni.mff.mbohin.productParser.adapters.rohlik;

import cz.cuni.mff.mbohin.productParser.adapters.rohlik.jsonSchema.RohlikJsonProduct;
import cz.cuni.mff.mbohin.productParser.adapters.rohlik.jsonSchema.Price;
import cz.cuni.mff.mbohin.productParser.adapters.Adapter;
import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.UnitType;

import java.math.BigDecimal;

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
