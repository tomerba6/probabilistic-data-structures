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
     * Creates an instance seeded with the current time in milliseconds, so two instances made in the
     * same millisecond produce the same numbers.
     */
    public HashingUtils() {
        rand = new Random(System.currentTimeMillis()); // Using current time as the random seed
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
     * range must be wide and must not contain numbers below 3: genLong is slow on a narrow range, and
     * a candidate below 3 leaves no base to test with, so it never returns.
     * @param lower - a lower bound for the returned value
     * @param higher - an upper bound for the returned value
     * @return a random prime, between lower and higher
     */
    public long genPrime(long lower, long higher) {
        long suspectPrime = genLong(lower,higher);
        while (((suspectPrime & 1) == 0) || !runMillerRabinTest(suspectPrime, MILLER_RABIN_ROUNDS)){
            suspectPrime = genLong(lower,higher);
        }
        return suspectPrime;
    }

    /***
     * Generates random long values until getting a value in the range [lower, higher].
     * Each draw lands in the range with probability (higher - lower + 1) / 2^64, so the expected number
     * of draws is 2^64 divided by the size of the range: a few for the ranges the hash functions use,
     * but far too many for a narrow range. If lower is above higher, it never returns.
     * @param lower - a lower bound for the returned value
     * @param higher - an upper bound for the returned value
     * @return a random long, between lower and higher
     */
    public long genLong(long lower, long higher) {
        long value = rand.nextLong();
        while (value < lower | value > higher)
            value = rand.nextLong();
        return value;
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
