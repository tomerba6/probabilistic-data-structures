package com.tomer.datastructures.skiplist;

import java.util.Objects;

public class IndexableSkipList extends AbstractSkipList {
    final protected double p; // p is the probability for "success" in the geometric process generating the height of each node.
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
            while(node.getNext(i) != null && node.getNext(i).key() <= key) {
                node = node.getNext(i);
            }
        }
        return node;
    }

    @Override
    public int generateHeight() {
        int height = 0;
        while (Math.random() >= this.p) {
            height++;
        }
        return height;
    }

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
