package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.lang.Character;

/**
 * Represents inferred data from a product name, including split name parts,
 * lowercased name parts, and a unique file name generated from the product name.
 */
public class InferredData {
    private final String[] nameParts;
    private final List<String> lowerCaseNameParts = new ArrayList<>();

    private final String uniqueFileName;

    /**
     * Constructs an InferredData instance from the provided product name.
     * Splits the product name into parts, converts them to lowercase, and generates a unique file name.
     *
     * @param productName the name of the product
     */
    public InferredData(String productName) {
        nameParts = productName.split(" ");
        for (String part : nameParts) {
            lowerCaseNameParts.add(part.toLowerCase());
        }
        uniqueFileName = filterLetters(productName);
    }

    /**
     * Returns the parts of the product name.
     *
     * @return an array of name parts
     */
    @SuppressWarnings("unused")
    public String[] getNameParts() {
        return nameParts;
    }

    /**
     * Returns the lowercased parts of the product name.
     *
     * @return a list of lowercased name parts
     */
    public List<String> getLowerCaseNameParts() {
        return lowerCaseNameParts;
    }

    /**
     * Returns the unique file name generated from the product name.
     *
     * @return the unique file name
     */
    public String getUniqueFileName() {
        return uniqueFileName;
    }

    /**
     * Filters letters from the input string to generate a unique file name.
     * Converts spaces to underscores, keeps only lowercase letters, and limits the length to 60 characters.
     *
     * @param s the input string
     * @return the filtered string
     */
    public static String filterLetters(String s) {
        String normalizedString = Normalizer.normalize(s, Form.NFD);
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : normalizedString.toCharArray()) {
            if (c == ' ') {
                stringBuilder.append('_');
            } else if (Character.isLetter(c) && !Character.isISOControl(c) && !Character.isDigit(c) && !Character.isSpaceChar(c)) {
                char letter = Character.toLowerCase(c);
                stringBuilder.append(letter);
                if (stringBuilder.length() > 60)
                    break;
            }
        }

        return stringBuilder.toString();
    }
}