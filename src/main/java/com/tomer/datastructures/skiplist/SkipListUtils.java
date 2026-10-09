package com.tomer.datastructures.skiplist;

public class SkipListUtils {
    public static double calculateExpectedHeight(double p) {
        return (1.0 / p) - 1.0;
    }
}
