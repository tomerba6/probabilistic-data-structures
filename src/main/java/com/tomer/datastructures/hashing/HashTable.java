package com.tomer.datastructures.hashing;

/**
 * A hash table mapping keys to values, with m = 2^k slots that double when the table gets too full.
 * Implemented by {@link ChainedHashTable} and {@link ProbingHashTable}.
 *
 * <p>Neither implementation checks for an existing key on insert, so inserting a key twice stores two
 * entries. {@link #search} returns one of them, and {@link #delete} removes the one search would
 * return, after which the other is found. Keys must not be null.
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 */
public interface HashTable<K, V>  {
    /**
     * Looks up the value stored with a key.
     *
     * @param key - The key to look up, not null.
     * @return The value stored with key, or null if key is not in the table. A value stored as null
     *         also comes back as null.
     */
    V search(K key);

    /**
     * Stores a value with a key, without checking whether the key is already there. If one more entry
     * would bring the load (entries per slot) to the table's max load factor, the table first doubles
     * its capacity, draws a new hash function and moves every entry to its new slot.
     *
     * @param key - The key, not null.
     * @param value - The value to store with it; may be null.
     */
    void insert(K key, V value);

    /**
     * Removes the entry that {@link #search} would return for a key.
     *
     * @param key - The key to remove, not null.
     * @return True if an entry was removed, false if key was not in the table.
     */
    boolean delete(K key);

    /**
     * Returns the number of slots, 2^k. It doubles each time the table grows.
     *
     * @return The number of slots.
     */
    int capacity();
}
