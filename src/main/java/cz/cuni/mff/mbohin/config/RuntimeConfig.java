package cz.cuni.mff.mbohin.config;

public class RuntimeConfig {
    public final String kosikProductDataRelativePath = "./../../../ProductParser/ScrapedEshopData/kosikProductDataIndented.json",

    rohlikZipesRelativePath = "./../../../ProductParser/ScrapedEshopData/rohlikProductData.zip",

    tescoProductDataRelativePath = "./../../../ProductParser/ScrapedEshopData/tescoProductData.json",

    zipExtractPath = "./out/decompressedFiles/";
    public static final int limitProcessedProducts = 50;
}
