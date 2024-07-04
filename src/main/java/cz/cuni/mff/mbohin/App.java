package cz.cuni.mff.mbohin;

import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.adapters.rohlik.RohlikAdapter;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.KosikAdapter;
import cz.cuni.mff.mbohin.productParser.adapters.tesco.TescoAdapter;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder.EqualProductsFinder;

import java.io.IOException;
import java.util.List;

/**
 * The main class of the application, responsible for initiating the process of parsing and comparing products
 * from different e-shops (Kosik, Tesco, and Rohlik). It coordinates the normalization of products,
 * the identification of probable equal products, and the timing of the entire operation.
 */
public class App 
{
    /**
     * The main method that serves as the entry point for the application.
     * It performs the following tasks:
     * <ul>
     * <li>Parses products from Kosik, Tesco, and Rohlik e-shops using respective adapters.</li>
     * <li>Initializes the EqualProductsFinder with the parsed products.</li>
     * <li>Asynchronously sorts and identifies probable equal products.</li>
     * <li>Measures and prints the duration of the operation.</li>
     * </ul>
     *
     * @param args command-line arguments (not used)
     * @throws IOException if an I/O error occurs during product parsing
     * @throws InterruptedException if the sorting process is interrupted
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime = System.nanoTime();

        // Parsing Kosik products
        /**/KosikAdapter ka = new KosikAdapter();
        List<NormalizedProduct> kosikProducts = ka.getNormalizedProducts();/**/

        // Parsing Tesco products
        /**/TescoAdapter ta = new TescoAdapter();
        List<NormalizedProduct> tescoProducts = ta.getNormalizedProducts();/**/

        // Parsing Rohlik products
        /**/RohlikAdapter ra = new RohlikAdapter();
        List<NormalizedProduct> rohlikProducts = ra.getNormalizedProducts(RuntimeConfig.zipExtractPath);/**/

        /**/EqualProductsFinder epf = new EqualProductsFinder(kosikProducts, rohlikProducts, tescoProducts);
        epf.sortProbableEqualProductsAsync();  /**/

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;

        System.out.println("Program ran for " + duration / 1000 + " seconds and " + duration % 1000 + " ms.");
    }
}


