package com.tomer.datastructures.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Element")
class ElementTest {

    @Test
    @DisplayName("equals only itself, even with the same key and data")
    void identityEquality() {
        Element<Integer, String> element = new Element<>(100, "x");
        Element<Integer, String> sameKeyAndData = new Element<>(100, "x");

        assertEquals(element, element, "An element equals itself");
        assertNotEquals(element, sameKeyAndData,
                "A separately made element with the same key and data is a different element");
    }
}
