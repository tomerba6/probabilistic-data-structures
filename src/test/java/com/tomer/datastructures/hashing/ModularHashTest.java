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

@DisplayName("ModularHash (Carter-Wegman, Task 3.1)")
class ModularHashTest {

    private final ModularHash factory = new ModularHash();

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
        private ModularHash.Functor functor;

        @BeforeEach
        void pick() {
            functor = (ModularHash.Functor) factory.pickHash(k);
        }

        @Test
        @DisplayName("draws its parameters within their bounds")
        void parameterBounds() {
            assertTrue(functor.a() >= 1 && functor.a() < Integer.MAX_VALUE, "Parameter 'a' is within valid bounds [1, MAX-1]");
            assertTrue(functor.b() >= 0, "Parameter 'b' is within valid bounds [0, MAX]");
            assertTrue(functor.p() > Integer.MAX_VALUE, "Parameter 'p' is strictly greater than Integer.MAX_VALUE");
            assertEquals(1 << k, functor.m(), "Parameter 'm' correctly calculated as 2^k");
        }

        @ParameterizedTest(name = "x = {0}")
        @ValueSource(ints = {42, Integer.MIN_VALUE, Integer.MAX_VALUE})
        @DisplayName("computes ((a*x + b) mod p) mod m, checked with BigInteger")
        void formula(int key) {
            BigInteger a = BigInteger.valueOf(functor.a());
            BigInteger b = BigInteger.valueOf(functor.b());
            BigInteger p = BigInteger.valueOf(functor.p());
            BigInteger m = BigInteger.valueOf(functor.m());

            int expectedHash = a.multiply(BigInteger.valueOf(key)).add(b).mod(p).mod(m).intValueExact();

            assertEquals(expectedHash, functor.hash(key), "Hash function accurately calculates ((a*x + b) mod p) mod m (x = " + key + ")");
        }

        @Test
        @DisplayName("hashes positive, negative and large keys into [0, m)")
        void range() {
            int m = functor.m();
            int hash1 = functor.hash(42);
            int hash2 = functor.hash(-500);
            int hash3 = functor.hash(9999999);

            assertTrue(hash1 >= 0 && hash1 < m, "Hash result for normal positive key is a valid array index [0, m-1]");
            assertTrue(hash2 >= 0 && hash2 < m, "Hash result for negative key is a valid array index [0, m-1]");
            assertTrue(hash3 >= 0 && hash3 < m, "Hash result for very large key is a valid array index [0, m-1]");
        }
    }
}
