package com.tomer.datastructures.skiplist;

import com.tomer.datastructures.skiplist.AbstractSkipList.SkipListNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SkipListNode")
class SkipListNodeTest {

    // Two levels, 0 and 1, so the node's height is 1 and level 2 is above it.
    private final SkipListNode node = new SkipListNode(7);

    @BeforeEach
    void addTwoLevels() {
        node.addLevel(null, null);
        node.addLevel(null, null);
    }

    static Stream<Arguments> accessorsAtLevelTwo() {
        return Stream.of(
                Arguments.of("getNext", (Consumer<SkipListNode>) n -> n.getNext(2)),
                Arguments.of("getPrev", (Consumer<SkipListNode>) n -> n.getPrev(2)),
                Arguments.of("getNextWidth", (Consumer<SkipListNode>) n -> n.getNextWidth(2)),
                Arguments.of("setNext", (Consumer<SkipListNode>) n -> n.setNext(2, null)),
                Arguments.of("setPrev", (Consumer<SkipListNode>) n -> n.setPrev(2, null)),
                Arguments.of("setNextWidth", (Consumer<SkipListNode>) n -> n.setNextWidth(2, 0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessorsAtLevelTwo")
    @DisplayName("rejects a level above the node's height")
    void levelAboveHeight(String accessor, Consumer<SkipListNode> call) {
        assertThrows(IllegalStateException.class, () -> call.accept(node),
                accessor + "(2) throws IllegalStateException on a node of height 1");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessorsAtLevelTwo")
    @DisplayName("names the level and the height when it rejects a level")
    void messageNamesLevelAndHeight(String accessor, Consumer<SkipListNode> call) {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> call.accept(node));
        assertEquals("Level 2 is above this node's height 1", e.getMessage(),
                accessor + "(2) names level 2 and height 1 in its message");
    }
}
