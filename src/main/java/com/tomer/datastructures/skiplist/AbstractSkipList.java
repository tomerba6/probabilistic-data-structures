package com.tomer.datastructures.skiplist;

import com.tomer.datastructures.core.Element;

import java.util.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;

abstract public class AbstractSkipList {
    final protected SkipListNode head;
    final protected SkipListNode tail;
    protected int size = 0;

    public AbstractSkipList() {
        head = new SkipListNode(Integer.MIN_VALUE);
        tail = new SkipListNode(Integer.MAX_VALUE);
        increaseHeight();
    }

    public void increaseHeight() {
        head.addLevel(tail, null);
        tail.addLevel(null, head);
        head.setNextWidth(head.height(), size + 1);
    }

    abstract void decreaseHeight();

    abstract SkipListNode find(int key);

    abstract int generateHeight();

    public SkipListNode search(int key) {
        SkipListNode curr = find(key);

        // find returns the head for a key below every element, and the head's key is Integer.MIN_VALUE.
        return curr != head && curr.key() == key ? curr : null;
    }

    public SkipListNode insert(int key) {
        int nodeHeight = generateHeight();

        while (nodeHeight > head.height()) {
            increaseHeight();
        }

        Predecessors predecessors = predecessorsOf(key);

        SkipListNode nextNode = predecessors.nodes()[0].getNext(0);
        if (nextNode != tail && nextNode.key() == key) {
            return null;
        }

        SkipListNode newNode = new SkipListNode(key);
        linkIn(newNode, nodeHeight, predecessors);
        size++;

        return newNode;
    }

    public boolean delete(SkipListNode skipListNode) {
        if (skipListNode == null) {
            return false;
        }

        Predecessors predecessors = predecessorsOf(skipListNode.key());

        if (predecessors.nodes()[0].getNext(0) != skipListNode) {
            return false;
        }

        unlink(skipListNode, predecessors);
        size--;

        while (head.height() > 0 && head.getNext(head.height()) == tail) {
            decreaseHeight();
        }

        return true;
    }

    // On each level, the last node before a key and its rank, the number of steps from the head at level 0.
    private record Predecessors(SkipListNode[] nodes, int[] ranks) {}

    private Predecessors predecessorsOf(int key) {
        int currentMaxHeight = head.height();

        SkipListNode[] nodes = new SkipListNode[currentMaxHeight + 1];
        int[] ranks = new int[currentMaxHeight + 1];

        SkipListNode curr = head;
        int currentRank = 0;

        for (int i = currentMaxHeight; i >= 0; i--) {
            while (curr.getNext(i) != tail && curr.getNext(i).key() < key) {
                currentRank += curr.getNextWidth(i);
                curr = curr.getNext(i);
            }

            nodes[i] = curr;
            ranks[i] = currentRank;
        }

        return new Predecessors(nodes, ranks);
    }

    // Links newNode in after its predecessors on levels 0 to nodeHeight, and widens each link above them by one.
    private void linkIn(SkipListNode newNode, int nodeHeight, Predecessors predecessors) {
        int newNodeRank = predecessors.ranks()[0] + 1;

        for (int i = 0; i < predecessors.nodes().length; i++) {
            SkipListNode prev = predecessors.nodes()[i];
            SkipListNode next = prev.getNext(i);

            if (i <= nodeHeight) {
                int oldWidth = prev.getNextWidth(i);

                newNode.addLevel(next, prev);
                prev.setNext(i, newNode);
                next.setPrev(i, newNode);

                int widthToNew = newNodeRank - predecessors.ranks()[i];
                int widthFromNew = oldWidth - widthToNew + 1;

                prev.setNextWidth(i, widthToNew);
                newNode.setNextWidth(i, widthFromNew);

            } else {
                int oldWidth = prev.getNextWidth(i);
                prev.setNextWidth(i, oldWidth + 1);
            }
        }
    }

    // Unlinks node from every level it is on, and narrows each link that passed over it by one.
    private void unlink(SkipListNode node, Predecessors predecessors) {
        for (int i = 0; i < predecessors.nodes().length; i++) {
            SkipListNode prev = predecessors.nodes()[i];

            if (prev.getNext(i) == node) {
                int widthToNode = prev.getNextWidth(i);
                int widthFromNode = node.getNextWidth(i);
                prev.setNextWidth(i, widthToNode + widthFromNode - 1);

                SkipListNode nextNode = node.getNext(i);
                prev.setNext(i, nextNode);
                nextNode.setPrev(i, prev);

            } else {
                int oldWidth = prev.getNextWidth(i);
                prev.setNextWidth(i, oldWidth - 1);
            }
        }
    }

    public SkipListNode predecessor(SkipListNode skipListNode) {
        return skipListNode.getPrev(0);
    }

    public SkipListNode successor(SkipListNode skipListNode) {
        return skipListNode.getNext(0);
    }

    public SkipListNode minimum() {
        if (head.getNext(0) == tail) {
            throw new NoSuchElementException("minimum of an empty skip list");
        }

        return head.getNext(0);
    }

    public SkipListNode maximum() {
        if (tail.getPrev(0) == head) {
            throw new NoSuchElementException("maximum of an empty skip list");
        }

        return tail.getPrev(0);
    }

    private void levelToString(StringBuilder s, int level) {
        s.append("H    ");
        SkipListNode curr = head.getNext(0);

        while (curr != tail) {
            if (curr.height >= level) {
                s.append(curr.key());
                s.append("    ");
            }
            else {
                s.append("    ");
                for (int i = 0; i < curr.key().toString().length(); i = i + 1)
                    s.append(" ");
            }

            curr = curr.getNext(0);
        }

        s.append("T\n");
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int level = head.height(); level >= 0; --level) {
            levelToString(str, level);
        }

        return str.toString();
    }

    public static class SkipListNode extends Element<Integer, Object> {
        final private List<SkipListNode> next;
        final private List<SkipListNode> prev;
        private List<Integer> nextWidth;
        private int height;

        public SkipListNode(int key) {
            super(key);
            next = new ArrayList<>();
            prev = new ArrayList<>();
            nextWidth = new ArrayList<>();
            this.height = -1;

        }

        public SkipListNode getPrev(int level) {
            checkLevel(level);
            return prev.get(level);
        }

        public SkipListNode getNext(int level) {
            checkLevel(level);
            return next.get(level);
        }

        public int getNextWidth(int level) {
            checkLevel(level);
            return nextWidth.get(level);
        }

        public void setNext(int level, SkipListNode next) {
            checkLevel(level);
            this.next.set(level, next);
        }

        public void setPrev(int level, SkipListNode prev) {
            checkLevel(level);
            this.prev.set(level, prev);
        }

        public void setNextWidth(int level, int nextWidth) {
            checkLevel(level);
            this.nextWidth.set(level, nextWidth);
        }

        public void addLevel(SkipListNode next, SkipListNode prev) {
            ++height;
            this.next.add(next);
            this.prev.add(prev);
            this.nextWidth.add(0);
        }

        public void removeLevel() {
            this.next.remove(height);
            this.prev.remove(height);
            this.nextWidth.remove(height);
            --height;
        }

        public int height() { return height; }

        private void checkLevel(int level) {
            if (level > height) {
                throw new IllegalStateException("Level " + level + " is above this node's height " + height);
            }
        }
    }
}
