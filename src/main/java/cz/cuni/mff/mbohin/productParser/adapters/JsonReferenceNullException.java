package cz.cuni.mff.mbohin.productParser.adapters;

public class JsonReferenceNullException extends IllegalArgumentException {

    public JsonReferenceNullException(String message) {
        super(message);
    }

    public JsonReferenceNullException() {
        super();
    }
}
