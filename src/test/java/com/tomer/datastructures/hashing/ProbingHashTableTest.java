package com.tomer.datastructures.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProbingHashTable (Task 3.4)")
class ProbingHashTableTest {

    @Nested
    @DisplayName("with ModularHash and Integer keys")
    class WithModularHash {

        private final ProbingHashTable<Integer, String> table = new ProbingHashTable<>(new ModularHash(), 2, 0.75);

        private void insertTenTwentyThirty() {
            table.insert(10, "Value_10");
            table.insert(20, "Value_20");
            table.insert(30, "Value_30");
        }

        @Test
        @DisplayName("starts with capacity 2^k")
        void initialCapacity() {
            assertEquals(4, table.capacity(), "Initial capacity is correctly set to 4 (2^2)");
        }

        @Test
        @DisplayName("finds inserted keys and misses absent ones")
        void search() {
            insertTenTwentyThirty();
            assertEquals("Value_20", table.search(20), "Search successfully finds existing Integer key");
            assertNull(table.search(99), "Search returns null for non-existing key");
        }

        @Test
        @DisplayName("deletes a present key")
        void delete() {
            insertTenTwentyThirty();
            assertTrue(table.delete(20), "Delete returns true for existing key");
            assertNull(table.search(20), "Key 20 is no longer found after deletion");
        }

        @Test
        @DisplayName("grows and keeps every live key")
        void growth() {
            insertTenTwentyThirty();
            table.delete(20);
            table.insert(40, "Value_40");
            table.insert(50, "Value_50");

            assertEquals(8, table.capacity(), "Capacity is 8: the table doubled at insert 30, when the load reached 0.75");
            assertEquals("Value_10", table.search(10), "Every live key is found after the resize (key 10)");
            assertEquals("Value_30", table.search(30), "Every live key is found after the resize (key 30)");
            assertEquals("Value_40", table.search(40), "Every live key is found after the resize (key 40)");
            assertEquals("Value_50", table.search(50), "Every live key is found after the resize (key 50)");
            assertNull(table.search(20), "Key 20, deleted after the resize, stays absent through the later inserts");
        }
    }

    @Nested
    @DisplayName("with MultiplicativeShiftingHash and Long keys")
    class WithMultiplicativeHash {

        private final ProbingHashTable<Long, String> table = new ProbingHashTable<>(new MultiplicativeShiftingHash(), 3, 0.5);

        private void insertHundreds() {
            table.insert(100L, "Cent");
            table.insert(200L, "Deux Cents");
            table.insert(300L, "Trois Cents");
            table.insert(400L, "Quatre Cents");
        }

        @Test
        @DisplayName("starts with capacity 2^k")
        void initialCapacity() {
            assertEquals(8, table.capacity(), "Initial capacity is correctly set to 8 (2^3)");
        }

        @Test
        @DisplayName("finds an inserted key")
        void search() {
            insertHundreds();
            assertEquals("Trois Cents", table.search(300L), "Search successfully finds Long key using Bitwise Shift");
        }

        @Test
        @DisplayName("no longer finds deleted keys")
        void delete() {
            insertHundreds();
            table.delete(200L);
            table.delete(300L);

            assertNull(table.search(200L), "Multiple Long keys successfully deleted (key 200)");
            assertNull(table.search(300L), "Multiple Long keys successfully deleted (key 300)");
        }

        @Test
        @DisplayName("grows and keeps every live key")
        void growth() {
            insertHundreds();
            table.delete(200L);
            table.delete(300L);
            table.insert(500L, "Cinq Cents");
            table.insert(600L, "Six Cents");
            table.insert(700L, "Sept Cents");

            assertEquals(16, table.capacity(), "Capacity is 16: the table doubled at insert 400, when the load reached 0.5");
            assertEquals("Cent", table.search(100L), "Every live Long key is found after the resize (key 100)");
            assertEquals("Quatre Cents", table.search(400L), "Every live Long key is found after the resize (key 400)");
            assertEquals("Sept Cents", table.search(700L), "Every live Long key is found after the resize (key 700)");
        }
    }

    @Nested
    @DisplayName("with every key hashed to slot 0, so all keys share one probe chain")
    class WhenEveryKeyCollides {

        // k = 3 and load factor 0.75: the first resize comes at the sixth insert, after these tests.
        private final ProbingHashTable<Integer, String> table =
                new ProbingHashTable<>(new FixedHashFactory<>((key, m) -> 0), 3, 0.75);

        private void insertAll(int... keys) {
            for (int key : keys) {
                table.insert(key, "Value_" + key);
            }
        }

        @Test
        @DisplayName("skips a DELETED cell to find the key after it")
        void skipsADeletedCell() {
            insertAll(10, 20, 30);
            table.delete(20);
            assertEquals("Value_30", table.search(30), "Search correctly skips over DELETED cells to find subsequent keys");
        }

        @Test
        @DisplayName("skips a run of DELETED cells to find the key after them")
        void skipsARunOfDeletedCells() {
            insertAll(10, 20, 30, 40);
            table.delete(20);
            table.delete(30);
            assertEquals("Value_40", table.search(40), "Search successfully navigates through a sequence of multiple DELETED cells");
        }

        @Test
        @DisplayName("inserts into a chain that holds a DELETED cell")
        void insertAfterDelete() {
            insertAll(10, 20, 30);
            table.delete(20);
            table.insert(40, "Value_40");
            assertEquals("Value_40", table.search(40), "Insert successfully functions when table contains DELETED cells");
        }
    }

    @Nested
    @DisplayName("resizing after a delete, with key mod m as the hash")
    class ResizeAfterDelete {

        // k = 2 and load factor 0.75: keys 0 to 3 land in slots 0 to 3, and the insert that would
        // bring the load to 3/4 doubles the table first.
        private final ProbingHashTable<Integer, String> table =
                new ProbingHashTable<>(new FixedHashFactory<>((key, m) -> Math.floorMod(key, m)), 2, 0.75);

        @Test
        @DisplayName("drops the DELETED cell and keeps every live key")
        void dropsDeletedCells() {
            table.insert(0, "Value_0");
            table.insert(1, "Value_1");
            table.delete(1);
            table.insert(2, "Value_2");
            assertEquals(4, table.capacity(), "Capacity is still 4 before the insert that brings the load to 0.75");

            // Slot 1 still holds the DELETED marker. A resize that copied it would hash its null key and throw.
            table.insert(3, "Value_3");

            assertEquals(8, table.capacity(), "Capacity doubled to 8 on the insert that brought the load to 0.75");
            assertNull(table.search(1), "Deleted key 1 is not found after the resize");
            assertEquals("Value_0", table.search(0), "Every live key is found after the resize (key 0)");
            assertEquals("Value_2", table.search(2), "Every live key is found after the resize (key 2)");
            assertEquals("Value_3", table.search(3), "Every live key is found after the resize (key 3)");
        }
    }
}
