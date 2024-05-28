package cz.cuni.mff.mbohin.productParser.adapters.tesco.jsonSchema;

import java.time.LocalDateTime;

public class TescoPromotion {
    public String promotionId;
    public String promotionType;
    public LocalDateTime startDate;
    public LocalDateTime endDate;
    public Object unitSellingInfo;
    public String offerText;
    public Object price;
    public String[] attributes;
}

