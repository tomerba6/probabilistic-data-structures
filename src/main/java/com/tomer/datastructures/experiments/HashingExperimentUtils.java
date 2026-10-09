package com.tomer.datastructures.experiments;

import com.tomer.datastructures.hashing.ChainedHashTable;
import com.tomer.datastructures.hashing.HashFactory;
import com.tomer.datastructures.hashing.HashTable;
import com.tomer.datastructures.hashing.HashingUtils;
import com.tomer.datastructures.hashing.ModularHash;
import com.tomer.datastructures.hashing.ProbingHashTable;

public class HashingExperimentUtils {
    private static volatile long sink;
    final private static int k = 16;
    final private static int CAPACITY = 1 << k;
    final private static int NUM_OF_EXPERIMENTS = 30;
    final private static int WARMUP_ITERATIONS = 5;

    // ==========================================
    // Public API Methods (Tasks 3.5 - 3.8)
    // ==========================================

    public static double[] measureInsertionsProbing() {
        double[] alphas = {0.5, 0.75, 7.0/8.0, 15.0/16.0};
        return measureInsertions(alphas, true);
    }

    public static double[] measureSearchesProbing() {
        double[] alphas = {0.5, 0.75, 7.0/8.0, 15.0/16.0};
        return measureSearches(alphas, true);
    }

    public static double[] measureInsertionsChaining() {
        double[] alphas = {0.5, 0.75, 1.0, 1.5, 2.0};
        return measureInsertions(alphas, false);
    }

    public static double[] measureSearchesChaining() {
        double[] alphas = {0.5, 0.75, 1.0, 1.5, 2.0};
        return measureSearches(alphas, false);
    }

    // ============================
    // Private Helper Methods
    // ============================

    private static double[] measureInsertions(double[] alphas, boolean isProbing) {
        double[] results = new double[alphas.length];
        HashingUtils utils = new HashingUtils();
        HashFactory<Integer> factory = new ModularHash();

        // --- PRE-HEATING ---
        for (int w = 0; w < WARMUP_ITERATIONS; w++) {

            for (double alpha : alphas) {
                int elements = (int)(CAPACITY * alpha);
                HashTable<Integer, Integer> table = isProbing ?
                        new ProbingHashTable<>(factory, k, 2.0) : new ChainedHashTable<>(factory, k, 10.0);
                for (int j = 0; j < elements; j++){
                    table.insert(j, j);
                    sink += 1;
                }
            }
        }
        // --------------------------------

        for (int i = 0; i < alphas.length; i++) {
            double alpha = alphas[i];
            int elementsToInsert = (int) (CAPACITY * alpha);
            int itemsToMeasure = 100;
            int elementsToWarmup = elementsToInsert - itemsToMeasure;

            double totalNanoTime = 0;

            for (int exp = 0; exp < NUM_OF_EXPERIMENTS; exp++) {
                Integer[] keys = utils.genUniqueIntegers(elementsToInsert);
                HashTable<Integer, Integer> table = isProbing ?
                        new ProbingHashTable<>(factory, k, 2.0) : new ChainedHashTable<>(factory, k, 10.0);

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

    private static double[] measureSearches(double[] alphas, boolean isProbing) {
        double[] results = new double[alphas.length];
        HashingUtils utils = new HashingUtils();
        HashFactory<Integer> factory = new ModularHash();

        // --- PRE-HEATING ---
        for (int w = 0; w < WARMUP_ITERATIONS; w++) {
            for (double alpha : alphas) {
                int elements = (int)(CAPACITY * alpha);
                HashTable<Integer, Integer> table = isProbing ?
                        new ProbingHashTable<>(factory, k, 2.0) : new ChainedHashTable<>(factory, k, 10.0);
                for (int j = 0; j < elements; j++) {
                    table.insert(j, j);
                    Integer res = table.search(j);
                    if (res != null) sink += res;
                }
            }
        }
        // --------------------------------

        for (int i = 0; i < alphas.length; i++) {
            double alpha = alphas[i];
            int elementsInTable = (int) (CAPACITY * alpha);
            int totalKeysToGenerate = elementsInTable * 2;
            double totalNanoTime = 0;

            for (int exp = 0; exp < NUM_OF_EXPERIMENTS; exp++) {
                Integer[] keys = utils.genUniqueIntegers(totalKeysToGenerate);
                HashTable<Integer, Integer> table = isProbing ?
                        new ProbingHashTable<>(factory, k, 2.0) : new ChainedHashTable<>(factory, k, 10.0);

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