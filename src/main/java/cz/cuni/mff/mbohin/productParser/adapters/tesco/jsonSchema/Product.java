package cz.cuni.mff.mbohin.productParser.adapters.tesco.jsonSchema;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class Product {
    public String typename;
    public Object context;
    public String id;
    public Object modelMetadata;
    public Object gtin;
    public Object adId;
    public String baseProductId;
    public String title;
    public Object seller;
    public Object brandName;
    public String shortDescription;
    public String defaultImageUrl;
    public String superDepartmentId;
    public String superDepartmentName;
    public String departmentId;
    public String departmentName;
    public String aisleId;
    public String aisleName;
    public Object shelfId;
    public Object shelfName;
    public String displayType;
    public String productType;
    public Object charges;
    public float averageWeight;
    public int bulkBuyLimit;
    public int maxQuantityAllowed;
    public int groupBulkBuyLimit;
    public Object bulkBuyLimitMessage;
    public Object bulkBuyLimitGroupId;
    public Object timeRestrictedDelivery;
    public Object restrictedDelivery;
    public boolean isForSale;
    public boolean isInFavourites;
    public Object isNew;
    public Object isRestrictedOrderAmendment;
    public String status;
    public Object maxWeight;
    public Object minWeight;
    public Object increment;
    public Object details;
    public TescoCatchweightlist[] catchWeightList;
    public Object[] restrictions;
    public BigDecimal price;
    public float unitPrice;
    public String unitOfMeasure;
    public Object[] substitutions;
}

