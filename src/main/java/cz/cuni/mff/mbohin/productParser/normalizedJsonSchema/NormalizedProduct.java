package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;

import java.util.Optional;

public class NormalizedProduct {
    private final String name;
    private final String url;
    private final double price;
    public final Eshop eshop;

    private String producer;
    private String description;
    private String storageConditions;

    private UnitType unitType;
    private Integer pieces;
    private Double weight;
    private Double volume;

    private NutritionalValues nutritionalValues;

    public final InferredData inferredData;

    public NormalizedProduct(String name, String url, double price, Eshop eshop) {
        this.name = assertStringIsNotNullOrEmpty(name);
        this.url = assertStringIsNotNullOrEmpty(url);
        this.eshop = eshop;

        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        this.price = price;

        this.inferredData = new InferredData(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(price).append("\n");
        sb.append(eshop).append("\n");
        sb.append(url).append("\n");
        Optional.ofNullable(description).ifPresent(sb::append);
        Optional.ofNullable(producer).ifPresent(sb::append);
        Optional.ofNullable(storageConditions).ifPresent(sb::append);

        Optional.ofNullable(unitType).ifPresent(sb::append);
        Optional.ofNullable(pieces).ifPresent(sb::append);
        Optional.ofNullable(weight).ifPresent(sb::append);
        Optional.ofNullable(volume).ifPresent(sb::append);

        Optional.ofNullable(nutritionalValues).ifPresent(sb::append);

        return sb.toString();
    }

    private static String assertStringIsNotNullOrEmpty(String s) {
        if (s == null || s.isEmpty())
            throw new IllegalArgumentException("String cannot be null or empty: " + s);
        return s;
    }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setProducer(String producer) { this.producer = producer; }
    public void setStorageConditions(String conditions) { this.storageConditions = conditions; }
    public void setPieces(int pieces) {
        if (this.unitType != null) {
            throw new IllegalStateException("Unit type has been attempted to be set twice.");
        }
        this.unitType = UnitType.PIECES;
        this.pieces = pieces;
    }
    public void setWeight(double weight) {
        if (this.unitType != null) {
            throw new IllegalStateException("Unit type has been attempted to be set twice.");
        }
        this.unitType = UnitType.WEIGHT;
        this.weight = weight;
    }
    public void setVolume(double volume) {
        if (this.unitType != null) {
            throw new IllegalStateException("Unit type has been attempted to be set twice.");
        }
        this.unitType = UnitType.VOLUME;
        this.volume = volume;
    }
}