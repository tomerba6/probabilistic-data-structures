package com.tomer.datastructures.hashing;

public interface HashFactory<K> {
    /**
     * @param k - The capacity of the hash table using the expected hash function is 2^k.
     * @return A randomly chosen hash function wrapped in a HashFunctor class.
     */
    public HashFunctor<K> pickHash(int k);
}
