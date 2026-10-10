package com.tomer.datastructures.hashing;

public interface HashTable<K, V>  {
    V search(K key);

    void insert(K key, V value);

    boolean delete(K key);

    int capacity();
}
