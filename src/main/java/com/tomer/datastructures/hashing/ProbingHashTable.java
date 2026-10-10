package com.tomer.datastructures.hashing;

import com.tomer.datastructures.core.Element;

/**
 * A {@link HashTable} that resolves collisions by linear probing: each of its m = 2^k slots holds at
 * most one entry, and a key whose slot is taken goes to the next free slot, wrapping around at the
 * end. Delete leaves a marker in the slot so later searches keep walking past it; insert reuses
 * marked slots, and doubling the table drops the markers. Search and delete stop at an empty slot or
 * after visiting every slot once. Every walk gets longer as the load (entries per slot) nears 1.
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 */
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

    /**
     * Creates an empty table with 2^k slots and draws its first hash function.
     *
     * @param hashFactory - Draws the table's hash function now and a new one each time it grows.
     * @param k - The table starts with 2^k slots, with {@code 0 <= k <= MAX_K}.
     * @param maxLoadFactor - When an insert would bring the load (entries per slot) to this value, the
     *                        table doubles first. Should be in (0, 1]: above 1 the table can fill every
     *                        slot, and an insert into a full table never returns.
     * @throws IllegalArgumentException - If k is negative or above {@link HashFactory#MAX_K}.
     */
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
