package com.tomer.datastructures.experiments;

/**
 * Runs the hashing experiments (Tasks 3.5 - 3.8) and prints the average time per operation
 * for each load factor. Run with {@code mvnw -q exec:java}.
 */
public class HashingExperiments {
    public static void main(String[] args) {
        System.out.println("\n=======================================================");
        System.out.println("   RUNNING HASHING EXPERIMENTS (Tasks 3.5 - 3.8)");
        System.out.println("   Note: This may take a few seconds due to the large");
        System.out.println("   arrays (65,536 items) and multiple iterations...");
        System.out.println("=======================================================\n");

        String[] probingAlphas = {"1/2   (0.5)   ", "3/4   (0.75)  ", "7/8   (0.875) ", "15/16 (0.9375)"};
        String[] chainingAlphas = {"1/2   (0.5)   ", "3/4   (0.75)  ", "1     (1.0)   ", "3/2   (1.5)   ", "2     (2.0)   "};

        printResults("Task 3.5: Probing Insertions", HashingExperimentUtils.measureInsertionsProbing(), probingAlphas);
        printResults("Task 3.6: Probing Searches", HashingExperimentUtils.measureSearchesProbing(), probingAlphas);

        printResults("Task 3.7: Chaining Insertions", HashingExperimentUtils.measureInsertionsChaining(), chainingAlphas);
        printResults("Task 3.8: Chaining Searches", HashingExperimentUtils.measureSearchesChaining(), chainingAlphas);

        System.out.println("Experiments Completed Successfully!");
    }

    private static void printResults(String taskName, double[] results, String[] alphaLabels) {
        System.out.println("--- " + taskName + " (Average nano-seconds) ---");
        for (int i = 0; i < results.length; i++) {
            System.out.printf("Alpha = %s: %.2f ns\n", alphaLabels[i], results[i]);
        }
        System.out.println();
    }
}
