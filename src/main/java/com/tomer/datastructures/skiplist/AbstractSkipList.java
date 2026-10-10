package com.tomer.datastructures.skiplist;

import com.tomer.datastructures.core.Element;

import java.util.NoSuchElementException;

import java.util.ArrayList;
import java.util.List;

/**
 * A skip list of distinct int keys in ascending order, with a head and a tail sentinel on every
 * level. Level 0 links every element, and each higher level skips more of them. Every link also
 * stores its width, the number of level-0 steps it spans, so on every level the widths from head to
 * tail add up to size + 1. Subclasses choose node heights, how to search and how to drop a level. Any
 * int can be stored, including Integer.MIN_VALUE and Integer.MAX_VALUE, the sentinels' own keys.
 */
abstract public class AbstractSkipList {
    /** The sentinel before every element: key Integer.MIN_VALUE, on every level, no previous node. */
    final protected SkipListNode head;
    /** The sentinel after every element: key Integer.MAX_VALUE, on every level, no next node. */
    final protected SkipListNode tail;
    /** The number of elements, not counting the sentinels. */
    protected int size = 0;

    /**
     * Creates an empty list: the head and the tail, linked on level 0.
     */
    public AbstractSkipList() {
        head = new SkipListNode(Integer.MIN_VALUE);
        tail = new SkipListNode(Integer.MAX_VALUE);
        increaseHeight();
    }

    /**
     * Adds a level on top of the list, on which the head links straight to the tail with width
     * size + 1. Insert calls it when a new node is taller than the list.
     */
    public void increaseHeight() {
        head.addLevel(tail, null);
        tail.addLevel(null, head);
        head.setNextWidth(head.height(), size + 1);
    }

    /**
     * Removes the top level from the head and the tail. Delete calls it while the top level holds no
     * elements; it must not be called while an element is on the top level, or on a list of one level.
     */
    abstract void decreaseHeight();

    /**
     * Returns the node with the largest key that is not above key. Expected O(log n).
     *
     * @param key the key to look for; any int
     * @return the node with key if it is stored, otherwise the one before where it would go: the head
     *         if key is below every element. Never the tail.
     */
    abstract SkipListNode find(int key);

    /**
     * Draws the height of a new node: the index of its top level.
     *
     * @return the new node's height, 0 or more
     */
    abstract int generateHeight();

    /**
     * Finds the node with a key. Expected O(log n).
     *
     * @param key the key to look for; any int
     * @return the node with key, or null if it is not stored. Never a sentinel.
     */
    public SkipListNode search(int key) {
        SkipListNode curr = find(key);

        // find returns the head for a key below every element, and the head's key is Integer.MIN_VALUE.
        return curr != head && curr.key() == key ? curr : null;
    }

    /**
     * Adds a node with a key at a height drawn by generateHeight, raising the list first if the node
     * is taller, and updates the widths of the links it splits or passes under. Expected O(log n).
     *
     * @param key the key to add; any int
     * @return the new node, or null if key is already stored. Nothing is added then, but the list may
     *         still have grown taller.
     */
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

    /**
     * Removes a node from every level it is on, updates the widths, and drops the top level while it
     * holds no elements. Expected O(log n).
     *
     * @param skipListNode the node to remove; may be null, but must not be the tail sentinel
     * @return true if the node was removed; false if it is null, the head, or not in this list, such as
     *         a node already deleted or one from another list with the same key
     */
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

    /**
     * Returns the node before a node on level 0, in constant time.
     *
     * @param skipListNode a node of this list, not null
     * @return the previous node: the head for the smallest element, null for the head itself
     */
    public SkipListNode predecessor(SkipListNode skipListNode) {
        return skipListNode.getPrev(0);
    }

    /**
     * Returns the node after a node on level 0, in constant time.
     *
     * @param skipListNode a node of this list, not null
     * @return the next node: the tail for the largest element, null for the tail itself
     */
    public SkipListNode successor(SkipListNode skipListNode) {
        return skipListNode.getNext(0);
    }

