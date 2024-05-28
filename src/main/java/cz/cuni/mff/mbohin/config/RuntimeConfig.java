package cz.cuni.mff.mbohin.config;

@SuppressWarnings("unused")
public class RuntimeConfig {
    public static final String kosikProductDataRelativePath = "./src/main/resources/kosikProductDataIndented.json";

    public final String rohlikZipesRelativePath = "./src/main/resources/rohlikProductData.zip";

    public static final String tescoProductDataRelativePath = "./src/main/resources/tescoProductData.json";

    public final String zipExtractPath = "./out/decompressedFiles/";
    public static final int limitProcessedProducts = 50;
}
