package com.tomer.datastructures.skiplist;

import com.tomer.datastructures.composite.MyDataStructure;
import com.tomer.datastructures.hashing.ChainedHashTable;
import com.tomer.datastructures.hashing.HashFactory;
import com.tomer.datastructures.hashing.HashingUtils;
import com.tomer.datastructures.hashing.ModularHash;
import com.tomer.datastructures.hashing.MultiplicativeShiftingHash;
import com.tomer.datastructures.hashing.ProbingHashTable;

import java.util.Arrays;
import java.util.List;

public class Tester {
    public static void main(String[] args) {
        System.out.println("Starting Test Suite...\n");

        testTask2_1_DecreaseHeight();
        testTask2_2_Find();
        testTask2_3_GenerateHeight();
        testTask2_5_InsertWidths();
        testTask2_5_DeleteWidths();
        testTask2_5_Rank();
        testTask2_5_Select();

        testTask3_1_ModularHash();
        testTask3_2_MultiplicativeShiftingHash();

        testChainedHashTable_ModularHash();
        testChainedHashTable_MultiplicativeHash();

        testProbingHashTable_ModularHash();
        testProbingHashTable_MultiplicativeHash();

        System.out.println("=========================================");
        System.out.println("   Running MyDataStructure Tests");
        System.out.println("=========================================\n");

        testBasicInsertAndContains();
        testCapacityAndDuplicates();
        testDelete();
        testRankAndSelect();
        testRange();

        System.out.println("\nAll tests completed! (Check console for any FAIL messages)");
    }

    // ==========================================
    // TASK 2.1: decreaseHeight()
    // ==========================================
    public static void testTask2_1_DecreaseHeight() {
        System.out.println("--- Testing Task 2.1: decreaseHeight() ---");

        IndexableSkipList list = new IndexableSkipList(0.5);

        int initialHeight = list.head.height();

        list.increaseHeight();
        list.increaseHeight();

        check(list.head.height() == initialHeight + 2, "increaseHeight works correctly");

        list.decreaseHeight();
        check(list.head.height() == initialHeight + 1, "decreaseHeight successfully reduced height by 1");

        check(list.head.height() == list.tail.height(), "head and tail heights are synchronized after decrease");
    }

    // ==========================================
    // TASK 2.2: find(key)
    // ==========================================
    public static void testTask2_2_Find() {
        System.out.println("\n--- Testing Task 2.2: find(int key) ---");

        IndexableSkipList list = new IndexableSkipList(0.5);

        list.insert(10);
        list.insert(20);
        list.insert(30);
        list.insert(40);
        list.insert(50);

        // Edge Case 1: Finding an existing element
        AbstractSkipList.SkipListNode foundNode = list.find(30);
        check(foundNode != null && foundNode.key() == 30, "Find returns the exact node when key exists");

        // Edge Case 2: Finding a non-existing element (middle)
        AbstractSkipList.SkipListNode prevNode = list.find(25);
        check(prevNode != null && prevNode.key() == 20, "Find returns the correct predecessor for a missing key");

        // Edge Case 3: Finding a non-existing element (smaller than minimum)
        AbstractSkipList.SkipListNode minNode = list.find(5);
        check(minNode != null && minNode.key() == Integer.MIN_VALUE, "Find returns the head node for a key smaller than any in list");

        // Edge Case 4: Finding a non-existing element (larger than maximum)
        AbstractSkipList.SkipListNode maxNode = list.find(100);
        check(maxNode != null && maxNode.key() == 50, "Find returns the maximum element node for a key larger than any in list");
    }

    // ==========================================
    // TASK 2.3: generateHeight() - Statistical Test
    // ==========================================
    public static void testTask2_3_GenerateHeight() {
        System.out.println("\n--- Testing Task 2.3: generateHeight() (Statistical Test) ---");

        double p = 0.5;
        IndexableSkipList list = new IndexableSkipList(p);

        int n = 1_000_000;
        int[] heightCounts = new int[10];

        for (int i = 0; i < n; i++) {
            int h = list.generateHeight();
            if (h < heightCounts.length) {
                heightCounts[h]++;
            }
        }

        boolean passed = true;
        double currentExpectedProb = p;
        double epsilon = 0.01; // standard deviation

        System.out.println("Running 1,000,000 iterations (Tolerance: +/- " + epsilon + "):");

        for (int h = 0; h < 5; h++) {
            double actualProb = (double) heightCounts[h] / n;

            boolean levelPassed = Math.abs(currentExpectedProb - actualProb) <= epsilon;

            System.out.printf("  Level %d: Expected ~%.3f, Actual ~%.3f -> %s\n",
                    h, currentExpectedProb, actualProb, (levelPassed ? "OK" : "DEVIATION"));

            if (!levelPassed) {
                passed = false;
            }

            currentExpectedProb = currentExpectedProb * (1 - p);
        }

        check(passed, "generateHeight distribution perfectly matches geometric process with p=" + p);
    }

