package cz.cuni.mff.mbohin.productParser.normalizedJsonSchema;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.lang.Character;

public class InferredData {
    private final String[] nameParts;
    private final List<String> lowerCaseNameParts = new ArrayList<>();

    private final String uniqueFileName;

    public InferredData(String productName) {
        nameParts = productName.split(" ");
        for (String part : nameParts) {
            lowerCaseNameParts.add(part.toLowerCase());
        }
        uniqueFileName = filterLetters(productName);
    }

    public String[] getNameParts() {
        return nameParts;
    }

    public List<String> getLowerCaseNameParts() {
        return lowerCaseNameParts;
    }

    public String getUniqueFileName() {
        return uniqueFileName;
    }

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