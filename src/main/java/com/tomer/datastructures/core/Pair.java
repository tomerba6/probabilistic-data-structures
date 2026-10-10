package com.tomer.datastructures.core;

/**
 * This class represents an immutable pair of values of different types.
 * Pairs compare by identity: equals is not overridden.
 * @param <T> The type of the first value.
 * @param <U> The type of the second value.
 */
public class Pair<T, U> {
    final private T first;
    final private U second;

    /**
     * Creates a pair of two values.
     *
     * @param first The first value; may be null.
     * @param second The second value; may be null.
     */
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value given to the constructor.
     *
     * @return The first value; may be null.
     */
    public T first() {
        return first;
    }

    /**
     * Returns the second value given to the constructor.
     *
     * @return The second value; may be null.
     */
    public U second() {
        return second;
    }
}