    // ==========================================
    // TASK 2.5: Width Maintenance on Insert
    // ==========================================
    public static void testTask2_5_InsertWidths() {
        System.out.println("\n--- Testing Insert Width Maintenance ---");
        IndexableSkipList list = new IndexableSkipList(0.5);

        // Edge Case 1: Empty list width
        check(validateAllLevelsWidth(list), "Empty list widths equal size+1");

        // Insert first element
        list.insert(50);
        check(validateAllLevelsWidth(list), "Widths correct after 1st insertion (50)");
        check(list.size == 1, "Size correctly updated to 1");

        // Insert smaller element (tests updating widths for elements inserted at the beginning)
        list.insert(20);
        check(validateAllLevelsWidth(list), "Widths correct after prepending (20)");

        // Insert larger element (tests updating widths for elements inserted at the end)
        list.insert(80);
        check(validateAllLevelsWidth(list), "Widths correct after appending (80)");

        // Insert middle elements (tests splitting existing edge widths)
        list.insert(30);
        list.insert(40);
        check(validateAllLevelsWidth(list), "Widths correct after middle insertions (30, 40)");
        check(list.size == 5, "Size correctly updated to 5");

        // Attempt to insert duplicate (should be rejected, size and widths unchanged)
        list.insert(30);
        check(validateAllLevelsWidth(list), "Widths unaffected by duplicate insertion");
        check(list.size == 5, "Size unchanged after duplicate insertion");
    }

    // ==========================================
    // TASK 2.5: Width Maintenance on Delete
    // ==========================================
    public static void testTask2_5_DeleteWidths() {
        System.out.println("\n--- Testing Delete Width Maintenance ---");
        IndexableSkipList list = new IndexableSkipList(0.5);

        // Setup: Insert 10, 20, 30, 40, 50
        list.insert(10); list.insert(20); list.insert(30); list.insert(40); list.insert(50);
        check(validateAllLevelsWidth(list), "Setup: Initial widths are correct before deletion");

        // Edge Case 1: Delete Middle Element
        AbstractSkipList.SkipListNode node30 = list.search(30);
        boolean deleted30 = list.delete(node30);
        check(deleted30, "Successfully deleted middle element (30)");
        check(validateAllLevelsWidth(list), "Widths correctly merged and updated after middle deletion");
        check(list.size == 4, "Size correctly decremented to 4");

        // Edge Case 2: Delete First Element
        AbstractSkipList.SkipListNode node10 = list.search(10);
        list.delete(node10);
        check(validateAllLevelsWidth(list), "Widths correct after deleting the first element (10)");

        // Edge Case 3: Delete Last Element
        AbstractSkipList.SkipListNode node50 = list.search(50);
        list.delete(node50);
        check(validateAllLevelsWidth(list), "Widths correct after deleting the last element (50)");

        // Edge Case 4: Delete Non-Existent Element
        boolean deleted100 = list.delete(new AbstractSkipList.SkipListNode(100));
        check(!deleted100, "Deletion of non-existent element rejected");
        check(validateAllLevelsWidth(list), "Widths unaffected by failed deletion");

        // Edge Case 5: Emptying the list completely
        list.delete(list.search(20));
        list.delete(list.search(40));
        check(validateAllLevelsWidth(list), "Widths correct after completely emptying the list");
        check(list.size == 0, "Size is 0 after deleting all elements");
    }

