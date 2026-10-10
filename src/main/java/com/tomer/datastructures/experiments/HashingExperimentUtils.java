package com.tomer.datastructures.experiments;

import com.tomer.datastructures.hashing.ChainedHashTable;
import com.tomer.datastructures.hashing.HashFactory;
import com.tomer.datastructures.hashing.HashTable;
import com.tomer.datastructures.hashing.HashingUtils;
import com.tomer.datastructures.hashing.ModularHash;
import com.tomer.datastructures.hashing.ProbingHashTable;

import java.util.List;

/**
 * Timing experiments for the two hash tables (Tasks 3.5 - 3.8): the average time of an insert and of
 * a search at several load factors, with {@link ModularHash} and tables of {@link #CAPACITY} slots
 * that never resize mid-run. Timings depend on the machine and vary between runs.
 */
public class HashingExperimentUtils {
    // Written by the timed loops, so the JIT cannot drop their work as unused.
    private static volatile long sink;
    final private static int K = 16;
    /** The number of slots in every table, 2^16. A run's load factor sets how many keys it inserts. */
    final static int CAPACITY = 1 << K;
    final private static int NUM_OF_EXPERIMENTS = 30;
    final private static int WARMUP_ITERATIONS = 5;

    /** The load factors the probing table is measured at, all below 1. */
    final static List<Alpha> PROBING_ALPHAS =
            List.of(new Alpha(1, 2), new Alpha(3, 4), new Alpha(7, 8), new Alpha(15, 16));
    /** The load factors the chaining table is measured at, up to 2. */
    final static List<Alpha> CHAINING_ALPHAS =
            List.of(new Alpha(1, 2), new Alpha(3, 4), new Alpha(1, 1), new Alpha(3, 2), new Alpha(2, 1));

    /**
     * A load factor written as a fraction, so its value and its label come from the same numbers.
     *
     * @param numerator the fraction's numerator
     * @param denominator the fraction's denominator, positive
     */
    record Alpha(int numerator, int denominator) {
        double value() {
            return (double) numerator / denominator;
        }

        /**
         * The fraction, then the decimal, each padded so the rows line up: "7/8   (0.875) ".
         *
         * @return the label, at least 14 characters wide
         */
        String label() {
            String fraction = denominator == 1 ? String.valueOf(numerator) : numerator + "/" + denominator;
            return String.format("%-6s%-8s", fraction, "(" + value() + ")");
        }
    }

    /** The kind of hash table a run measures. */
    enum TableKind {
        // Max load factors no run reaches, so no table resizes mid-run and every timing is taken at its
        // intended alpha: a probing table's load stays below 1, and the highest chaining alpha is 2.
        PROBING(2.0),
        CHAINING(10.0);

        private final double maxLoadFactor;

        TableKind(double maxLoadFactor) {
            this.maxLoadFactor = maxLoadFactor;
        }

        HashTable<Integer, Integer> newTable(HashFactory<Integer> factory) {
            return switch (this) {
                case PROBING -> new ProbingHashTable<>(factory, K, maxLoadFactor);
                case CHAINING -> new ChainedHashTable<>(factory, K, maxLoadFactor);
            };
        }
    }

    // ==========================================
    // Public API Methods (Tasks 3.5 - 3.8)
    // ==========================================

    /**
     * Measures inserts into a probing table at each load factor in {@link #PROBING_ALPHAS}. Each of 30
     * runs fills a new table to that load with random distinct keys and times only the last 100
     * inserts. Takes a few seconds.
     *
     * @return the average nanoseconds per insert, one per load factor, in list order
     */
    public static double[] measureInsertionsProbing() {
        return measureInsertions(PROBING_ALPHAS, TableKind.PROBING);
    }

    /**
     * Measures searches in a probing table at each load factor in {@link #PROBING_ALPHAS}. Each of 30
     * runs fills a new table to that load with random distinct keys, then times a search for every
     * stored key and for as many keys that are not stored. Takes a few seconds.
     *
     * @return the average nanoseconds per search, one per load factor, in list order
     */
    public static double[] measureSearchesProbing() {
        return measureSearches(PROBING_ALPHAS, TableKind.PROBING);
    }

