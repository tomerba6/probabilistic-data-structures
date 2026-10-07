package com.tomer.datastructures.composite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyDataStructure (Task 4.1)")
class MyDataStructureTest {

    @Nested
    @DisplayName("insert and contains")
    class InsertAndContains {

        private final MyDataStructure ds = new MyDataStructure(10);

        @Test
        @DisplayName("contains nothing when empty")
        void emptyStructure() {
            assertFalse(ds.contains(5), "Contains on empty DS should be false");
        }

        @Test
        @DisplayName("inserts values and then contains them")
        void insertThenContains() {
            assertTrue(ds.insert(5), "Insert 5 should return true");
            assertTrue(ds.contains(5), "Contains 5 should now be true");
            assertTrue(ds.insert(10), "Insert 10 should return true");
            assertTrue(ds.contains(10), "Contains 10 should be true");
        }
    }

    @Nested
    @DisplayName("capacity and duplicates")
    class CapacityAndDuplicates {

        private final MyDataStructure ds = new MyDataStructure(3);

        @Test
        @DisplayName("rejects a duplicate")
        void duplicate() {
            assertTrue(ds.insert(1), "Insert 1");
            assertTrue(ds.insert(2), "Insert 2");
            assertFalse(ds.insert(2), "Insert duplicate 2 should return false");
        }

        @Test
        @DisplayName("rejects an insert beyond N")
        void beyondCapacity() {
            assertTrue(ds.insert(1), "Insert 1");
            assertTrue(ds.insert(2), "Insert 2");
            ds.insert(2);
            assertTrue(ds.insert(3), "Insert 3");

            assertFalse(ds.insert(4), "Insert 4 beyond capacity should return false");
            assertFalse(ds.contains(4), "Contains 4 should be false");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        private final MyDataStructure ds = new MyDataStructure(10);

        @BeforeEach
        void insertTenTwentyThirty() {
            ds.insert(10);
            ds.insert(20);
            ds.insert(30);
        }

        @Test
        @DisplayName("reports a value that is not present")
        void absentValue() {
            assertFalse(ds.delete(15), "Delete non-existing (15) should return false");
        }

        @Test
        @DisplayName("removes a present value and keeps the rest")
        void presentValue() {
            assertTrue(ds.delete(20), "Delete existing (20) should return true");
            assertFalse(ds.contains(20), "Contains 20 after deletion should be false");
            assertTrue(ds.contains(10), "Contains 10 should still be true");
            assertTrue(ds.contains(30), "Contains 30 should still be true");
        }

        @Test
        @DisplayName("accepts new inserts afterwards")
        void insertAfterDelete() {
            ds.delete(20);
            assertTrue(ds.insert(40), "Insert 40 should be successful after delete");
        }
    }

    @Nested
    @DisplayName("rank and select")
    class RankAndSelect {

        private final MyDataStructure ds = new MyDataStructure(10);

        @BeforeEach
        void insertOutOfOrder() {
            ds.insert(50);
            ds.insert(20);
            ds.insert(10);
            ds.insert(30);
            ds.insert(40);
        }

        @Test
        @DisplayName("rank counts the values below the argument")
        void rank() {
            assertEquals(0, ds.rank(10), "Rank of 10 (smallest) should be 0");
            assertEquals(3, ds.rank(35), "Rank of 35 (not in DS) should be 3 (10,20,30 are smaller)");
            assertEquals(5, ds.rank(60), "Rank of 60 (larger than all) should be 5");
        }

        @Test
        @DisplayName("select returns the value at each index")
        void select() {
            assertEquals(10, ds.select(0), "Select index 0 should be 10");
            assertEquals(30, ds.select(2), "Select index 2 should be 30");
            assertEquals(50, ds.select(4), "Select index 4 should be 50");
        }
    }

    @Nested
    @DisplayName("range")
    class Range {

        private final MyDataStructure ds = new MyDataStructure(10);

        @BeforeEach
        void insertOutOfOrder() {
            ds.insert(15);
            ds.insert(5);
            ds.insert(25);
            ds.insert(35);
            ds.insert(45);
        }

        @Test
        @DisplayName("returns the values from low up to high")
        void lowToHigh() {
            assertEquals(List.of(15, 25, 35), ds.range(15, 40), "Range(15, 40) should return [15, 25, 35]");
        }

        @Test
        @DisplayName("returns just low when low equals high")
        void singleValue() {
            assertEquals(List.of(25), ds.range(25, 25), "Range(25, 25) should return [25]");
        }

        @Test
        @DisplayName("returns null when low is not present")
        void absentLow() {
            assertNull(ds.range(10, 30), "Range(10, 30) where low=10 is NOT in DS should return null");
        }

        @Test
        @DisplayName("returns every value when the bounds span them all")
        void everything() {
            assertEquals(List.of(5, 15, 25, 35, 45), ds.range(5, 50), "Range(5, 50) should return all elements");
        }
    }
}
