package com.tomer.datastructures.hashing;

import com.tomer.datastructures.core.Pair;

import java.math.BigInteger;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Random numbers and modular arithmetic for the hash functions: random longs and primes in a range,
 * distinct random integers, and a remainder that is never negative. Each instance draws from its own
 * {@link Random}.
 */
public class HashingUtils {
    // Rounds of the Miller-Rabin test that genPrime runs on each candidate. A composite passes all of
    // them with probability at most 4^-rounds.
    private static final int MILLER_RABIN_ROUNDS = 50;

    final private Random rand;

    /**
     * Creates an instance with its own random source, seeded differently from every other instance,
     * even one made at the same moment.
     */
    public HashingUtils() {
        rand = new Random();
    }

    /**
     * Returns x modulo m, in [0, m), where {@code x % m} would be negative for a negative x.
     *
     * @param x - The value to reduce; any long.
     * @param m - The modulus. Must be positive: with a negative m the result can fall outside [0, |m|).
     * @return x mod m, in [0, m).
     * @throws ArithmeticException - If m is 0.
     */
    public static long mod(long x, long m) {
        long res = x % m;

        return (res < 0) ? res + m : res;
    }

    /**
     * Returns x modulo m, in [0, m), where {@code x % m} would be negative for a negative x.
     *
     * @param x - The value to reduce; any int.
     * @param m - The modulus. Must be positive: with a negative m the result can fall outside [0, |m|).
     * @return x mod m, in [0, m).
     * @throws ArithmeticException - If m is 0.
     */
    public static int mod(int x, int m) {
        int res = x % m;

        return (res < 0) ? res + m : res;
    }

    /***
     * Generates random prime in the range [lower, higher].
     * It draws odd candidates with genLong until one passes MILLER_RABIN_ROUNDS rounds of the
     * Miller-Rabin test, so it returns a composite with probability at most 4^-50, and never 2. The
     * range must hold an odd prime, or it never returns.
     * @param lower - a lower bound for the returned value, at least 3
     * @param higher - an upper bound for the returned value
     * @return a random prime, between lower and higher
     * @throws IllegalArgumentException - If lower is below 3 or above higher.
     */
    public long genPrime(long lower, long higher) {
        // A candidate below 3 would leave the Miller-Rabin test no base to draw from [2, candidate - 1].
        if (lower < 3) {
            throw new IllegalArgumentException("lower must be at least 3. Received: " + lower);
        }
        long suspectPrime = genLong(lower,higher);
        while (((suspectPrime & 1) == 0) || !runMillerRabinTest(suspectPrime, MILLER_RABIN_ROUNDS)){
            suspectPrime = genLong(lower,higher);
        }
        return suspectPrime;
    }

    /***
     * Generates a random long in the range [lower, higher], uniformly, with both bounds included.
     * @param lower - a lower bound for the returned value
     * @param higher - an upper bound for the returned value
     * @return a random long, between lower and higher
     * @throws IllegalArgumentException - If lower is above higher.
     */
    public long genLong(long lower, long higher) {
        if (lower > higher) {
            throw new IllegalArgumentException(
                    "lower must not be above higher. Received: [" + lower + ", " + higher + "]");
        }
        // nextLong(origin, bound) leaves out bound, so an upper bound of Long.MAX_VALUE is reached by
        // shifting the range down by one, or, for the whole range, by drawing any long.
        if (higher < Long.MAX_VALUE) {
            return rand.nextLong(lower, higher + 1);
        }
        if (lower > Long.MIN_VALUE) {
            return rand.nextLong(lower - 1, higher) + 1;
        }
        return rand.nextLong();
    }

    /**
     * Draws distinct integers uniformly from [0, Integer.MAX_VALUE), in the order they were drawn.
     *
     * @param numOfItemsToGen - How many to draw; 0 gives an empty array.
     * @return numOfItemsToGen distinct integers.
     * @throws IllegalArgumentException - If numOfItemsToGen is negative.
     */
    public Integer[] genUniqueIntegers(int numOfItemsToGen) {
        return Stream.generate(() -> rand.ints(0, Integer.MAX_VALUE))
                .flatMap(IntStream::boxed)
                .distinct()
                .limit(numOfItemsToGen)
                .toArray(Integer[]::new);
    }

    // Splits num into 2^s * d with d odd, and returns (s, d). num must not be 0: it would never become odd.
    private static Pair<Integer, Long> calculateEvenDivisorSplit(long num) {
        int s = 0;
        while ((num & 1) == 0) {
            num >>>= 1;
            ++s;
        }

        return new Pair<>(s, num);
    }

    /***
     * Multiplies a by b at modulo mod, using BigInteger so the product cannot overflow a long.
     * @param a - The first factor
     * @param b - The second factor
     * @param mod - The intended modulo of the value
     * @return (a * b) % mod
     */
    private static long multiplyMod(long a, long b, long mod) {
        final BigInteger aBig = BigInteger.valueOf(a);
        final BigInteger bBig = BigInteger.valueOf(b);
        final BigInteger multiplyRes = aBig.multiply(bBig);
        final BigInteger modBig = BigInteger.valueOf(mod);

        return multiplyRes.mod(modBig).longValue();
    }

    /***
     * Evaluates the power of a at b, done at modulo mod in an efficient manner,
     * using the characteristics of modulo: (x * y) mod n = ((x mod n) * (y mod n)) mod n
     * @param a - The basis
     * @param b - The power
     * @param mod - The intended modulo of the value
     * @return (a ^ b) % mod
     */
    private static long modPow(long a, long b, long mod) {
        final BigInteger aBig = BigInteger.valueOf(a);
        final BigInteger bBig = BigInteger.valueOf(b);
        final BigInteger modBig = BigInteger.valueOf(mod);
        final BigInteger res = aBig.modPow(bBig, modBig);

        return res.longValue();
    }


    /**
     * An implementation of the Rabin-Miller probabilistic primality test as defined in the following link:
     * <a href="https://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test#Miller%E2%80%93Rabin_test">...</a>
     * This process requires theta of (rounds * log(suspect) ^ 3)
     * @param suspect - The number suspected of being prime, assuming suspect isn't even.
     * @param rounds - The number of rounds to run the test. genPrime passes {@code MILLER_RABIN_ROUNDS}.
     * @return True if suspect is probably prime with a false positive p of 4^(-rounds)
     */
    private boolean runMillerRabinTest(long suspect, int rounds) {
        long[] bases = new long[rounds];
        for (int i = 0; i < rounds; ++i) {
            bases[i] = genLong(2L, suspect - 1);
        }

        return passesMillerRabin(suspect, bases);
    }

    /**
     * Runs one round of the Miller-Rabin test per base. The caller picks the bases, so the test can be
     * checked against known values.
     * @param suspect - The number suspected of being prime, assuming suspect isn't even.
     * @param bases - The bases to test with, each in [2, suspect - 1].
     * @return True if no base shows that suspect is composite
     */
    static boolean passesMillerRabin(long suspect, long... bases) {
        Pair<Integer, Long> split = calculateEvenDivisorSplit(suspect - 1);
        final int s = split.first();
        final long d = split.second();

        for (long a : bases) {
            long x = modPow(a, d, suspect);
            long y = 1;

            for (int j = 0; j < s; ++j) {
                y = multiplyMod(x, x, suspect);

                if (y == 1 && x != 1 && x != suspect - 1) {
                    return false;
                }

                x = y;
            }

            // y is now a^(suspect - 1) mod suspect. For a prime it is 1 (Fermat), for every base.
            if (y != 1) {
                return false;
            }
        }

        return true;
    }
}
