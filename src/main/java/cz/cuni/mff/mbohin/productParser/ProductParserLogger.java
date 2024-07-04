package cz.cuni.mff.mbohin.productParser;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles logging of parsed products to a file for development and debugging purposes.
 * The logs are stored in a designated directory and are categorized by e-shop.
 */
public class ProductParserLogger {

    private static final String LOGS_PATH = "./out/devLogs/";

    /**
     * Generates the log file name for the specified e-shop.
     *
     * @param eshop the e-shop for which the log file name is generated
     * @return the log file name as a string
     */
    private static String parserLogName(Eshop eshop) {
        return "parsed" + eshop + "Products.txt";
    }

    /**
     * Logs the list of normalized products to a file specific to the given e-shop.
     * The log files are stored in a directory structure that is created if it does not exist.
     *
     * @param products the list of normalized products to be logged
     * @param eshop the e-shop to which the products belong
     */
    public static void log(List<NormalizedProduct> products, Eshop eshop) {
        Path logPath = Paths.get(LOGS_PATH + parserLogName(eshop));
        try {
            Files.createDirectories(logPath.getParent());  // Ensure directories are created
            try (BufferedWriter writer = Files.newBufferedWriter(logPath)) {
                for (NormalizedProduct product : products) {
                    writer.write(product.toString() + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
