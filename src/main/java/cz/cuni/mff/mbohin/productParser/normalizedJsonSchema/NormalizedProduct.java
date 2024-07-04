package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;

import java.util.Optional;
import java.math.BigDecimal;

/**
 * Represents a normalized view of a product, abstracting various details into a unified format that facilitates processing and integration
 * across different e-commerce platforms. This class consolidates essential product information such as name, URL, price, and shop into a
 * standardized structure while also providing fields for additional details like producer, description, and storage conditions.
 *
 * <p>Instances of this class ensure data integrity by validating that essential strings and price values are not null, empty, or negative,
 * respectively. The class also supports optional properties that can enhance product information like weight, volume, and nutritional values,
 * each handled through optional fields or methods.</p>
 *
 *
 * <p>Key properties include:</p>
 * <ul>
 * <li>{@code name} - The product name, which is validated upon creation.</li>
 * <li>{@code url} - The URL to the product page, validated to ensure it is not null or empty.</li>
 * <li>{@code price} - The price of the product, must be a non-negative value.</li>
 * <li>{@code eshop} - The e-shop from which the product originates, indicating the source platform.</li>
 * <li>{@code description}, {@code producer}, {@code storageConditions} - Optional details about the product that can be set after instantiation.</li>
 * <li>{@code unitType}, {@code pieces}, {@code weight}, {@code volume} - Optional units of measurement and quantity details.</li>
 * <li>{@code nutritionalValues} - Nutritional information which can be associated with food items.</li>
 * </ul>
 *
 * <p>Usage of this class allows for the normalization of data where various attributes of products from different sources are standardized,
 * thus simplifying data management and integration tasks.</p>
 */
public class NormalizedProduct {
    public final String name, url;
    public final BigDecimal price;
    public final Eshop eshop;

    public String producer;
    public String description;
    public String storageConditions;

    public UnitType unitType;
    public Integer pieces;
    public Double weight, volume;

    public NutritionalValues nutritionalValues;

    public final InferredData inferredData;

    /**
     * Constructs a NormalizedProduct instance with the specified name, URL, price, and e-shop.
     * Ensures that name and URL are not null or empty and that price is non-negative.
     *
     * @param name the name of the product
     * @param url the URL to the product page
     * @param price the price of the product
     * @param eshop the e-shop from which the product originates
     * @throws IllegalArgumentException if name or URL are null or empty, or if price is negative
     */
    public NormalizedProduct(String name, String url, BigDecimal price, Eshop eshop) {
        this.name = assertStringIsNotNullOrEmpty(name);
        this.url = assertStringIsNotNullOrEmpty(url);
        this.eshop = eshop;

        if (price.compareTo(BigDecimal.ZERO) < 0)
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

    /**
     * Sets the description of the product.
     *
     * @param description the description of the product
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Sets the producer of the product.
     *
     * @param producer the producer of the product
     */
    public void setProducer(String producer) { this.producer = producer; }

    /**
     * Sets the storage conditions of the product.
     *
     * @param conditions the storage conditions of the product
     */
    public void setStorageConditions(String conditions) { this.storageConditions = conditions; }

    /**
     * Sets the number of pieces for the product and updates the unit type to PIECES.
     *
     * @param pieces the number of pieces
     */
    public void setPieces(int pieces) {
        this.unitType = UnitType.PIECES;
        this.pieces = pieces;
    }

    /**
     * Sets the weight of the product and updates the unit type to WEIGHT.
     *
     * @param weight the weight of the product
     */
    @SuppressWarnings("unused")
    public void setWeight(Double weight) {
        this.unitType = UnitType.WEIGHT;
        this.weight = weight;
    }

    /**
     * Sets the volume of the product and updates the unit type to VOLUME.
     *
     * @param volume the volume of the product
     */
    @SuppressWarnings("unused")
    public void setVolume(Double volume) {
        this.unitType = UnitType.VOLUME;
        this.volume = volume;
    }
}