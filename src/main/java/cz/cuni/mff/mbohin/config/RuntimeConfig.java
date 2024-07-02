package cz.cuni.mff.mbohin.config;


/**
 * Provides configuration settings for the product parser system. This class contains constants used across
 * various parts of the application to configure and access product data. These settings include paths to
 * data files and operational parameters that influence the system's behavior during runtime.
 *
 * <p>Each public static final field in this class represents a specific configuration constant:</p>
 * <ul>
 * <li>{@code kosikProductDataRelativePath} - Path to the Kosik e-shop product data file in JSON format.</li>
 * <li>{@code rohlikZipesRelativePath} - Path to the Rohlik e-shop product data compressed file (ZIP).</li>
 * <li>{@code tescoProductDataRelativePath} - Path to the Tesco e-shop product data file in JSON format.</li>
 * <li>{@code zipExtractPath} - Directory path where ZIP files are extracted during processing.</li>
 * <li>{@code limitProcessedProducts} - The maximum number of products to process, which can be used to limit processing during development or testing.</li>
 * </ul>
 *
 * <p>This configuration class simplifies the management of path and operational settings, ensuring that they are centrally managed and easily accessible
 * throughout the application. Changing a setting here affects all components that rely on these paths or parameters.</p>
 */
public class RuntimeConfig {
    public static final String kosikProductDataRelativePath = "./src/main/resources/kosikProductDataIndented.json";

    public static final String rohlikZipesRelativePath = "./src/main/resources/rohlikProductData.zip";

    public static final String tescoProductDataRelativePath = "./src/main/resources/tescoProductData.json";

    public static final String zipExtractPath = "./out/decompressedFiles/";

    public static final String substringsMappingDirectory = "./out/substringsMappingView/";
    public static final int limitProcessedProducts = 50;
}
