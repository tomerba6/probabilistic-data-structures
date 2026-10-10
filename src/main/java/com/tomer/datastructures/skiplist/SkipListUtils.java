package com.tomer.datastructures.skiplist;

/**
 * Formulas about skip lists.
 */
public class SkipListUtils {
    /**
     * Returns the expected height of a node whose height is drawn as in
     * {@link IndexableSkipList#generateHeight}: (1 - p) / p, the mean of the geometric distribution
     * p(1 - p)^h.
     *
     * @param p the probability that a node stops growing at each level, in (0, 1]
     * @return the expected height: 0 when p is 1, infinite when p is 0
     */
    public static double calculateExpectedHeight(double p) {
        return (1.0 / p) - 1.0;
    }
}
