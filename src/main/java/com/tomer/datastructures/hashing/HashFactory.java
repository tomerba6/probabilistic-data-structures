package com.tomer.datastructures.hashing;

/**
 * A family of hash functions that a hash table draws from at random. Each call to {@link #pickHash}
 * returns a newly drawn function for a table of 2^k slots, and the hash tables in this package draw
 * a new one each time they double. Implemented by {@link ModularHash} for {@code Integer} keys and
 * {@link MultiplicativeShiftingHash} for {@code Long} keys.
 *
 * @param <K> The type of the keys the functions hash.
 */
public interface HashFactory<K> {
    /**
     * The largest k that pickHash accepts. The 2^k slots must fit in a positive int, and
     * {@code 1 << 31} overflows.
     */
    int MAX_K = 30;

    /**
     * Draws a hash function at random from this family, for a table of 2^k slots.
     *
     * @param k - The capacity of the hash table using the expected hash function is 2^k, with
     *            {@code 0 <= k <= MAX_K}.
     * @return A randomly chosen hash function wrapped in a HashFunctor class.
     * @throws IllegalArgumentException - If k is negative or above {@link #MAX_K}.
     */
    public HashFunctor<K> pickHash(int k);
}
