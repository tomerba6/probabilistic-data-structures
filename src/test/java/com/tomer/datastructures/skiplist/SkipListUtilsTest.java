package com.tomer.datastructures.skiplist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SkipListUtils")
class SkipListUtilsTest {

    @ParameterizedTest(name = "p = {0} gives {1}")
    @CsvSource({"0.5, 1.0", "0.25, 3.0"})
    @DisplayName("calculateExpectedHeight is (1 - p) / p, the mean of the height distribution")
    void expectedHeight(double p, double expected) {
        assertEquals(expected, SkipListUtils.calculateExpectedHeight(p), 1e-9,
                "calculateExpectedHeight(" + p + ") is " + expected);
    }
}
