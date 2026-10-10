package com.tomer.datastructures.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
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
            // The bounds ModularHash draws p from.
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

        // The narrow-range tests run under a timeout: genPrime used to draw by rejection from every long,
        // so a narrow range never finished.
        @Test
        @DisplayName("draws primes from a small range")
        void smallRange() {
            HashingUtils utils = new HashingUtils();
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                for (int draw = 1; draw <= 100; draw++) {
                    long prime = utils.genPrime(3, 100);
                    assertTrue(prime >= 3 && prime <= 100, "Draw " + draw + " (" + prime + ") is within [3, 100]");
                    assertTrue(BigInteger.valueOf(prime).isProbablePrime(50), "Draw " + draw + " (" + prime + ") is prime");
                }
            });
        }

        @ParameterizedTest(name = "lower = {0}")
        @ValueSource(longs = {2, 0, -10})
        @DisplayName("rejects a range starting below 3")
        void lowerBelowThree(long lower) {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                    assertThrows(IllegalArgumentException.class, () -> new HashingUtils().genPrime(lower, 100),
                            "genPrime(" + lower + ", 100) is rejected"));
        }
    }

    @Nested
    @DisplayName("seeding")
    class Seeding {

        // Seeded from the current millisecond, two instances made back to back drew the same numbers.
        @Test
        @DisplayName("two instances made back to back draw different numbers")
        void instancesMadeTogether() {
            for (int pair = 1; pair <= 20; pair++) {
                HashingUtils first = new HashingUtils();
                HashingUtils second = new HashingUtils();
                assertNotEquals(first.genLong(Long.MIN_VALUE, Long.MAX_VALUE),
                        second.genLong(Long.MIN_VALUE, Long.MAX_VALUE),
                        "Pair " + pair + " drew the same first number");
            }
        }
    }

    @Nested
    @DisplayName("genLong")
    class GenLong {

        // Each test runs under a timeout: genLong used to draw by rejection from every long, so a
        // narrow range never finished.
        @Test
        @DisplayName("draws every value of a small range, and nothing outside it")
        void smallRange() {
            HashingUtils utils = new HashingUtils();
            Set<Long> seen = new HashSet<>();
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                for (int draw = 0; draw < 1000; draw++) {
                    long value = utils.genLong(5, 10);
                    assertTrue(value >= 5 && value <= 10, "Draw " + value + " is within [5, 10]");
                    seen.add(value);
                }
            });
            assertEquals(6, seen.size(), "All six values from 5 to 10 were drawn");
        }

        @Test
        @DisplayName("includes Long.MAX_VALUE when the range ends there")
        void rangeEndingAtMaxValue() {
            HashingUtils utils = new HashingUtils();
            Set<Long> seen = new HashSet<>();
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                for (int draw = 0; draw < 1000; draw++) {
                    seen.add(utils.genLong(Long.MAX_VALUE - 1, Long.MAX_VALUE));
                }
            });
            assertEquals(Set.of(Long.MAX_VALUE - 1, Long.MAX_VALUE), seen, "Both values of the range were drawn");
        }

        @Test
        @DisplayName("draws from the whole long range")
        void wholeRange() {
            HashingUtils utils = new HashingUtils();
            boolean negative = false, positive = false;
            for (int draw = 0; draw < 1000; draw++) {
                long value = utils.genLong(Long.MIN_VALUE, Long.MAX_VALUE);
                negative |= value < 0;
                positive |= value > 0;
            }
            assertTrue(negative && positive, "1000 draws over every long include negative and positive values");
        }

        @Test
        @DisplayName("rejects a lower bound above the upper bound")
        void lowerAboveHigher() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                    assertThrows(IllegalArgumentException.class, () -> new HashingUtils().genLong(10, 5),
                            "genLong(10, 5) is rejected"));
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
