package com.tomer.datastructures.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HashingUtils")
class HashingUtilsTest {

    @Nested
    @DisplayName("genPrime")
    class GenPrime {

        @Test
        @DisplayName("draws primes within its bounds")
        void primesWithinBounds() {
            HashingUtils utils = new HashingUtils();
            // The bounds ModularHash draws p from. genLong samples by rejection, so a narrow range
            // would take too many draws to finish.
            long lower = Integer.MAX_VALUE;
            long higher = Long.MAX_VALUE;

            for (int draw = 1; draw <= 200; draw++) {
                long prime = utils.genPrime(lower, higher);
                assertTrue(prime >= lower && prime <= higher,
                        "Draw " + draw + " (" + prime + ") is within [Integer.MAX_VALUE, Long.MAX_VALUE]");
                assertTrue(BigInteger.valueOf(prime).isProbablePrime(50),
                        "Draw " + draw + " (" + prime + ") is prime");
            }
        }
    }

    @Nested
    @DisplayName("passesMillerRabin")
    class PassesMillerRabin {

        @ParameterizedTest(name = "{0} with base {1}: {2}")
        @CsvSource({
                // 2^31 - 1 is prime.
                "2147483647, 2, true",
                // 2047 = 23 * 89, and 3^2046 mod 2047 is 1013, not 1.
                "2047, 3, false",
                // 2047 is the smallest strong pseudoprime to base 2: that base alone cannot expose it.
                "2047, 2, true",
                // 561 = 3 * 11 * 17 passes the Fermat check for base 2; squaring 2^35 exposes a square root
                // of 1 other than 1 and 560.
                "561, 2, false",
        })
        @DisplayName("gives the known verdict for one base")
        void oneBase(long suspect, long base, boolean expected) {
            assertEquals(expected, HashingUtils.passesMillerRabin(suspect, base),
                    suspect + " with base " + base + " passes: " + expected);
        }

        @Test
        @DisplayName("fails when any base exposes the suspect, not only the last one")
        void witnessBeforeALiar() {
            // Base 3 exposes 2047 and base 2 cannot, so 2047 fails in either order.
            assertFalse(HashingUtils.passesMillerRabin(2047, 2, 3), "2047 with bases 2 then 3 fails");
            assertFalse(HashingUtils.passesMillerRabin(2047, 3, 2), "2047 with bases 3 then 2 fails");
        }
    }

    @Nested
    @DisplayName("mod")
    class Mod {

        @ParameterizedTest(name = "mod({0}, {1}) = {2}")
        @CsvSource({
                "9, 8, 1",
                "0, 8, 0",
                "-1, 8, 7",
                "-8, 8, 0",
                "-9, 8, 7",
                "-2147483648, 8, 0",
                "-2147483648, 3, 1",
        })
        @DisplayName("int version: lands in [0, m) for negative x too")
        void intMod(int x, int m, int expected) {
            assertEquals(expected, HashingUtils.mod(x, m), "mod(" + x + ", " + m + ") is " + expected);
        }

        @ParameterizedTest(name = "mod({0}, {1}) = {2}")
        @CsvSource({
                "9, 8, 1",
                "0, 8, 0",
                "-1, 8, 7",
                "-8, 8, 0",
                "-9, 8, 7",
                "-9223372036854775808, 8, 0",
                "-9223372036854775808, 3, 1",
                "-1, 9223372036854775807, 9223372036854775806",
        })
        @DisplayName("long version: lands in [0, m) for negative x too")
        void longMod(long x, long m, long expected) {
            assertEquals(expected, HashingUtils.mod(x, m), "mod(" + x + ", " + m + ") is " + expected);
        }
    }
}
