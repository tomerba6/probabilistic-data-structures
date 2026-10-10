package com.tomer.datastructures.hashing;

import java.util.Random;

/**
 * A {@link HashFactory} for {@code Integer} keys. Each function it draws computes
 * ((a * x + b) mod p) mod m for a key x, with a and b random ints, p a random prime of at least
 * 2^31 - 1, and m = 2^k. Each draw generates a new prime, which is the slow part of drawing.
 */
public class ModularHash implements HashFactory<Integer> {
    private Random rand;
    private HashingUtils utils;

    /**
     * Creates a factory with its own random sources for a, b and p.
     */
    public ModularHash() {
        rand = new Random();
        utils = new HashingUtils();
    }

    @Override
    public HashFunctor<Integer> pickHash(int k) {
        return new Functor(k);
    }

    /**
     * One function from the family, ((a * x + b) mod p) mod m, with its parameters fixed when it is
     * made. Usually obtained from {@link ModularHash#pickHash}.
     */
    public class Functor implements HashFunctor<Integer> {
        final private int a;
        final private int b;
        final private long p;
        final private int m;

        /**
         * Draws a, b and p at random, for a table of 2^k slots.
         *
         * @param k - The table has 2^k slots, with {@code 0 <= k <= MAX_K}.
         * @throws IllegalArgumentException - If k is negative or above {@link HashFactory#MAX_K}.
         */
        public Functor(int k){
            if (k < 0 || k > MAX_K) {
                throw new IllegalArgumentException("k must be between 0 and " + MAX_K + ". Received: " + k);
            }
            this.a = rand.nextInt(Integer.MAX_VALUE - 1) + 1;
            this.b = rand.nextInt(Integer.MAX_VALUE);
            this.p = utils.genPrime(Integer.MAX_VALUE, Long.MAX_VALUE);
            this.m = 1 << k;
        }

        @Override
        public int hash(Integer key) {
            return (int)HashingUtils.mod(HashingUtils.mod(((long)a * key + b), p), m);
        }

        /**
         * Returns the multiplier a.
         *
         * @return a, in [1, Integer.MAX_VALUE - 1].
         */
        public int a() {
            return a;
        }

        /**
         * Returns the offset b.
         *
         * @return b, in [0, Integer.MAX_VALUE - 1].
         */
        public int b() {
            return b;
        }

        /**
         * Returns the prime p.
         *
         * @return p, a prime in [Integer.MAX_VALUE, Long.MAX_VALUE].
         */
        public long p() {
            return p;
        }

        /**
         * Returns the number of slots m.
         *
         * @return m = 2^k.
         */
        public int m() {
            return m;
        }
    }
}
