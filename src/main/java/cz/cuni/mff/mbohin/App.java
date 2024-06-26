package cz.cuni.mff.mbohin;

import cz.cuni.mff.mbohin.config.RuntimeConfig;
import cz.cuni.mff.mbohin.productParser.adapters.rohlik.RohlikAdapter;
import cz.cuni.mff.mbohin.productParser.adapters.kosik.KosikAdapter;
import cz.cuni.mff.mbohin.productParser.adapters.tesco.TescoAdapter;
import cz.cuni.mff.mbohin.productParser.normalizedJsonSchema.NormalizedProduct;
import cz.cuni.mff.mbohin.sameProductEstimator.EqualProductsFinder;

import java.io.IOException;
import java.util.List;

public class App 
{
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


