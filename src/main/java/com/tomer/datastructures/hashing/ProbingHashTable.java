package com.tomer.datastructures.hashing;

import com.tomer.datastructures.core.Element;

public class ProbingHashTable<K, V> implements HashTable<K, V> {
    private static final int NOT_FOUND = -1;
    private final Element<K, V> DELETED = new Element<>(null, null);
    final private HashFactory<K> hashFactory;
    final private double maxLoadFactor;
    private int capacity;
    private int k;
    private HashFunctor<K> hashFunc;
    private Element<K,V>[] table;
    private int tableSize;

    public ProbingHashTable(HashFactory<K> hashFactory, int k, double maxLoadFactor) {
        this.hashFactory = hashFactory;
        this.maxLoadFactor = maxLoadFactor;
        this.capacity = 1 << k;
        this.k = k;
        this.hashFunc = hashFactory.pickHash(k);
        this.table = new Element[capacity];
        this.tableSize = 0;

    }

    public V search(K key) {
        int index = indexOf(key);
        return index == NOT_FOUND ? null : table[index].satelliteData();
    }

    public void insert(K key, V value) {
        if ((double) (this.tableSize + 1) / this.capacity >= this.maxLoadFactor) {
            rehashTable();
        }

        int index = this.hashFunc.hash(key);
        while (this.table[index] != null && this.table[index] != DELETED) {
            index = (index + 1) % this.capacity;
        }

        this.table[index] = new Element<>(key, value);
        this.tableSize++;
    }

    private void rehashTable() {
        this.k++;
        int newCapacity = this.capacity << 1;
        Element<K,V>[] newTable = new Element[newCapacity];

        this.hashFunc = hashFactory.pickHash(k);
        for (int i = 0; i < this.capacity; i = i + 1) {
            if (this.table[i] != null && this.table[i] != DELETED) {
                int index = this.hashFunc.hash(this.table[i].key());
                while (newTable[index] != null) {
                    index = (index + 1) % newCapacity;
                }

                newTable[index] = this.table[i];
            }
        }

        this.table = newTable;
        this.capacity = newCapacity;
    }

    public boolean delete(K key) {
        int index = indexOf(key);
        if (index == NOT_FOUND) {
            return false;
        }

        table[index] = DELETED;
        tableSize--;
        return true;
    }

    // Probes from the key's hash, skipping DELETED cells. Stops at an empty slot or after one full pass.
    private int indexOf(K key) {
        int index = hashFunc.hash(key);
        for (int i = 0; i < this.capacity; i++) {
            if (table[index] == null) {
                return NOT_FOUND;
            }

            if (table[index] != DELETED && table[index].key().equals(key)) {
                return index;
            }

            index = (index + 1) % capacity;
        }
        return NOT_FOUND;
    }

    public int capacity() { return capacity; }
}
