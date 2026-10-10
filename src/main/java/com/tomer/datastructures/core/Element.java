package com.tomer.datastructures.core;

/**
 * This class represents a general element in a general data structure,
 * with key and satellite data.
 * @param <K> The type of the key.
 * @param <V> The type of the satellite data.
 */
public class Element<K,V> {
    private K key;
    private V satelliteData;

    public Element(K key, V satelliteData) {
        this.key = key;
        this.satelliteData = satelliteData;
    }

    public Element(K key) {
        this(key, null);
    }

    public K key() {
        return this.key;
    }

    public V satelliteData() {
        return this.satelliteData;
    }

    public String toString() {
        return "[" + this.key() + "]";
    }

}
