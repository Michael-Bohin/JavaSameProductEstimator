package cz.cuni.mff.mbohin.config;

@SuppressWarnings("unused")
public class RuntimeConfig {
    public final String kosikProductDataRelativePath = "./../../../ProductParser/ScrapedEshopData/kosikProductDataIndented.json";

    public final String rohlikZipesRelativePath = "./../../../ProductParser/ScrapedEshopData/rohlikProductData.zip";

    public static final String tescoProductDataRelativePath = "./src/main/resources/tescoProductData.json";

    public final String zipExtractPath = "./out/decompressedFiles/";
    public static final int limitProcessedProducts = 50;
}
