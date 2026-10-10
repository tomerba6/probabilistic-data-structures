package com.tomer.datastructures.hashing;

public interface HashFactory<K> {
    /**
     * The largest k that pickHash accepts. The 2^k slots must fit in a positive int, and 1 << 31 overflows.
     */
    int MAX_K = 30;

    /**
     * @param k - The capacity of the hash table using the expected hash function is 2^k, with 0 <= k <= MAX_K.
     * @return A randomly chosen hash function wrapped in a HashFunctor class.
     */
    public HashFunctor<K> pickHash(int k);
}
