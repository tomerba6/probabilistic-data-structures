package com.tomer.datastructures.skiplist;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Structural assertions for skip lists.
 *
 * <p>An indexable skip list answers {@code rank} and {@code select} from the widths stored on
 * its links, so a width that drifts gives wrong answers long before anything else looks broken.
 * On every level, the widths from head to tail must add up to {@code size + 1}: one step per
 * element, plus the step onto the tail.
 */
final class SkipListInvariants {

    private SkipListInvariants() {
    }

    /**
     * Checks that, on every level, the widths from head to tail add up to {@code size + 1}.
     *
     * @param list    the list to check
     * @param message what the caller expected, reported when the check fails
     */
    static void assertWidthsValid(AbstractSkipList list, String message) {
        int expectedSum = list.size + 1;

        for (int level = 0; level <= list.head.height(); level++) {
            int sum = 0;
            for (AbstractSkipList.SkipListNode curr = list.head; curr != list.tail; curr = curr.getNext(level)) {
                sum += curr.getNextWidth(level);
            }

            if (sum != expectedSum) {
                fail(message + ": the widths on level " + level + " add up to " + sum
                        + ", expected " + expectedSum);
            }
        }
    }
}
