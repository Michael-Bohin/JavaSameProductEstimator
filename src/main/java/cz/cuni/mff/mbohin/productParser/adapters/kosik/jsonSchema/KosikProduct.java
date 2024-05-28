package cz.cuni.mff.mbohin.productParser.adapters.kosik.jsonSchema;


import java.math.BigDecimal;

@SuppressWarnings("unused")
public class KosikProduct {
    public int id;
    public String name;
    public String image;
    public String url;
    public BigDecimal price;
    public int returnablePackagePrice;
    public String unit;
    public BigDecimal recommendedPrice;
    public int percentageDiscount;
    public Productquantity productQuantity;
    public Label[] labels;
    public String actionLabel;
    public String countryCode;
    public String[] pictographs;
    public int maxInCart;
    public Integer limitInCart;
    public Object firstOrderDay;
    public Object lastOrderDay;
    public Object plannedStock;
    public Object relatedProduct;
    public Maincategory mainCategory;
    public Priceperunit pricePerUnit;
    public Cumulativeprice[] cumulativePrices;
    public Object[] giftIds;
    public boolean favorite;
    public boolean purchased;
    public int unitStep;
    public int vendorId;
    public Object pharmacyCertificate;
    public Object[] productGroups;
    public float recommendedSellPrice;
    public Detail detail;
    public boolean hasAssociatedProducts;
    public boolean eLicence;
    public Object marketplaceVendor;
}
