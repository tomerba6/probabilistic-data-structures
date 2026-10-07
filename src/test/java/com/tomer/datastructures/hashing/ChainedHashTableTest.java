package com.tomer.datastructures.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChainedHashTable (Task 3.3)")
class ChainedHashTableTest {

    @Nested
    @DisplayName("with ModularHash and Integer keys")
    class WithModularHash {

        private final ChainedHashTable<Integer, String> table = new ChainedHashTable<>(new ModularHash(), 2, 1.5);

        private void insertTenTwentyThirty() {
            table.insert(10, "A");
            table.insert(20, "B");
            table.insert(30, "C");
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
            assertEquals("A", table.search(10), "Search successfully finds existing element");
            assertNull(table.search(99), "Search returns null for non-existing element");
        }

        @Test
        @DisplayName("deletes present keys and reports absent ones")
        void delete() {
            insertTenTwentyThirty();
            assertTrue(table.delete(30), "Delete successfully removes existing element");
            assertFalse(table.delete(99), "Delete returns false for non-existing element");
            assertNull(table.search(30), "Element is no longer found after deletion");
        }

        @Test
        @DisplayName("doubles its capacity and keeps every element")
        void rehash() {
            insertTenTwentyThirty();
            table.delete(30);
            table.insert(40, "D");
            table.insert(50, "E");
            table.insert(60, "F");
            table.insert(70, "G");

            assertEquals(8, table.capacity(), "Table successfully doubled its capacity to 8 after exceeding load factor");
            assertEquals("A", table.search(10), "All elements successfully survived the rehash process (key 10)");
            assertEquals("B", table.search(20), "All elements successfully survived the rehash process (key 20)");
            assertEquals("G", table.search(70), "All elements successfully survived the rehash process (key 70)");
        }
    }

    @Nested
    @DisplayName("with MultiplicativeShiftingHash and Long keys")
    class WithMultiplicativeHash {

        private final ChainedHashTable<Long, String> table = new ChainedHashTable<>(new MultiplicativeShiftingHash(), 2, 2.0);

        private void insertHundreds() {
            table.insert(100L, "OneHundred");
            table.insert(200L, "TwoHundred");
            table.insert(300L, "ThreeHundred");
            table.insert(400L, "FourHundred");
        }

        @Test
        @DisplayName("starts with capacity 2^k")
        void initialCapacity() {
            assertEquals(4, table.capacity(), "Initial capacity is correctly set to 4 (2^2)");
        }

        @Test
        @DisplayName("finds an inserted key")
        void search() {
            insertHundreds();
            assertEquals("TwoHundred", table.search(200L), "Search successfully finds Long key");
        }

        @Test
        @DisplayName("no longer finds a deleted key")
        void delete() {
            insertHundreds();
            table.delete(100L);
            assertNull(table.search(100L), "Successfully deleted Long key");
        }

        @Test
        @DisplayName("grows during a bulk insert and still finds keys")
        void bulkInsert() {
            insertHundreds();
            table.delete(100L);
            for (long i = 1; i <= 10; i++) {
                table.insert(i * 1000L, "Value_" + i);
            }

            assertTrue(table.capacity() >= 8, "Table successfully expanded its capacity during bulk insert");
            assertEquals("Value_5", table.search(5000L), "Search works accurately after rehashing");
        }
    }
}
