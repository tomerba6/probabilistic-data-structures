package com.tomer.datastructures.core;

/**
 * This class represents a general element in a general data structure,
 * with key and satellite data.
 * Its key and satellite data never change after it is made, and elements compare by identity: two
 * elements with the same key and data are still different elements.
 * @param <K> The type of the key.
 * @param <V> The type of the satellite data.
 */
public class Element<K,V> {
    private K key;
    private V satelliteData;

    /**
     * Creates an element with a key and satellite data.
     *
     * @param key The key; may be null.
     * @param satelliteData The data carried with the key; may be null.
     */
    public Element(K key, V satelliteData) {
        this.key = key;
        this.satelliteData = satelliteData;
    }

    /**
     * Creates an element with a key and no satellite data.
     *
     * @param key The key; may be null.
     */
    public Element(K key) {
        this(key, null);
    }

    /**
     * Returns the key this element was made with.
     *
     * @return The key; may be null.
     */
    public K key() {
        return this.key;
    }

    /**
     * Returns the satellite data this element was made with.
     *
     * @return The satellite data, or null if it has none.
     */
    public V satelliteData() {
        return this.satelliteData;
    }

    public String toString() {
        return "[" + this.key() + "]";
    }

}
