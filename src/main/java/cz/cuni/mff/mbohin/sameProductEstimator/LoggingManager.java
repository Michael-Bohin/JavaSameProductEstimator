package cz.cuni.mff.mbohin.sameProductEstimator;

import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.Eshop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Random;
import java.util.TreeMap;

/**
 * The LoggingManager class handles logging for the EqualProductsFinder class.
 * It provides methods to delete text files in a specified directory, log statistical information
 * about candidate frequencies, and log sorted candidate products based on various similarity measures.
 * The class also ensures that file paths for logs are unique to prevent overwriting existing files.
 * Key functionalities include:
 * - Deleting old log files to prepare directories for new logs.
 * - Logging detailed statistical information about product comparisons.
 * - Logging sorted candidate products for further analysis.
 * The class uses the Java Logger API to handle logging errors and important information.
 */
public class LoggingManager {
    private static final Logger LOGGER = Logger.getLogger("EqualProductsFinder logger");
    private static final String loggingDirectory = "./out/equalProductsFinder/";

    /**
     * Asserts that the specified string parameter is indeed a directory, and deletes all files with a ".txt" extension.
     * It checks if the directory exists and is a directory, then finds and deletes all ".txt" files within.
     * If the path is not valid or not a directory, it prints an error message.
     *
     * @param directoryPath the path to the directory where text files will be deleted
     */
    public static void deleteTextFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    assert !file.isFile() || file.delete();
                }
            }
        } else {
            System.out.println("The provided path does not exist or is not a directory.");
        }
    }

    /**
     * Logs statistical information about the frequency of equal candidates found between two e-shops.
     * This method writes detailed statistics to a log file, including the frequency distribution of equal candidates,
     * average candidates per product, and the percentage of candidates relative to the total possible pairs.
     *
     * @param equalCandidatesFrequencies a TreeMap where keys are the number of equal candidates and values are their frequencies
     * @param smallerEshop an instance of EshopSubstrings representing the e-shop with fewer products
     * @param largerEshop an instance of EshopSubstrings representing the e-shop with more products
     */
    public static void logStatsOfCandidates(TreeMap<Integer, Integer> equalCandidatesFrequencies, EshopSubstrings smallerEshop, EshopSubstrings largerEshop) {
        Eshop smallerName = smallerEshop.products.getFirst().eshop;
        Eshop largerName = largerEshop.products.getFirst().eshop;

        String filePath = loggingDirectory + "candidatesStats" + smallerName + "_to_" + largerName + ".txt";
        try (PrintWriter sw = new PrintWriter(filePath)) {
            sw.println("Equal candidates frequencies of " + smallerName + " -> " + largerName);

            Statistics statistics = new Statistics(equalCandidatesFrequencies, smallerEshop.products.size(), largerEshop.products.size());
            sw.println(statistics.getFormattedStatistics());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logStatsOfCandidates", e);
        }
    }

    /**
     * Logs the sorted list of candidate products based on their similarity to a product from a smaller e-shop.
     * This method constructs a directory path based on the product and similarity type, creates the directory if it does not exist,
     * and logs details of the similarity comparison into a file within this directory. Each entry includes the similarity score,
     * candidate product name, and URL. If the directory cannot be created or a file writing error occurs, it logs a severe error.
     *
     * @param similarityType the type of similarity by which the candidates are sorted (e.g., "substringSimilarity")
     * @param product the reference product from the smaller e-shop
     * @param largerEshop the larger e-shop containing candidate products
     * @param sortedCandidates a list of candidates sorted by the specified similarity type, each paired with their similarity score
     */
    public static void logSortedCandidates(String similarityType, NormalizedProduct product, EshopSubstrings largerEshop, List<SimilarityCandidatePair> sortedCandidates) {
        NormalizedProduct largerName = largerEshop.products.getFirst();
        String directoryPath = loggingDirectory + product.eshop + "_to_" + largerName.eshop + "/" + similarityType + "/";
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            LOGGER.log(Level.SEVERE, "Failed to create directory: " + directoryPath);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Equal candidates of ").append(product.name).append(", to be found at url: ").append(product.url).append("\n");

        for (SimilarityCandidatePair candidate : sortedCandidates) {
            sb.append(String.format("%.4f\t%s\t%s\n", candidate.similarity(), candidate.candidate().name, candidate.candidate().url));
        }

        String uniqueFilePath = ensureUniqueFilePath(directoryPath, product.inferredData.getUniqueFileName());

        try (FileWriter fw = new FileWriter(uniqueFilePath)) {
            fw.write(sb.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred in logSortedCandidates", e);
        }
    }

    /**
     * Ensures that the file path is unique within the specified directory by appending a random number to the filename if necessary.
     * It checks if a file with the given name already exists in the directory; if it does, it generates a new filename with a random number
     * appended to the base filename until a unique filename is obtained. The method finally returns the path of this unique file.
     *
     * @param directory the directory in which to check for uniqueness of the file
     * @param filename the initial filename (without an extension) to use for creating the file
     * @return the unique file path as a String
     */
    private static String ensureUniqueFilePath(String directory, String filename) {
        File file = new File(directory, filename + ".txt");
        while (file.exists()) {
            int randomNumber = new Random().nextInt(Integer.MAX_VALUE) + 1;
            file = new File(directory, filenameWithoutExtension(filename) + "_" + randomNumber + ".txt");
        }
        return file.getPath();
    }

    private static String filenameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }
}