    // ==========================================
    // TASK 2.5: Skip List - Rank(val)
    // ==========================================
    public static void testTask2_5_Rank() {
        System.out.println("\n--- Testing Task 2.5: Rank(val) ---");
        IndexableSkipList list = new IndexableSkipList(0.5);

        // Edge Case 1: Empty list
        check(list.rank(10) == 0, "Rank in an empty list is always 0");

        // Setup: Insert 10, 20, 30, 40, 50
        list.insert(10); list.insert(20); list.insert(30); list.insert(40); list.insert(50);

        // Standard Cases: Existing elements
        check(list.rank(10) == 0, "Rank of minimum element (10) is 0");
        check(list.rank(30) == 2, "Rank of middle element (30) is 2");
        check(list.rank(50) == 4, "Rank of maximum element (50) is 4");

        // Edge Cases: Non-existing elements
        check(list.rank(25) == 2, "Rank of non-existent middle element (25) is correctly 2 (10, 20 are smaller)");
        check(list.rank(5) == 0, "Rank of element smaller than all elements (5) is 0");
        check(list.rank(100) == 5, "Rank of element larger than all elements (100) is 5 (equal to size)");
    }

    // ==========================================
    // TASK 2.5: Skip List - Select(i)
    // ==========================================
    public static void testTask2_5_Select() {
        System.out.println("\n--- Testing Task 2.5: Select(i) ---");
        IndexableSkipList list = new IndexableSkipList(0.5);

        // Setup: Insert 10, 20, 30, 40, 50
        list.insert(10); list.insert(20); list.insert(30); list.insert(40); list.insert(50);

        // Standard Cases: Valid indices
        check(list.select(0) == 10, "Select(0) returns the minimum element (10)");
        check(list.select(2) == 30, "Select(2) returns the middle element (30)");
        check(list.select(4) == 50, "Select(4) returns the maximum element (50)");

        // Dynamic Case 1: Select after deletion
        // We delete 30. The list is now: 10, 20, 40, 50
        list.delete(list.search(30));
        check(list.select(2) == 40, "Select(2) dynamically shifts to 40 after deleting 30");
        check(list.select(3) == 50, "Select(3) is now the maximum element (50)");

        // Dynamic Case 2: Select after insertion
        // We insert 25. The list is now: 10, 20, 25, 40, 50
        list.insert(25);
        check(list.select(2) == 25, "Select(2) dynamically shifts to 25 after insertion");
        check(list.select(3) == 40, "Select(3) shifts back to 40");
    }

