package com.tomer.datastructures.experiments;

import com.tomer.datastructures.experiments.HashingExperimentUtils.Alpha;

import java.util.List;
import java.util.Locale;

/**
 * Runs the hashing experiments (Tasks 3.5 - 3.8) and prints the average time per operation
 * for each load factor. Run with {@code mvnw -q exec:java}.
 */
public class HashingExperiments {
    public static void main(String[] args) {
        System.out.println("\n=======================================================");
        System.out.println("   RUNNING HASHING EXPERIMENTS (Tasks 3.5 - 3.8)");
        System.out.println("   Note: This may take a few seconds due to the large");
        System.out.printf(Locale.ROOT, "   arrays (%,d items) and multiple iterations...%n", HashingExperimentUtils.CAPACITY);
        System.out.println("=======================================================\n");

        List<Alpha> probingAlphas = HashingExperimentUtils.PROBING_ALPHAS;
        List<Alpha> chainingAlphas = HashingExperimentUtils.CHAINING_ALPHAS;

        printResults("Task 3.5: Probing Insertions", HashingExperimentUtils.measureInsertionsProbing(), probingAlphas);
        printResults("Task 3.6: Probing Searches", HashingExperimentUtils.measureSearchesProbing(), probingAlphas);

        printResults("Task 3.7: Chaining Insertions", HashingExperimentUtils.measureInsertionsChaining(), chainingAlphas);
        printResults("Task 3.8: Chaining Searches", HashingExperimentUtils.measureSearchesChaining(), chainingAlphas);

        System.out.println("Experiments Completed Successfully!");
    }

    private static void printResults(String taskName, double[] results, List<Alpha> alphas) {
        System.out.println("--- " + taskName + " (Average nano-seconds) ---");
        for (int i = 0; i < results.length; i++) {
            System.out.printf("Alpha = %s: %.2f ns\n", alphas.get(i).label(), results[i]);
        }
        System.out.println();
    }
}
