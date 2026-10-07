package com.tomer.datastructures.hashing;

import java.util.Random;

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
            if (k < 0 || k > 30) {
                throw new IllegalArgumentException("k must be between 0 and 30. Received: " + k);
            }
            this.a = utils.genLong(2L, Long.MAX_VALUE);
            this.k = k;
        }
        @Override
        public int hash(Long key) {
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
