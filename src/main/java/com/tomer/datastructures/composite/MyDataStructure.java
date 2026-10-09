package com.tomer.datastructures.composite;

import com.tomer.datastructures.hashing.ChainedHashTable;
import com.tomer.datastructures.hashing.ModularHash;
import com.tomer.datastructures.skiplist.AbstractSkipList;
import com.tomer.datastructures.skiplist.IndexableSkipList;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite data structure supporting order-statistics and fast lookups.
 * It combines an {@code IndexableSkipList} for expected Theta(log n) rank, select,
 * and sequential range queries, with a {@code ChainedHashTable} for expected
 * Theta(1) contains queries and direct node access to prevent redundant searches.
 */
public class MyDataStructure {
    /*
     * We use a composite data structure combining:
     * 1. A ChainedHashTable for expected O(1) searches. It maps a value to its SkipListNode.
     * 2. An IndexableSkipList for O(log n) order-statistics operations (Rank, Select)
     * and sequential traversal.
     */
    private IndexableSkipList skipList;
    private final double PROBABILITY = 0.5;
    private ChainedHashTable<Integer, AbstractSkipList.SkipListNode> hashTable;
    private final int capacity;
    private int size;

    /**
     * Initializes the data structure for a maximum of N items.
     * Calculates the minimal power of 2 (k) where 2^k >= N, and initializes a
     * ChainedHashTable with capacity 2^k and a load factor >= 1.0 to prevent rehashing.
     * Time Complexity: Theta(log N) for k calculation, Theta(N) worst-case to
     * initialize the hash table array and empty linked lists. Total: Theta(N) Worst Case.
     *
     * @param N The maximal number of items that may reside in the DS.
     */
    public MyDataStructure(int N) {
        this.capacity = N;
        this.size = 0;
        this.skipList = new IndexableSkipList(PROBABILITY);

        // Calculate the minimum k such that 2^k >= N
        int k = 1;
        while ((1 << k) < N) {
            k++;
        }

        // We set maxLoadFactor to 2.0. Since the capacity is >= N, it will never exceed 1.0,
        // guaranteeing no rehash will ever occur during inserts.
        this.hashTable = new ChainedHashTable<>(new ModularHash(), k, 2.0);
    }

    /**
     * Inserts a value into the DS if it isn't contained already.
     * It checks for existence using the hash table, inserts the value into the
     * SkipList, and maps the value to the generated node in the hash table.
     * Time Complexity: Theta(1) expected for hash table checks/insertion,
     * and Theta(log n) expected for SkipList insertion. Total: Theta(log n) Expected.
     *
     * @param value the value to insert.
     * @return true if the item was inserted, false if it already exists or at capacity.
     */
    public boolean insert(int value) {
        if (size == capacity || contains(value)) {
            return false;
        }

        AbstractSkipList.SkipListNode node = skipList.insert(value);
        hashTable.insert(value, node);
        size++;
        return true;
    }

    /**
     * Removes the value from the DS if it exists.
     * Locates the SkipListNode directly via the hash table to bypass SkipList search,
     * deletes the node from the SkipList using the reference, and removes the key.
     * Time Complexity: Theta(1) expected for hash table search/deletion.
     * Theta(log n) expected for SkipList node deletion. Total: Theta(log n) Expected.
     *
     * @param value the value to delete.
     * @return true if the item was removed, false if it wasn't found.
     */
    public boolean delete(int value) {
        AbstractSkipList.SkipListNode node = hashTable.search(value);
        if (node == null) {
            return false;
        }

        skipList.delete(node);
        hashTable.delete(value);
        size--;
        return true;
    }

    /**
     * Checks if the data structure contains the specified value.
     * It delegates the query directly to the internal HashTable,
     * bypassing the SkipList entirely to achieve optimal lookup times.
     * Time Complexity: Searching a ChainedHashTable with a bounded load factor
     * takes Theta(1) Expected time.
     *
     * @param value the value to check.
     * @return true if the DS contains the value, false otherwise.
     */
    public boolean contains(int value) {
        return hashTable.search(value) != null;
    }

    /**
     * Returns the number of items in the DS that are strictly less than val.
     * It delegates the operation to the IndexableSkipList, which uses the
     * maintained sizes of sub-lists in its links to compute the rank.
     * Time Complexity: Traversing the IndexableSkipList based on link sizes
     * takes Theta(log n) Expected time.
     *
     * @param value the value to rank.
     * @return the number of elements smaller than the value.
     */
    public int rank(int value) {
        return skipList.rank(value);
    }

    /**
     * Returns the item at the specified index (0 <= index <= DS.size - 1).
     * It delegates the operation to the IndexableSkipList, which traverses
     * its levels using the recorded link sizes to locate the i-th element.
     * Time Complexity: The SkipList performs this search by traversing down
     * and right, which takes Theta(log n) Expected time.
     *
     * @param index the index to select.
     * @return the value at the given index.
     */
    public int select(int index) {
        return skipList.select(index);
    }

    /**
     * Returns a list of items between low and high in ascending order.
     * Uses the hash table to find the 'low' node in expected Theta(1) time.
     * Then traverses the bottom level (level 0) of the SkipList to collect items.
     * Time Complexity: Theta(1) for lookup, and iterating through level 0 touches
     * exactly |L| elements. Total: Theta(|L|) Expected.
     *
     * @param low the lower bound (must be present in the DS to return a list).
     * @param high the upper bound.
     * @return a List of items in the range, or null if 'low' is not in the DS.
     */
    public List<Integer> range(int low, int high) {
        AbstractSkipList.SkipListNode currentNode = hashTable.search(low);
        if (currentNode == null) {
            return null;
        }

        List<Integer> result = new ArrayList<>();
        // Stop at the tail sentinel, the only level-0 node with no next. Its key is
        // Integer.MAX_VALUE, so comparing keys alone would add it when high is that value.
        while (currentNode.getNext(0) != null && currentNode.key() <= high) {
            result.add(currentNode.key());
            currentNode = currentNode.getNext(0);
        }
        return result;
    }
}
