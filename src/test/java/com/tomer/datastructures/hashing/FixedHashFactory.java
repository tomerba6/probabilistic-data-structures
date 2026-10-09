package com.tomer.datastructures.hashing;

import java.util.function.ToIntBiFunction;

/**
 * A {@link HashFactory} whose hash the test chooses, so collisions happen on every run instead of
 * depending on a random draw.
 */
final class FixedHashFactory<K> implements HashFactory<K> {

    private final ToIntBiFunction<K, Integer> slot;

    /**
     * @param slot maps a key and the table size m to the key's slot in [0, m)
     */
    FixedHashFactory(ToIntBiFunction<K, Integer> slot) {
        this.slot = slot;
    }

    @Override
    public HashFunctor<K> pickHash(int k) {
        int m = 1 << k;
        return key -> slot.applyAsInt(key, m);
    }
}
