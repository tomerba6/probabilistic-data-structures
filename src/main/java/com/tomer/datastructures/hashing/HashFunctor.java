package com.tomer.datastructures.hashing;

/**
 * A hash function for a table of m = 2^k slots, drawn by a {@link HashFactory}. Its parameters are
 * fixed when it is drawn, so it always maps the same key to the same slot.
 *
 * @param <K> The type of the keys it hashes.
 */
public interface HashFunctor<K> {
    /***
     * Maps a key to its slot in the table.
     *
     * @param key - A valid key of type K, not null.
     * @return A hash mapping into [m], where m = 2^k is the size of the table the function was drawn
     *         for: a slot in {@code [0, m)}.
     */
    public int hash(K key);
}
