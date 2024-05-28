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

@SuppressWarnings("unused")
public class KosikAdapter extends Adapter<KosikJsonProduct> {
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

    private static String getStorageConditions(KosikJsonProduct product) {
        Supplierinfo[] list = product.product.detail.supplierInfo;
        if (list == null)
            return null;

        for (Supplierinfo info : list)
            if ("Skladovací podmínky".equals(info.title))
                return info.value;

        return null;
    }

    private static UnitType safeRetrieveUnitType(KosikJsonProduct jsonProduct) {
        String unitDesc = jsonProduct.product.unit;

        if (unitDesc == null)
            return null;

        if ("ks".equals(unitDesc))
            return UnitType.PIECES;

        return null;
    }

    private static NutritionalValues toNormalized(KosikNutritionalValues values) {
        if (values == null || values.values == null)
            return null;

        int energetickaKJ = 0, energetickaKCAL = 0;
        BigDecimal tuky = BigDecimal.ZERO, mastneKyseliny = BigDecimal.ZERO, sacharidy = BigDecimal.ZERO, cukry = BigDecimal.ZERO, bilkoviny = BigDecimal.ZERO, sul = BigDecimal.ZERO, vlaknina = BigDecimal.ZERO;

        for (KosikNutritionalValue value : values.values) {
            switch (value.title) {
                case "Energetická hodnota":
                    if ("kJ".equals(value.unit))
                        energetickaKJ = Integer.parseInt(value.value);
                    else if ("kcal".equals(value.unit))
                        energetickaKCAL = Integer.parseInt(value.value);
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
}