    // ==========================================
    // TASK 3.1: Modular Hash (Carter-Wegman)
    // ==========================================
    public static void testTask3_1_ModularHash() {
        System.out.println("\n--- Testing Task 3.1: ModularHash (Carter-Wegman) ---");
        ModularHash factory = new ModularHash();

        // 1. Edge Cases for 'k' (Exception Handling)
        boolean exceptionThrown = false;
        try {
            factory.pickHash(-1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        check(exceptionThrown, "Throws IllegalArgumentException for k < 0");

        exceptionThrown = false;
        try {
            factory.pickHash(31);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        check(exceptionThrown, "Throws IllegalArgumentException for k > 30");

        // 2. Validate bounds of generated parameters
        int k = 10;
        ModularHash.Functor functor = (ModularHash.Functor) factory.pickHash(k);

        check(functor.a() >= 1 && functor.a() < Integer.MAX_VALUE, "Parameter 'a' is within valid bounds [1, MAX-1]");
        check(functor.b() >= 0, "Parameter 'b' is within valid bounds [0, MAX]");
        check(functor.p() > Integer.MAX_VALUE, "Parameter 'p' is strictly greater than Integer.MAX_VALUE");
        check(functor.m() == (1 << k), "Parameter 'm' correctly calculated as 2^k");

        // 3. Mathematical verification of the hash function
        int testKey = 42;
        long a = functor.a();
        long b = functor.b();
        long p = functor.p();
        int m = functor.m();

        long expectedInner = HashingUtils.mod((a * testKey + b), p);
        int expectedHash = (int) HashingUtils.mod(expectedInner, m);

        check(functor.hash(testKey) == expectedHash, "Hash function accurately calculates ((a*x + b) mod p) mod m");

        // 4. Boundary and Validity verification
        int negativeKey = -500;
        int largeKey = 9999999;

        int hash1 = functor.hash(testKey);
        int hash2 = functor.hash(negativeKey);
        int hash3 = functor.hash(largeKey);

        check(hash1 >= 0 && hash1 < m, "Hash result for normal positive key is a valid array index [0, m-1]");
        check(hash2 >= 0 && hash2 < m, "Hash result for negative key is a valid array index [0, m-1]");
        check(hash3 >= 0 && hash3 < m, "Hash result for very large key is a valid array index [0, m-1]");
    }

    // ==========================================
    // TASK 3.2: Multiplicative Shifting Hash (DHKP)
    // ==========================================
    public static void testTask3_2_MultiplicativeShiftingHash() {
        System.out.println("\n--- Testing Task 3.2: MultiplicativeShiftingHash (DHKP) ---");
        MultiplicativeShiftingHash factory = new MultiplicativeShiftingHash();

        // 1. Edge Cases for 'k' (Exception Handling)
        boolean exceptionThrown = false;
        try {
            factory.pickHash(-1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        check(exceptionThrown, "Throws IllegalArgumentException for k < 0");

        exceptionThrown = false;
        try {
            factory.pickHash(31);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        check(exceptionThrown, "Throws IllegalArgumentException for k > 30");

        // 2. Validate bounds of generated parameters
        int k = 10;
        int m = 1 << k; // 2^10 = 1024

        MultiplicativeShiftingHash.Functor functor = (MultiplicativeShiftingHash.Functor) factory.pickHash(k);

        check(functor.a() > 1, "Parameter 'a' is strictly greater than 1");
        check(functor.k() == k, "Parameter 'k' is stored correctly");

        // 3. Mathematical verification of the bitwise shift logic
        long testKey = 42L;
        long a = functor.a();

        int expectedHash = (int) ((a * testKey) >>> (64 - k));

        check(functor.hash(testKey) == expectedHash, "Hash function accurately calculates (a * x) >>> (w - k)");

        // 4. Boundary and Validity verification (Crucial for Bitwise operations)
        long negativeKey = -999999999L;
        long veryLargeKey = Long.MAX_VALUE - 5;
        long zeroKey = 0L;

        int hash1 = functor.hash(testKey);
        int hash2 = functor.hash(negativeKey);
        int hash3 = functor.hash(veryLargeKey);
        int hash4 = functor.hash(zeroKey);

        check(hash1 >= 0 && hash1 < m, "Hash result for normal positive key is a valid index [0, m-1]");
        check(hash2 >= 0 && hash2 < m, "Hash result for negative key is a positive valid index [0, m-1] (Thanks to >>>)");
        check(hash3 >= 0 && hash3 < m, "Hash result for very large key is a valid index [0, m-1]");
        check(hash4 == 0, "Hash result for key 0 is always 0");
    }

    // ==========================================
    // Chained Hash Table with ModularHash (Integer)
    // ==========================================
    public static void testChainedHashTable_ModularHash() {
        System.out.println("\n--- Testing ChainedHashTable with ModularHash (Integer) ---");

        HashFactory<Integer> factory = new ModularHash();
        ChainedHashTable<Integer, String> table = new ChainedHashTable<>(factory, 2, 1.5);

        check(table.capacity() == 4, "Initial capacity is correctly set to 4 (2^2)");

        // 1. Basic Insert & Search
        table.insert(10, "A");
        table.insert(20, "B");
        table.insert(30, "C");
        check("A".equals(table.search(10)), "Search successfully finds existing element");
        check(table.search(99) == null, "Search returns null for non-existing element");

        /*
        // 2. Update existing key
        table.insert(20, "B_Updated");
        check("B_Updated".equals(table.search(20)), "Insert correctly updates value for an existing key");
         */

        // 3. Delete
        check(table.delete(30), "Delete successfully removes existing element");
        check(!table.delete(99), "Delete returns false for non-existing element");
        check(table.search(30) == null, "Element is no longer found after deletion");

        // 4. Rehashing Test
        table.insert(40, "D");
        table.insert(50, "E");
        table.insert(60, "F");


        table.insert(70, "G");

        check(table.capacity() == 8, "Table successfully doubled its capacity to 8 after exceeding load factor");

        check("A".equals(table.search(10)) && "B".equals(table.search(20)) &&
                "G".equals(table.search(70)), "All elements successfully survived the rehash process");
    }

    // ==========================================
    // Chained Hash Table with MultiplicativeShiftingHash (Long)
    // ==========================================
    public static void testChainedHashTable_MultiplicativeHash() {
        System.out.println("\n--- Testing ChainedHashTable with MultiplicativeShiftingHash (Long) ---");

        HashFactory<Long> factory = new MultiplicativeShiftingHash();
        ChainedHashTable<Long, String> table = new ChainedHashTable<>(factory, 2, 2.0);

        check(table.capacity() == 4, "Initial capacity is correctly set to 4 (2^2)");

        table.insert(100L, "OneHundred");
        table.insert(200L, "TwoHundred");
        table.insert(300L, "ThreeHundred");
        table.insert(400L, "FourHundred");

        check("TwoHundred".equals(table.search(200L)), "Search successfully finds Long key");

        table.delete(100L);
        check(table.search(100L) == null, "Successfully deleted Long key");

        for (long i = 1; i <= 10; i++) {
            table.insert(i * 1000L, "Value_" + i);
        }

        check(table.capacity() >= 8, "Table successfully expanded its capacity during bulk insert");
        check("Value_5".equals(table.search(5000L)), "Search works accurately after rehashing");
    }

    // ==========================================
    // Probing HashTable with ModularHash (Integer)
    // ==========================================
    public static void testProbingHashTable_ModularHash() {
        System.out.println("\n--- Testing ProbingHashTable with ModularHash (Integer) ---");

        HashFactory<Integer> factory = new ModularHash();
        ProbingHashTable<Integer, String> table = new ProbingHashTable<>(factory, 2, 0.75);

        check(table.capacity() == 4, "Initial capacity is correctly set to 4 (2^2)");

        table.insert(10, "Value_10");
        table.insert(20, "Value_20");
        table.insert(30, "Value_30");

        check("Value_20".equals(table.search(20)), "Search successfully finds existing Integer key");
        check(table.search(99) == null, "Search returns null for non-existing key");


        check(table.delete(20), "Delete returns true for existing key");
        check(table.search(20) == null, "Key 20 is no longer found after deletion");

        check("Value_30".equals(table.search(30)), "Search correctly skips over DELETED cells to find subsequent keys");

        table.insert(40, "Value_40");
        check("Value_40".equals(table.search(40)), "Insert successfully functions when table contains DELETED cells");

        table.insert(50, "Value_50");

        check(table.capacity() == 8, "Table successfully doubled capacity to 8 after exceeding load factor");

        check("Value_10".equals(table.search(10)) &&
                "Value_30".equals(table.search(30)) &&
                "Value_40".equals(table.search(40)) &&
                "Value_50".equals(table.search(50)), "All live elements successfully migrated and are searchable after rehash");

        check(table.search(20) == null, "Deleted elements were correctly pruned during rehash");
    }

    // ==========================================
    // Probing HashTable with MultiplicativeShiftingHash (Long)
    // ==========================================
    public static void testProbingHashTable_MultiplicativeHash() {
        System.out.println("\n--- Testing ProbingHashTable with MultiplicativeShiftingHash (Long) ---");

        HashFactory<Long> factory = new MultiplicativeShiftingHash();
        ProbingHashTable<Long, String> table = new ProbingHashTable<>(factory, 3, 0.5);

        check(table.capacity() == 8, "Initial capacity is correctly set to 8 (2^3)");

        table.insert(100L, "Cent");
        table.insert(200L, "Deux Cents");
        table.insert(300L, "Trois Cents");
        table.insert(400L, "Quatre Cents");

        check("Trois Cents".equals(table.search(300L)), "Search successfully finds Long key using Bitwise Shift");

        table.delete(200L);
        table.delete(300L);

        check(table.search(200L) == null && table.search(300L) == null, "Multiple Long keys successfully deleted");
        check("Quatre Cents".equals(table.search(400L)), "Search successfully navigates through a sequence of multiple DELETED cells");

        table.insert(500L, "Cinq Cents");
        table.insert(600L, "Six Cents");
        table.insert(700L, "Sept Cents");

        check(table.capacity() == 16, "Table successfully expanded to 16 after bulk Long insertions");

        check("Cent".equals(table.search(100L)) &&
                "Quatre Cents".equals(table.search(400L)) &&
                "Sept Cents".equals(table.search(700L)), "All Long keys accurately re-hashed into the expanded table");
    }

    // ==========================================
    // TASK 4.1: MyDataStructure Tests
    // ==========================================

    public static void testBasicInsertAndContains() {
        System.out.println("--- Test: Basic Insert & Contains ---");
        MyDataStructure ds = new MyDataStructure(10);

        check(!ds.contains(5), "Contains on empty DS should be false");
        check(ds.insert(5), "Insert 5 should return true");
        check(ds.contains(5), "Contains 5 should now be true");
        check(ds.insert(10), "Insert 10 should return true");
        check(ds.contains(10), "Contains 10 should be true");
        System.out.println();
    }

    public static void testCapacityAndDuplicates() {
        System.out.println("--- Test: Capacity & Duplicates ---");
        MyDataStructure ds = new MyDataStructure(3); // Capacity is exactly 3

        check(ds.insert(1), "Insert 1");
        check(ds.insert(2), "Insert 2");
        check(!ds.insert(2), "Insert duplicate 2 should return false");
        check(ds.insert(3), "Insert 3");

        // DS is now full (size = 3)
        check(!ds.insert(4), "Insert 4 beyond capacity should return false");
        check(!ds.contains(4), "Contains 4 should be false");
        System.out.println();
    }

    public static void testDelete() {
        System.out.println("--- Test: Delete ---");
        MyDataStructure ds = new MyDataStructure(10);
        ds.insert(10);
        ds.insert(20);
        ds.insert(30);

        check(!ds.delete(15), "Delete non-existing (15) should return false");
        check(ds.delete(20), "Delete existing (20) should return true");
        check(!ds.contains(20), "Contains 20 after deletion should be false");
        check(ds.contains(10), "Contains 10 should still be true");
        check(ds.contains(30), "Contains 30 should still be true");

        // Ensure size decreased allowing new inserts
        check(ds.insert(40), "Insert 40 should be successful after delete");
        System.out.println();
    }

    public static void testRankAndSelect() {
        System.out.println("--- Test: Rank & Select ---");
        MyDataStructure ds = new MyDataStructure(10);
        ds.insert(50);
        ds.insert(20);
        ds.insert(10);
        ds.insert(30);
        ds.insert(40);
        // Elements in order: 10, 20, 30, 40, 50

        check(ds.rank(10) == 0, "Rank of 10 (smallest) should be 0");
        check(ds.rank(35) == 3, "Rank of 35 (not in DS) should be 3 (10,20,30 are smaller)");
        check(ds.rank(60) == 5, "Rank of 60 (larger than all) should be 5");

        check(ds.select(0) == 10, "Select index 0 should be 10");
        check(ds.select(2) == 30, "Select index 2 should be 30");
        check(ds.select(4) == 50, "Select index 4 should be 50");
        System.out.println();
    }

    public static void testRange() {
        System.out.println("--- Test: Range ---");
        MyDataStructure ds = new MyDataStructure(10);
        ds.insert(15);
        ds.insert(5);
        ds.insert(25);
        ds.insert(35);
        ds.insert(45);
        // Elements in order: 5, 15, 25, 35, 45

        List<Integer> expected1 = Arrays.asList(15, 25, 35);
        check(expected1.equals(ds.range(15, 40)), "Range(15, 40) should return [15, 25, 35]");

        List<Integer> expected2 = Arrays.asList(25);
        check(expected2.equals(ds.range(25, 25)), "Range(25, 25) should return [25]");

        check(ds.range(10, 30) == null, "Range(10, 30) where low=10 is NOT in DS should return null");

        List<Integer> expectedFull = Arrays.asList(5, 15, 25, 35, 45);
        check(expectedFull.equals(ds.range(5, 50)), "Range(5, 50) should return all elements");
        System.out.println();
    }

    // ==========================================
    // Helper function: The "Golden Rule" Width Validator
    // ==========================================
    private static boolean validateAllLevelsWidth(AbstractSkipList list) {
        int expectedSum = list.size + 1;
        int maxHeight = list.head.height();

        for (int i = 0; i <= maxHeight; i++) {
            int currentSum = 0;
            AbstractSkipList.SkipListNode curr = list.head;

            while (curr != list.tail) {
                currentSum += curr.getNextWidth(i);
                curr = curr.getNext(i);
            }

            if (currentSum != expectedSum) {
                System.out.println("      [ERROR] Width invariant failed at level " + i +
                        "! Expected: " + expectedSum + ", Actual: " + currentSum);
                return false;
            }
        }
        return true;
    }

    // ==========================================
    // Helper function for checking conditions
    // ==========================================
    private static void check(boolean condition, String testName) {
        if (condition) {
            System.out.println("[PASS] " + testName);
        } else {
            System.out.println("[FAIL] " + testName);
        }
    }
}
