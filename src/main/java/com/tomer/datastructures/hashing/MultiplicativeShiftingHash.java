package com.tomer.datastructures.hashing;

public class MultiplicativeShiftingHash implements HashFactory<Long> {
    private HashingUtils utils;

    public MultiplicativeShiftingHash() {
        utils = new HashingUtils();
    }

    @Override
    public HashFunctor<Long> pickHash(int k) {
        return new Functor(k);
    }

    public class Functor implements HashFunctor<Long> {
        final public static long WORD_SIZE = 64;
        final private long a;
        final private long k;

        public Functor(int k) {
            if (k < 0 || k > MAX_K) {
                throw new IllegalArgumentException("k must be between 0 and " + MAX_K + ". Received: " + k);
            }
            this.a = utils.genLong(2L, Long.MAX_VALUE);
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

        public long a() {
            return a;
        }

        public long k() {
            return k;
        }
    }
}
