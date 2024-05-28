package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;
import java.util.ArrayList;
import java.util.List;

public class InferredData {
    private final String[] nameParts;
    private final List<String> lowerCaseNameParts = new ArrayList<>();

    public InferredData(String productName) {
        nameParts = productName.split(" ");
        for (String part : nameParts) {
            lowerCaseNameParts.add(part.toLowerCase());
        }
    }

    public String[] getNameParts() {
        return nameParts;
    }

    public List<String> getLowerCaseNameParts() {
        return lowerCaseNameParts;
    }
}