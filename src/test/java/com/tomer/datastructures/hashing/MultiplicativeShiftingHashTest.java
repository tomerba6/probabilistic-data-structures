package com.tomer.datastructures.hashing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MultiplicativeShiftingHash (DHKP, Task 3.2)")
class MultiplicativeShiftingHashTest {

    private final MultiplicativeShiftingHash factory = new MultiplicativeShiftingHash();

    @Nested
    @DisplayName("pickHash(k) argument checks")
    class ArgumentChecks {

        @Test
        @DisplayName("rejects k below 0")
        void negativeK() {
            assertThrows(IllegalArgumentException.class, () -> factory.pickHash(-1),
                    "Throws IllegalArgumentException for k < 0");
        }

        @Test
        @DisplayName("rejects k above 30")
        void kAboveThirty() {
            assertThrows(IllegalArgumentException.class, () -> factory.pickHash(31),
                    "Throws IllegalArgumentException for k > 30");
        }
    }

    @Nested
    @DisplayName("a function picked with k = 10")
    class PickedFunction {

        private final int k = 10;
        private final int m = 1 << k;
        private MultiplicativeShiftingHash.Functor functor;

        @BeforeEach
        void pick() {
            functor = (MultiplicativeShiftingHash.Functor) factory.pickHash(k);
        }

        @Test
        @DisplayName("stores a and k")
        void parameters() {
            assertTrue(functor.a() > 1, "Parameter 'a' is strictly greater than 1");
            assertEquals(k, functor.k(), "Parameter 'k' is stored correctly");
        }

        @ParameterizedTest(name = "x = {0}")
        @ValueSource(longs = {42L, Long.MIN_VALUE, Long.MAX_VALUE})
        @DisplayName("computes the top k bits of (a * x) mod 2^w, checked with BigInteger")
        void formula(long key) {
            BigInteger wordModulus = BigInteger.ONE.shiftLeft(64);

            int expectedHash = BigInteger.valueOf(functor.a()).multiply(BigInteger.valueOf(key))
                    .mod(wordModulus).shiftRight(64 - k).intValueExact();

            assertEquals(expectedHash, functor.hash(key), "Hash function accurately calculates (a * x) >>> (w - k) (x = " + key + ")");
        }

        @Test
        @DisplayName("hashes positive, negative, large and zero keys into [0, m)")
        void range() {
            int hash1 = functor.hash(42L);
            int hash2 = functor.hash(-999999999L);
            int hash3 = functor.hash(Long.MAX_VALUE - 5);
            int hash4 = functor.hash(0L);

            assertTrue(hash1 >= 0 && hash1 < m, "Hash result for normal positive key is a valid index [0, m-1]");
            assertTrue(hash2 >= 0 && hash2 < m, "Hash result for negative key is a positive valid index [0, m-1] (Thanks to >>>)");
            assertTrue(hash3 >= 0 && hash3 < m, "Hash result for very large key is a valid index [0, m-1]");
            assertEquals(0, hash4, "Hash result for key 0 is always 0");
        }
    }

    @Nested
    @DisplayName("the edge values of k")
    class EdgeValuesOfK {

        @ParameterizedTest(name = "x = {0}")
        @ValueSource(longs = {0L, 42L, -1L, Long.MIN_VALUE, Long.MAX_VALUE})
        @DisplayName("k = 0 hashes every key to 0, the only slot")
        void kZero(long key) {
            assertEquals(0, factory.pickHash(0).hash(key), "With k = 0, key " + key + " hashes to 0");
        }

        @ParameterizedTest(name = "x = {0}")
        @ValueSource(longs = {0L, 42L, -1L, Long.MIN_VALUE, Long.MAX_VALUE})
        @DisplayName("k = 30 hashes into [0, 2^30)")
        void kThirty(long key) {
            int hash = factory.pickHash(30).hash(key);
            assertTrue(hash >= 0 && hash < (1 << 30), "With k = 30, key " + key + " hashes into [0, 2^30), got " + hash);
        }
    }
}
