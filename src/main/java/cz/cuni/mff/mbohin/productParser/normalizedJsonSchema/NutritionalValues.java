package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;

import java.math.BigDecimal;

public class NutritionalValues {
    private final int energetickaHodnotaKJ, energetickaHodnotaKCAL;
    private final BigDecimal tuky, zTohoNasyceneMastneKyseliny, sacharidy, zTohoCukry, bilkoviny, sul, vlaknina;

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

    private void assertIsNonNegative(BigDecimal d) {
        if (d.compareTo(BigDecimal.ZERO) >= 0)
            throw new IllegalArgumentException("Value cannot be negative: " + d);
    }

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
