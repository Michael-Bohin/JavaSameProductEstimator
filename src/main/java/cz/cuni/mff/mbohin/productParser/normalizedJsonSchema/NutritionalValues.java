package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;

import java.math.BigDecimal;

/**
 * Represents the nutritional values of a product, including energy, fats, carbohydrates, proteins, salt, and fiber.
 * This class ensures that all nutritional values are non-negative.
 */
public class NutritionalValues {
    private final int energetickaHodnotaKJ, energetickaHodnotaKCAL;
    private final BigDecimal tuky, zTohoNasyceneMastneKyseliny, sacharidy, zTohoCukry, bilkoviny, sul, vlaknina;

    /**
     * Constructs a NutritionalValues instance with the specified nutritional values.
     * Ensures that all provided values are non-negative.
     *
     * @param energetickaHodnotaKJ the energy value in kilojoules
     * @param energetickaHodnotaKCAL the energy value in kilocalories
     * @param tuky the total fat content
     * @param zTohoNasyceneMastneKyseliny the saturated fat content
     * @param sacharidy the total carbohydrate content
     * @param zTohoCukry the sugar content
     * @param bilkoviny the protein content
     * @param sul the salt content
     * @param vlaknina the fiber content
     * @throws IllegalArgumentException if any value is negative
     */
    public NutritionalValues(int energetickaHodnotaKJ, int energetickaHodnotaKCAL, BigDecimal tuky,
                             BigDecimal zTohoNasyceneMastneKyseliny, BigDecimal sacharidy, BigDecimal zTohoCukry,
                             BigDecimal bilkoviny, BigDecimal sul, BigDecimal vlaknina) {
        this.energetickaHodnotaKJ = energetickaHodnotaKJ;
        this.energetickaHodnotaKCAL = energetickaHodnotaKCAL;
        this.tuky = tuky;
        this.zTohoNasyceneMastneKyseliny = zTohoNasyceneMastneKyseliny;
        this.sacharidy = sacharidy;
        this.zTohoCukry = zTohoCukry;
        this.bilkoviny = bilkoviny;
        this.sul = sul;
        this.vlaknina = vlaknina;

        assertIsNonNegative(this.energetickaHodnotaKJ);
        assertIsNonNegative(this.energetickaHodnotaKCAL);
        assertIsNonNegative(this.tuky);
        assertIsNonNegative(this.zTohoNasyceneMastneKyseliny);
        assertIsNonNegative(this.sacharidy);
        assertIsNonNegative(this.zTohoCukry);
        assertIsNonNegative(this.bilkoviny);
        assertIsNonNegative(this.sul);
        assertIsNonNegative(this.vlaknina);
    }

    /**
     * Asserts that the specified BigDecimal value is non-negative.
     *
     * @param d the BigDecimal value to check
     * @throws IllegalArgumentException if the value is negative
     */
    private void assertIsNonNegative(BigDecimal d) {
        if (d.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Value cannot be negative: " + d);
    }

    /**
     * Asserts that the specified integer value is non-negative.
     *
     * @param i the integer value to check
     * @throws IllegalArgumentException if the value is negative
     */
    private void assertIsNonNegative(int i) {
        if (i < 0)
            throw new IllegalArgumentException("Value cannot be negative: " + i);
    }

    @Override
    public String toString() {
        return "Nutricni hodnoty na 100 g:\n" +
                "Energeticka hodnota KJ " + energetickaHodnotaKJ + "\n" +
                "Energeticka hodnota KCAL " + energetickaHodnotaKCAL + "\n" +
                "Tuky " + tuky + "\n" +
                "Mastne kyseliny " + zTohoNasyceneMastneKyseliny + "\n" +
                "Sacharidy " + sacharidy + "\n" +
                "Cukry " + zTohoCukry + "\n" +
                "Bilkoviny " + bilkoviny + "\n" +
                "Sul " + sul + "\n" +
                "Vlaknina " + vlaknina;
    }
}