    /**
     * Measures inserts into a chaining table at each load factor in {@link #CHAINING_ALPHAS}, the same
     * way as {@link #measureInsertionsProbing}.
     *
     * @return the average nanoseconds per insert, one per load factor, in list order
     */
    public static double[] measureInsertionsChaining() {
        return measureInsertions(CHAINING_ALPHAS, TableKind.CHAINING);
    }

    /**
     * Measures searches in a chaining table at each load factor in {@link #CHAINING_ALPHAS}, the same
     * way as {@link #measureSearchesProbing}.
     *
     * @return the average nanoseconds per search, one per load factor, in list order
     */
    public static double[] measureSearchesChaining() {
        return measureSearches(CHAINING_ALPHAS, TableKind.CHAINING);
    }

    // ============================
    // Private Helper Methods
    // ============================

    private static double[] measureInsertions(List<Alpha> alphas, TableKind kind) {
        double[] results = new double[alphas.size()];
        HashingUtils utils = new HashingUtils();
        HashFactory<Integer> factory = new ModularHash();

        // --- PRE-HEATING ---
        for (int w = 0; w < WARMUP_ITERATIONS; w++) {
            for (Alpha alpha : alphas) {
                int elements = (int)(CAPACITY * alpha.value());
                HashTable<Integer, Integer> table = kind.newTable(factory);
                for (int j = 0; j < elements; j++){
                    table.insert(j, j);
                    sink += 1;
                }
            }
        }
        // --------------------------------

        for (int i = 0; i < alphas.size(); i++) {
            double alpha = alphas.get(i).value();
            int elementsToInsert = (int) (CAPACITY * alpha);
            int itemsToMeasure = 100;
            int elementsToWarmup = elementsToInsert - itemsToMeasure;

            double totalNanoTime = 0;

            for (int exp = 0; exp < NUM_OF_EXPERIMENTS; exp++) {
                Integer[] keys = utils.genUniqueIntegers(elementsToInsert);
                HashTable<Integer, Integer> table = kind.newTable(factory);

                for (int j = 0; j < elementsToWarmup; j++) table.insert(keys[j], keys[j]);

                long startTime = System.nanoTime();
                for (int j = elementsToWarmup; j < elementsToInsert; j++) table.insert(keys[j], keys[j]);
                long endTime = System.nanoTime();

                totalNanoTime += (double)(endTime - startTime) / itemsToMeasure;
            }
            results[i] = totalNanoTime / NUM_OF_EXPERIMENTS;
        }
        return results;
    }

    private static double[] measureSearches(List<Alpha> alphas, TableKind kind) {
        double[] results = new double[alphas.size()];
        HashingUtils utils = new HashingUtils();
        HashFactory<Integer> factory = new ModularHash();

        // --- PRE-HEATING ---
        for (int w = 0; w < WARMUP_ITERATIONS; w++) {
            for (Alpha alpha : alphas) {
                int elements = (int)(CAPACITY * alpha.value());
                HashTable<Integer, Integer> table = kind.newTable(factory);
                for (int j = 0; j < elements; j++) {
                    table.insert(j, j);
                    Integer res = table.search(j);
                    if (res != null) sink += res;
                }
            }
        }
        // --------------------------------

        for (int i = 0; i < alphas.size(); i++) {
            double alpha = alphas.get(i).value();
            int elementsInTable = (int) (CAPACITY * alpha);
            int totalKeysToGenerate = elementsInTable * 2;
            double totalNanoTime = 0;

            for (int exp = 0; exp < NUM_OF_EXPERIMENTS; exp++) {
                Integer[] keys = utils.genUniqueIntegers(totalKeysToGenerate);
                HashTable<Integer, Integer> table = kind.newTable(factory);

                for (int j = 0; j < elementsInTable; j++) table.insert(keys[j], keys[j]);

                long startTime = System.nanoTime();
                for (int j = 0; j < totalKeysToGenerate; j++) {
                    Integer res = table.search(keys[j]);
                    if (res != null) sink += res;
                }
                long endTime = System.nanoTime();

                totalNanoTime += (double)(endTime - startTime) / totalKeysToGenerate;
            }
            results[i] = totalNanoTime / NUM_OF_EXPERIMENTS;
        }
        return results;
    }
}
