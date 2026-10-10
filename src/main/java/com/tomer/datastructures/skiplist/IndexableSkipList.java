package com.tomer.datastructures.skiplist;

import java.util.Objects;

/**
 * An {@link AbstractSkipList} whose node heights follow a geometric distribution with parameter p, and
 * which uses the link widths to answer rank and select in expected O(log n).
 */
public class IndexableSkipList extends AbstractSkipList {
    /** p is the probability for "success" in the geometric process generating the height of each node. */
    final protected double p;

    /**
     * Creates an empty list whose nodes stop growing with probability p at each level.
     *
     * @param probability p, in (0, 1]. At 0 or below the first insert never returns; at 1 every node
     *                    has height 0, so the list has a single level.
     */
    public IndexableSkipList(double probability) {
        super();
        this.p = probability;
    }

    @Override
    public void decreaseHeight() {
        head.removeLevel();
        tail.removeLevel();
    }

    @Override
    public SkipListNode find(int key) {
        SkipListNode node = head;
        for (int i = head.height(); i >= 0 ; --i) {
            while(node.getNext(i) != tail && node.getNext(i).key() <= key) {
                node = node.getNext(i);
            }
        }
        return node;
    }

    /**
     * Draws a height from the geometric distribution: height h with probability p(1 - p)^h, which
     * averages (1 - p) / p.
     *
     * @return the new node's height, 0 or more
     */
    @Override
    public int generateHeight() {
        int height = 0;
        while (Math.random() >= this.p) {
            height++;
        }
        return height;
    }

    /**
     * Counts the elements with keys strictly below key, using the link widths. Expected O(log n).
     *
     * @param key any int; it need not be stored
     * @return the number of elements below key, from 0 to size
     */
    public int rank(int key) {
        int currentRank = 0;
        SkipListNode node = head;
        for (int i = head.height(); i >= 0 ; --i) {
            while(node.getNext(i) != null && node.getNext(i).key() < key) {
                currentRank = currentRank + node.getNextWidth(i);
                node = node.getNext(i);
            }
        }
        return currentRank;
    }

    /**
     * Returns the key at a position in ascending order, found by walking the link widths. Expected
     * O(log n).
     *
     * @param index the 0-based position, from 0 to size - 1
     * @return the key at that position
     * @throws IndexOutOfBoundsException if index is negative or not less than size
     */
    public int select(int index) {
        Objects.checkIndex(index, size);
        SkipListNode node = head;
        for (int i = head.height(); i >= 0 ; --i) {
            while(node.getNext(i) != null && index - node.getNextWidth(i) >= 0) {
                index -= node.getNextWidth(i);
                node = node.getNext(i);
            }
        }
        return node.getNext(0).key();
    }
}
