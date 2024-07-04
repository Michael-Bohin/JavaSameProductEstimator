package cz.cuni.mff.mbohin.productParser.adapters;

/**
 * A generic class that represents a pair of two related objects.
 *
 * @param <K> the type of the first object (key)
 * @param <V> the type of the second object (value)
 */
public class Pair<K, V> {
    private K key;
    private V value;

    /**
     * Constructs a new Pair with the specified key and value.
     *
     * @param key the key of the pair
     * @param value the value of the pair
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of this pair.
     *
     * @return the key of this pair
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of this pair.
     *
     * @return the value of this pair
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the key of this pair.
     *
     * @param key the new key of this pair
     */
    @SuppressWarnings("unused")
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * Sets the value of this pair.
     *
     * @param value the new value of this pair
     */
    public void setValue(V value) {
        this.value = value;
    }
}

