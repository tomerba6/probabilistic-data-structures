package com.tomer.datastructures.hashing;

import com.tomer.datastructures.core.Element;

import java.util.List;
import java.util.LinkedList;

public class ChainedHashTable<K, V> implements HashTable<K, V> {
    final private HashFactory<K> hashFactory;
    final private double maxLoadFactor;
    private int capacity;
    private int k;
    private HashFunctor<K> hashFunc;
    private List<Element<K,V>>[] table;
    private int tableSize;

    public ChainedHashTable(HashFactory<K> hashFactory, int k, double maxLoadFactor) {
        this.hashFactory = hashFactory;
        this.maxLoadFactor = maxLoadFactor;
        this.capacity = 1 << k;
        this.k = k;
        this.hashFunc = hashFactory.pickHash(k);
        this.table = new List[this.capacity];
        for (int i = 0; i < this.capacity; i = i + 1) {
            this.table[i] = new LinkedList<>();
        }
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
        List<Element<K,V>>[] newTable = new List[newCapacity];
        for (int i = 0; i < newCapacity; i = i + 1) {
            newTable[i] = new LinkedList<>();
        }

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

    public boolean delete(K key) {
        int index = this.hashFunc.hash(key);
        for (Element<K,V> element : this.table[index]) {
            if (element.key().equals(key)) {
                this.table[index].remove(element);
                this.tableSize--;
                return true;
            }
        }
        return false;
    }

    public int capacity() { return capacity; }
}
