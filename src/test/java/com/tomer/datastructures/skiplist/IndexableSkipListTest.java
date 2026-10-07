package com.tomer.datastructures.skiplist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static com.tomer.datastructures.skiplist.SkipListInvariants.assertWidthsValid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("IndexableSkipList")
class IndexableSkipListTest {

    private final IndexableSkipList list = new IndexableSkipList(0.5);

    private void insertAll(int... keys) {
        for (int key : keys) {
            list.insert(key);
        }
    }

    private boolean deleteKey(int key) {
        return list.delete(list.search(key));
    }

    @Nested
    @DisplayName("increaseHeight and decreaseHeight (Task 2.1)")
    class Height {

        private int initialHeight;

        @BeforeEach
        void raiseByTwoLevels() {
            initialHeight = list.head.height();
            list.increaseHeight();
            list.increaseHeight();
        }

        @Test
        @DisplayName("increaseHeight adds one level per call")
        void increaseAddsALevel() {
            assertEquals(initialHeight + 2, list.head.height(), "increaseHeight works correctly");
        }

        @Test
        @DisplayName("decreaseHeight removes one level")
        void decreaseRemovesALevel() {
            list.decreaseHeight();
            assertEquals(initialHeight + 1, list.head.height(), "decreaseHeight successfully reduced height by 1");
        }

        @Test
        @DisplayName("keeps head and tail at the same height after a decrease")
        void headAndTailStayInStep() {
            list.decreaseHeight();
            assertEquals(list.head.height(), list.tail.height(), "head and tail heights are synchronized after decrease");
        }
    }

    @Nested
    @DisplayName("find (Task 2.2)")
    class Find {

