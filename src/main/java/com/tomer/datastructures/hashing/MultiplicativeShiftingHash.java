package com.tomer.datastructures.hashing;

/**
 * A {@link HashFactory} for {@code Long} keys. Each function it draws multiplies the key by a random
 * odd a, letting the product wrap around in 64 bits, and keeps the product's top k bits:
 * {@code (a * x) >>> (64 - k)}, a slot in [0, 2^k). Drawing one only takes a random long, so it is
 * cheap.
 */
public class MultiplicativeShiftingHash implements HashFactory<Long> {
    private HashingUtils utils;

    /**
     * Creates a factory with its own random source for a.
     */
    public MultiplicativeShiftingHash() {
        utils = new HashingUtils();
    }

    @Override
    public HashFunctor<Long> pickHash(int k) {
        return new Functor(k);
    }

    /**
     * One function from the family, {@code (a * x) >>> (64 - k)}, with a fixed when it is made.
     * Usually obtained from {@link MultiplicativeShiftingHash#pickHash}. With k = 0 it maps every key to
     * slot 0.
     */
    public class Functor implements HashFunctor<Long> {
        /**
         * The number of bits in a long. The hash keeps the top k of the product's WORD_SIZE bits.
         */
        final public static long WORD_SIZE = 64;
        final private long a;
        final private long k;

        /**
         * Draws a at random, for a table of 2^k slots.
         *
         * @param k - The table has 2^k slots, with {@code 0 <= k <= MAX_K}.
         * @throws IllegalArgumentException - If k is negative or above {@link HashFactory#MAX_K}.
         */
        public Functor(int k) {
            if (k < 0 || k > MAX_K) {
                throw new IllegalArgumentException("k must be between 0 and " + MAX_K + ". Received: " + k);
            }
            // Multiply-shift needs an odd a: with an even one, a * 2^63 wraps around to 0, so keys x and
            // x + 2^63 would always share a slot.
            this.a = utils.genLong(2L, Long.MAX_VALUE) | 1;
            this.k = k;
        }
        @Override
        public int hash(Long key) {
            // With k = 0 there is one slot. Java shifts a long by distance mod 64, so >>> 64 would
            // not shift at all and would return the product's low bits instead.
            if (k == 0) {
                return 0;
            }
            return (int)((a * key) >>> (WORD_SIZE - k));
        }

        /**
         * Returns the multiplier a.
         *
         * @return a, odd, in [3, Long.MAX_VALUE].
         */
        public long a() {
            return a;
        }

        /**
         * Returns k, the number of top bits of the product the hash keeps.
         *
         * @return k; the table has 2^k slots.
         */
        public long k() {
            return k;
        }
    }
}
