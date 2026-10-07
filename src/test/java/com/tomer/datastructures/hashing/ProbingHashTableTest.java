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
        @DisplayName("deletes a key and still finds the keys after it")
        void delete() {
            insertTenTwentyThirty();
            assertTrue(table.delete(20), "Delete returns true for existing key");
            assertNull(table.search(20), "Key 20 is no longer found after deletion");
            assertEquals("Value_30", table.search(30), "Search correctly skips over DELETED cells to find subsequent keys");
        }

        @Test
        @DisplayName("inserts into a table that holds DELETED cells")
        void insertAfterDelete() {
            insertTenTwentyThirty();
            table.delete(20);
            table.insert(40, "Value_40");
            assertEquals("Value_40", table.search(40), "Insert successfully functions when table contains DELETED cells");
        }

        @Test
        @DisplayName("grows and keeps every live key")
        void growth() {
            insertTenTwentyThirty();
            table.delete(20);
            table.insert(40, "Value_40");
            table.insert(50, "Value_50");

            assertEquals(8, table.capacity(), "Table successfully doubled capacity to 8 after exceeding load factor");
            assertEquals("Value_10", table.search(10), "All live elements successfully migrated and are searchable after rehash (key 10)");
            assertEquals("Value_30", table.search(30), "All live elements successfully migrated and are searchable after rehash (key 30)");
            assertEquals("Value_40", table.search(40), "All live elements successfully migrated and are searchable after rehash (key 40)");
            assertEquals("Value_50", table.search(50), "All live elements successfully migrated and are searchable after rehash (key 50)");
            assertNull(table.search(20), "Deleted elements were correctly pruned during rehash");
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
        @DisplayName("deletes several keys and still finds the keys after them")
        void delete() {
            insertHundreds();
            table.delete(200L);
            table.delete(300L);

            assertNull(table.search(200L), "Multiple Long keys successfully deleted (key 200)");
            assertNull(table.search(300L), "Multiple Long keys successfully deleted (key 300)");
            assertEquals("Quatre Cents", table.search(400L), "Search successfully navigates through a sequence of multiple DELETED cells");
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

            assertEquals(16, table.capacity(), "Table successfully expanded to 16 after bulk Long insertions");
            assertEquals("Cent", table.search(100L), "All Long keys accurately re-hashed into the expanded table (key 100)");
            assertEquals("Quatre Cents", table.search(400L), "All Long keys accurately re-hashed into the expanded table (key 400)");
            assertEquals("Sept Cents", table.search(700L), "All Long keys accurately re-hashed into the expanded table (key 700)");
        }
    }
}