    /**
     * Returns the node with the smallest key, in constant time.
     *
     * @return the first node after the head
     * @throws NoSuchElementException if the list is empty
     */
    public SkipListNode minimum() {
        if (head.getNext(0) == tail) {
            throw new NoSuchElementException("minimum of an empty skip list");
        }

        return head.getNext(0);
    }

    /**
     * Returns the node with the largest key, in constant time.
     *
     * @return the last node before the tail
     * @throws NoSuchElementException if the list is empty
     */
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

    /**
     * A node of a skip list: an int key, with no satellite data, and on each of its levels 0 to
     * height() the next and previous nodes and the width of the link to the next one. It starts with
     * no levels; addLevel adds them.
     */
    public static class SkipListNode extends Element<Integer, Object> {
        final private List<SkipListNode> next;
        final private List<SkipListNode> prev;
        private List<Integer> nextWidth;
        private int height;

        /**
         * Creates a node with no levels yet, so its height is -1.
         *
         * @param key the node's key
         */
        public SkipListNode(int key) {
            super(key);
            next = new ArrayList<>();
            prev = new ArrayList<>();
            nextWidth = new ArrayList<>();
            this.height = -1;

        }

        /**
         * Returns the previous node on a level.
         *
         * @param level a level from 0 to height()
         * @return the previous node, or null on the head
         * @throws IllegalStateException if level is above height()
         * @throws IndexOutOfBoundsException if level is negative
         */
        public SkipListNode getPrev(int level) {
            checkLevel(level);
            return prev.get(level);
        }

        /**
         * Returns the next node on a level.
         *
         * @param level a level from 0 to height()
         * @return the next node, or null on the tail
         * @throws IllegalStateException if level is above height()
         * @throws IndexOutOfBoundsException if level is negative
         */
        public SkipListNode getNext(int level) {
            checkLevel(level);
            return next.get(level);
        }

        /**
         * Returns the width of the link to the next node on a level: how many level-0 steps it spans.
         *
         * @param level a level from 0 to height()
         * @return the width
         * @throws IllegalStateException if level is above height()
         * @throws IndexOutOfBoundsException if level is negative
         */
        public int getNextWidth(int level) {
            checkLevel(level);
            return nextWidth.get(level);
        }

        /**
         * Links this node to a next node on a level, leaving the link's width unchanged.
         *
         * @param level a level from 0 to height()
         * @param next the new next node
         * @throws IllegalStateException if level is above height()
         * @throws IndexOutOfBoundsException if level is negative
         */
        public void setNext(int level, SkipListNode next) {
            checkLevel(level);
            this.next.set(level, next);
        }

        /**
         * Links this node to a previous node on a level.
         *
         * @param level a level from 0 to height()
         * @param prev the new previous node
         * @throws IllegalStateException if level is above height()
         * @throws IndexOutOfBoundsException if level is negative
         */
        public void setPrev(int level, SkipListNode prev) {
            checkLevel(level);
            this.prev.set(level, prev);
        }

        /**
         * Sets the width of the link to the next node on a level.
         *
         * @param level a level from 0 to height()
         * @param nextWidth how many level-0 steps the link spans
         * @throws IllegalStateException if level is above height()
         * @throws IndexOutOfBoundsException if level is negative
         */
        public void setNextWidth(int level, int nextWidth) {
            checkLevel(level);
            this.nextWidth.set(level, nextWidth);
        }

        /**
         * Adds a level on top of this node with the given links and a width of 0, for the caller to set.
         *
         * @param next the next node on the new level
         * @param prev the previous node on the new level
         */
        public void addLevel(SkipListNode next, SkipListNode prev) {
            ++height;
            this.next.add(next);
            this.prev.add(prev);
            this.nextWidth.add(0);
        }

        /**
         * Removes this node's top level and its links.
         *
         * @throws IndexOutOfBoundsException if the node has no levels
         */
        public void removeLevel() {
            this.next.remove(height);
            this.prev.remove(height);
            this.nextWidth.remove(height);
            --height;
        }

        /**
         * Returns the index of this node's top level.
         *
         * @return -1 with no levels, 0 when the node is only on level 0
         */
        public int height() { return height; }

        private void checkLevel(int level) {
            if (level > height) {
                throw new IllegalStateException("Level " + level + " is above this node's height " + height);
            }
        }
    }
}