        @BeforeEach
        void insertTenToFifty() {
            insertAll(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("returns the node itself when the key is present")
        void presentKey() {
            AbstractSkipList.SkipListNode node = list.find(30);
            assertNotNull(node, "Find returns the exact node when key exists");
            assertEquals(30, node.key(), "Find returns the exact node when key exists");
        }

        @Test
        @DisplayName("returns the predecessor when the key is missing")
        void missingKey() {
            AbstractSkipList.SkipListNode node = list.find(25);
            assertNotNull(node, "Find returns the correct predecessor for a missing key");
            assertEquals(20, node.key(), "Find returns the correct predecessor for a missing key");
        }

        @Test
        @DisplayName("returns the head for a key below every element")
        void belowMinimum() {
            AbstractSkipList.SkipListNode node = list.find(5);
            assertNotNull(node, "Find returns the head node for a key smaller than any in list");
            assertEquals(Integer.MIN_VALUE, node.key(), "Find returns the head node for a key smaller than any in list");
        }

        @Test
        @DisplayName("returns the maximum for a key above every element")
        void aboveMaximum() {
            AbstractSkipList.SkipListNode node = list.find(100);
            assertNotNull(node, "Find returns the maximum element node for a key larger than any in list");
            assertEquals(50, node.key(), "Find returns the maximum element node for a key larger than any in list");
        }
    }

    @Nested
    @DisplayName("generateHeight (Task 2.3)")
    class GenerateHeight {

        // p = 0.25 tells p from 1 - p: at p = 0.5 an inverted coin draws the same distribution.
        @ParameterizedTest(name = "p = {0}")
        @ValueSource(doubles = {0.5, 0.25})
        @DisplayName("follows the geometric distribution P(h) = p(1-p)^h")
        void geometricDistribution(double p) {
            IndexableSkipList heights = new IndexableSkipList(p);

            int n = 1_000_000;
            int[] heightCounts = new int[10];
            for (int i = 0; i < n; i++) {
                int h = heights.generateHeight();
                if (h < heightCounts.length) {
                    heightCounts[h]++;
                }
            }

            double currentExpectedProb = p;
            for (int h = 0; h < 5; h++) {
                double actualProb = (double) heightCounts[h] / n;
                // Each count is binomial, so its frequency has standard deviation sqrt(q(1-q)/n).
                // A 5 standard deviation tolerance fails by chance about once in 1.7 million runs per level.
                double tolerance = 5 * Math.sqrt(currentExpectedProb * (1 - currentExpectedProb) / n);
                assertEquals(currentExpectedProb, actualProb, tolerance,
                        "Frequency of height " + h + " is within 5 standard deviations of p(1-p)^h, with p=" + p);
                currentExpectedProb = currentExpectedProb * (1 - p);
            }
        }
    }

    @Nested
    @DisplayName("width maintenance on insert (Task 2.5)")
    class InsertWidths {

        @Test
        @DisplayName("an empty list")
        void emptyList() {
            assertWidthsValid(list, "Empty list widths equal size+1");
        }

        @Test
        @DisplayName("the first insertion")
        void firstInsertion() {
            list.insert(50);
            assertWidthsValid(list, "Widths correct after 1st insertion (50)");
            assertEquals(1, list.size, "Size correctly updated to 1");
        }

        @Test
        @DisplayName("an element below every other")
        void prepending() {
            insertAll(50, 20);
            assertWidthsValid(list, "Widths correct after prepending (20)");
        }

        @Test
        @DisplayName("an element above every other")
        void appending() {
            insertAll(50, 20, 80);
            assertWidthsValid(list, "Widths correct after appending (80)");
        }

        @Test
        @DisplayName("elements between existing ones")
        void middleInsertions() {
            insertAll(50, 20, 80, 30, 40);
            assertWidthsValid(list, "Widths correct after middle insertions (30, 40)");
            assertEquals(5, list.size, "Size correctly updated to 5");
        }

        @Test
        @DisplayName("a duplicate, which is rejected")
        void duplicate() {
            insertAll(50, 20, 80, 30, 40);
            list.insert(30);
            assertWidthsValid(list, "Widths unaffected by duplicate insertion");
            assertEquals(5, list.size, "Size unchanged after duplicate insertion");
        }
    }

    @Nested
    @DisplayName("width maintenance on delete (Task 2.5)")
    class DeleteWidths {

        @BeforeEach
        void insertTenToFifty() {
            insertAll(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("before any deletion")
        void beforeDeletion() {
            assertWidthsValid(list, "Setup: Initial widths are correct before deletion");
        }

        @Test
        @DisplayName("deleting a middle element")
        void middleElement() {
            boolean deleted30 = deleteKey(30);
            assertTrue(deleted30, "Successfully deleted middle element (30)");
            assertWidthsValid(list, "Widths correctly merged and updated after middle deletion");
            assertEquals(4, list.size, "Size correctly decremented to 4");
        }

        @Test
        @DisplayName("deleting the first element")
        void firstElement() {
            deleteKey(30);
            deleteKey(10);
            assertWidthsValid(list, "Widths correct after deleting the first element (10)");
        }

        @Test
        @DisplayName("deleting the last element")
        void lastElement() {
            deleteKey(30);
            deleteKey(10);
            deleteKey(50);
            assertWidthsValid(list, "Widths correct after deleting the last element (50)");
        }

        @Test
        @DisplayName("deleting a node that is not in the list")
        void absentNode() {
            deleteKey(30);
            deleteKey(10);
            deleteKey(50);
            boolean deleted100 = list.delete(new AbstractSkipList.SkipListNode(100));
            assertFalse(deleted100, "Deletion of non-existent element rejected");
            assertWidthsValid(list, "Widths unaffected by failed deletion");
        }

        @Test
        @DisplayName("deleting every element")
        void emptyingTheList() {
            deleteKey(30);
            deleteKey(10);
            deleteKey(50);
            list.delete(new AbstractSkipList.SkipListNode(100));
            deleteKey(20);
            deleteKey(40);
            assertWidthsValid(list, "Widths correct after completely emptying the list");
            assertEquals(0, list.size, "Size is 0 after deleting all elements");
        }
    }

    @Nested
    @DisplayName("rank (Task 2.5)")
    class Rank {

        @Test
        @DisplayName("is 0 on an empty list")
        void emptyList() {
            assertEquals(0, list.rank(10), "Rank in an empty list is always 0");
        }

        @Test
        @DisplayName("counts the smaller elements of a present key")
        void presentKeys() {
            insertAll(10, 20, 30, 40, 50);
            assertEquals(0, list.rank(10), "Rank of minimum element (10) is 0");
            assertEquals(2, list.rank(30), "Rank of middle element (30) is 2");
            assertEquals(4, list.rank(50), "Rank of maximum element (50) is 4");
        }

        @Test
        @DisplayName("counts the smaller elements of a missing key")
        void missingKeys() {
            insertAll(10, 20, 30, 40, 50);
            assertEquals(2, list.rank(25), "Rank of non-existent middle element (25) is correctly 2 (10, 20 are smaller)");
            assertEquals(0, list.rank(5), "Rank of element smaller than all elements (5) is 0");
            assertEquals(5, list.rank(100), "Rank of element larger than all elements (100) is 5 (equal to size)");
        }
    }

    @Nested
    @DisplayName("select (Task 2.5)")
    class Select {

        @BeforeEach
        void insertTenToFifty() {
            insertAll(10, 20, 30, 40, 50);
        }

        @Test
        @DisplayName("returns the element at each index")
        void validIndices() {
            assertEquals(10, list.select(0), "Select(0) returns the minimum element (10)");
            assertEquals(30, list.select(2), "Select(2) returns the middle element (30)");
            assertEquals(50, list.select(4), "Select(4) returns the maximum element (50)");
        }

        @Test
        @DisplayName("shifts down after a deletion")
        void afterDeletion() {
            deleteKey(30);
            assertEquals(40, list.select(2), "Select(2) dynamically shifts to 40 after deleting 30");
            assertEquals(50, list.select(3), "Select(3) is now the maximum element (50)");
        }

        @Test
        @DisplayName("shifts up after an insertion")
        void afterInsertion() {
            deleteKey(30);
            list.insert(25);
            assertEquals(25, list.select(2), "Select(2) dynamically shifts to 25 after insertion");
            assertEquals(40, list.select(3), "Select(3) shifts back to 40");
        }
    }

    @Nested
    @DisplayName("minimum and maximum")
    class MinimumAndMaximum {

        @Test
        @DisplayName("throw on an empty list")
        void emptyList() {
            assertThrows(NoSuchElementException.class, list::minimum, "minimum of an empty list throws");
            assertThrows(NoSuchElementException.class, list::maximum, "maximum of an empty list throws");
        }

        @Test
        @DisplayName("return the smallest and the largest element")
        void nonEmptyList() {
            insertAll(30, 10, 50, 20, 40);
            assertEquals(10, list.minimum().key(), "minimum returns the smallest element (10)");
            assertEquals(50, list.maximum().key(), "maximum returns the largest element (50)");
        }
    }

    @Nested
    @DisplayName("successor and predecessor")
    class SuccessorAndPredecessor {

        @Test
        @DisplayName("step to the neighbours of a middle element")
        void middleElement() {
            insertAll(10, 20, 30, 40, 50);
            AbstractSkipList.SkipListNode node = list.search(30);
            assertEquals(40, list.successor(node).key(), "successor of 30 is 40");
            assertEquals(20, list.predecessor(node).key(), "predecessor of 30 is 20");
        }
    }

    @Nested
    @DisplayName("200 keys inserted in a shuffled order")
    class ShuffledKeys {

        private final List<Integer> keys = new ArrayList<>();

        @BeforeEach
        void insertShuffled() {
            for (int i = 0; i < 200; i++) {
                keys.add(i * 10);
            }
            Collections.shuffle(keys, new Random(42));
            for (int key : keys) {
                list.insert(key);
            }
        }

        @Test
        @DisplayName("keeps widths, rank and select right after deleting every third key")
        void deleteEveryThird() {
            assertWidthsValid(list, "Widths correct after 200 shuffled insertions");

            List<Integer> remaining = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                if (i % 3 == 0) {
                    assertTrue(deleteKey(keys.get(i)), "Deleted key " + keys.get(i));
                } else {
                    remaining.add(keys.get(i));
                }
            }
            Collections.sort(remaining);

            assertWidthsValid(list, "Widths correct after deleting every third key");
            assertEquals(remaining.size(), list.size, "Size counts the remaining keys");
            for (int i = 0; i < remaining.size(); i++) {
                int expected = remaining.get(i);
                assertEquals(expected, list.select(i), "Select(" + i + ") matches the sorted remaining keys");
            }
            for (int key : keys) {
                int smaller = (int) remaining.stream().filter(k -> k < key).count();
                assertEquals(smaller, list.rank(key), "Rank(" + key + ") counts the remaining keys below it");
            }
        }

        @Test
        @DisplayName("returns to height 0 once every key is deleted")
        void emptiedListHasHeightZero() {
            assertTrue(list.head.height() > 0, "Setup: 200 insertions raised the list above height 0");

            for (int key : keys) {
                deleteKey(key);
            }

            assertEquals(0, list.size, "Size is 0 after deleting all 200 keys");
            assertEquals(0, list.head.height(), "Head is back to height 0");
            assertEquals(0, list.tail.height(), "Tail is back to height 0");
            assertWidthsValid(list, "Widths correct after emptying the list");
        }
    }
}
