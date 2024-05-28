package cz.cuni.mff.mbohin.productParser;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProductParserLogger {

    private static final String LOGS_PATH = "./out/devLogs/";

    private static String parserLogName(Eshop eshop) {
        return "parsed" + eshop + "Products.txt";
    }

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
