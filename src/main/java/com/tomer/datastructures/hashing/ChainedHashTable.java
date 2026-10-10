package com.tomer.datastructures.hashing;

import com.tomer.datastructures.core.Element;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * A {@link HashTable} that resolves collisions by chaining: each of its m = 2^k slots holds a linked
 * list of the entries hashed there. Insert appends to the key's list without searching it, so it takes
 * constant time apart from the inserts that double the table, which move every entry. Search and
 * delete walk the key's list, so their cost is that list's length: about the load (entries per slot)
 * when the hash spreads keys evenly. The load may go above 1.
 *
 * @param <K> The type of the keys.
 * @param <V> The type of the values.
 */
public class ChainedHashTable<K, V> implements HashTable<K, V> {
    final private HashFactory<K> hashFactory;
    final private double maxLoadFactor;
    private int capacity;
    private int k;
    private HashFunctor<K> hashFunc;
    private List<Element<K,V>>[] table;
    private int tableSize;

    /**
     * Creates an empty table with 2^k slots and draws its first hash function.
     *
     * @param hashFactory - Draws the table's hash function now and a new one each time it grows.
     * @param k - The table starts with 2^k slots, with {@code 0 <= k <= MAX_K}.
     * @param maxLoadFactor - When an insert would bring the load (entries per slot) to this value, the
     *                        table doubles first. Must be positive; it may be above 1.
     * @throws IllegalArgumentException - If k is negative or above {@link HashFactory#MAX_K}.
     */
    public ChainedHashTable(HashFactory<K> hashFactory, int k, double maxLoadFactor) {
        this.hashFactory = hashFactory;
        this.maxLoadFactor = maxLoadFactor;
        this.capacity = 1 << k;
        this.k = k;
        this.hashFunc = hashFactory.pickHash(k);
        this.table = newBuckets(this.capacity);
        this.tableSize = 0;

    }

    public V search(K key) {
        int index = this.hashFunc.hash(key);
        for (Element<K,V> element : this.table[index]) {
            if (element.key().equals(key)) {
                return element.satelliteData();
            }
        }
        return null;
    }

    public void insert(K key, V value) {
        if ((double) (this.tableSize + 1) / this.capacity >= this.maxLoadFactor) {
            rehashTable();
        }

        int index = this.hashFunc.hash(key);
        this.table[index].add(new Element<>(key, value));
        this.tableSize++;
    }

    private void rehashTable() {
        this.k++;
        int newCapacity = this.capacity << 1;
        List<Element<K,V>>[] newTable = newBuckets(newCapacity);

        this.hashFunc = hashFactory.pickHash(k);
        for (int i = 0; i < this.capacity; i = i + 1) {
            for (Element<K,V> element : this.table[i]) {
                int index = this.hashFunc.hash(element.key());
                newTable[index].add(element);
            }
        }

        this.table = newTable;
        this.capacity = newCapacity;
    }

    // Java can't create an array of a generic type, so this creates a List<?>[] and casts it. The cast is
    // safe: every bucket starts as a new empty list, and only Element<K,V> values are ever added to one.
    @SuppressWarnings("unchecked")
    private List<Element<K,V>>[] newBuckets(int count) {
        List<Element<K,V>>[] buckets = (List<Element<K,V>>[]) new List<?>[count];
        for (int i = 0; i < count; i = i + 1) {
            buckets[i] = new LinkedList<>();
        }
        return buckets;
    }

    public boolean delete(K key) {
        int index = this.hashFunc.hash(key);
        Iterator<Element<K,V>> iterator = this.table[index].iterator();
        while (iterator.hasNext()) {
            if (iterator.next().key().equals(key)) {
                iterator.remove();
                this.tableSize--;
                return true;
            }
        }
        return false;
    }

    public int capacity() { return capacity; }
}
