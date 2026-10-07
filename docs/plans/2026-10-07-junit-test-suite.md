# JUnit test suite

**Status:** active · **Branch:** `refactor/junit-tests` off `main` at `24aed09` · **Started:** 2026-10-07

## Context

The only tests are `Tester.java`, a hand-written `main` whose `check()` prints `[PASS]`/`[FAIL]`.
The test audit (2026-10-07) measured 116/116 checks passing, but also found:

- a failing check still exits 0;
- one exception ends the run (36 of 116 checks ran);
- 10.2 s of the 10.3 s run is benchmarks;
- 3 of 5 deliberate bugs went uncaught (M1 inverted coin, M3 delete skipping the skip list, M4
  rehash copying DELETED markers), and one more (M2) was caught in only 1 of 12 runs;
- two real bugs:
  - `MyDataStructure.range(low, Integer.MAX_VALUE)` returns the tail sentinel 2147483647;
  - `MultiplicativeShiftingHash.pickHash(0)` hashes outside [0, 1).

Outcome: a Maven project in packages, every check moved to JUnit, JaCoCo coverage with a floor,
the audit findings fixed, the gaps covered, both bugs fixed test-first. This is the safety net for
the later production refactor.

Not in scope: production refactoring beyond the package move and the two bug fixes; CI; compiler
warnings; README content beyond a build-and-test section.

## Decisions

From the interview (2026-10-07):

1. **Branch:** `refactor/junit-tests` off `main`; merge when done. Main stays the untouched baseline.
2. **Packages:** move into `com.tomer.datastructures.*` packages in step 1, like
   dynamic-sets-implementation, so the tests are written in their final packages once.
3. **Bugs:** fix both in this plan, each in its own step: a failing test first, then the smallest fix.
4. **Coverage:** a JaCoCo report on every `verify`, plus a floor (line coverage, rounded down to the
   nearest 5%) added after the gap tests.

Defaults chosen without asking (approved with the plan):

- **Build:** Maven via the script-only wrapper 3.3.4. `mvnw`, `mvnw.cmd` and `.mvn/` are copied
  from dynamic-sets-implementation and pointed at Maven 3.9.16 (latest 3.x; 4.0 is still an RC).
  `mvnw` is committed executable (100755), as in the sibling.
- **Versions** (latest on Maven Central today; re-checked in step 1):
  - JUnit 6.1.3 (the Jupiter API, same annotations as JUnit 5);
  - surefire 3.6.0, compiler plugin 3.16.0, JaCoCo 0.8.15, exec-maven-plugin 3.6.4.
- **Java:** `release 17`, as in the sibling (JUnit 6 needs 17+), built with your local JDK 21.
- **Coordinates and packages:** `com.tomer:probabilistic-data-structures:1.0.0`. Packages:
  - `core`: Element, Pair
  - `skiplist`: AbstractSkipList, IndexableSkipList, SkipListUtils
  - `hashing`: the 3 interfaces, the 2 tables, the 2 hash factories, HashingUtils
  - `composite`: MyDataStructure
  - `experiments`: HashingExperimentUtils and the new HashingExperiments main
- **Test style:** the sibling's.
  - One test class per production class, in the same package.
  - `@Nested` groups and `@DisplayName` on every test.
  - A `SkipListInvariants` helper (from `Tester.validateAllLevelsWidth`), like the sibling's
    `TreeInvariants`.
- **Migrate first, improve after:** step 3 is a one-to-one move.
  - Same scenarios, same expected values, weak checks included.
  - Each check becomes one assertion carrying its original message.
  - So every later fix shows up as its own diff.
- **Randomness:**
  - `generateHeight` must use `Math.random()` (spec), so its test stays statistical with a 5σ
    tolerance.
  - Every other test either fixes the hash (a test-only `HashFactory`) or asserts something true
    for every random outcome.
  - A seeded `Random` is used only to shuffle insertion orders.
- **Experiments:** their own `main`, run with `./mvnw -q exec:java`; not part of the tests.
- **The commented-out "update existing key" check** (`Tester.java:395-399`) is not migrated: the
  spec rules out duplicate keys.

## Approach

Each step is one commit that leaves the project building with its tests passing. The checks named
M1–M5 re-apply the audit's deliberate bugs in a scratch copy (never the working tree), so each new
test is seen failing.

### 1. Maven build, standard layout, packages

`./mvnw verify` builds the project; nothing about the checks changes.
Files: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.gitignore`, every
`src/*.java`

- [x] `git mv` each class to `src/main/java/com/tomer/datastructures/<package>/`, add its
      `package` line and the imports it needs (all cross-package members used are already public)
- [x] `git mv` Tester to `src/test/java/com/tomer/datastructures/skiplist/Tester.java`, for now (it
      reads the skip list's protected `head`/`tail`/`size`)
- [x] pom modelled on the sibling's: JUnit, surefire, JaCoCo `prepare-agent` + `report`, and exec
      plugin with `mainClass` left for step 2
- [x] `.gitignore`: add the sibling's Maven block (`target/`, wrapper jar)

**Verify (me):**
- `./mvnw -q verify` -> BUILD SUCCESS.
- `java -cp "target/classes;target/test-classes" com.tomer.datastructures.skiplist.Tester` ->
  116 PASS, 0 FAIL, as at baseline.
- `git log --follow` on a moved file shows its history.

**Verify (you):** open the project in IntelliJ and load it as a Maven project. If the old `.iml`
lingers, delete it. Then run Tester from the gutter: 116 PASS.
**Commit:** `Restructure as a Maven project in com.tomer.datastructures packages`

### 2. Run the experiments from their own main

The test run drops from about 10 s to under 1 s. Files: `experiments/HashingExperiments.java`
(new), `Tester.java`, `pom.xml`

- [x] Move `runHashingExperiments` and `printResults` out of Tester into `HashingExperiments.main`
- [x] Set the exec plugin's `mainClass` to it

**Verify (me):**
- Tester -> 116 PASS in under 1 s.
- `./mvnw -q exec:java` -> the four experiment tables (3.5–3.8).

**Verify (you):** nothing beyond the output above.
**Commit:** `Run the hashing experiments from their own main class`

### 3. Move all 116 checks to JUnit

A failing check now fails the build, and each test runs on its own. That fixes findings 1–2 and
L2–L3. Files: 6 test classes and `SkipListInvariants` under `src/test/java/com/tomer/datastructures/`;
Tester.java deleted

- [x] `skiplist/IndexableSkipListTest` (41 checks: Tasks 2.1, 2.2, 2.3, 2.5) and
      `skiplist/SkipListInvariants`
- [x] `hashing/ModularHashTest` (10), `MultiplicativeShiftingHashTest` (9),
      `ChainedHashTableTest` (13), `ProbingHashTableTest` (16)
- [x] `composite/MyDataStructureTest` (27)
- [x] Delete Tester.java

**Verify (me):**
- `./mvnw verify` -> BUILD SUCCESS, 0 failures. Record the test count and JaCoCo's per-package line
  coverage in the Session log.
- A script counts each of Tester's 116 check messages (from `git show HEAD:…Tester.java`) in
  `src/test/java` -> every message appears at least as often as in Tester.
- M5 in scratch -> `verify` exits non-zero, and surefire still reports every test class run.

**Verify (you):** IntelliJ's test runner shows the tests green.
**Commit:** `Move Tester's 116 checks to JUnit 5 tests`

### 4. Fix the audit findings in the existing tests

Findings 3, 4, 6 and 7. Files: `hashing/FixedHashFactory.java` (new, test-only), the four hashing
tests, `IndexableSkipListTest`

- [x] `FixedHashFactory`: a `HashFactory` whose hash the test chooses (e.g. every key to slot 0).
      The DELETED-skip checks use it, so the collision happens on every run.
- [x] Probing: delete, then insert until a resize. Assert capacity before and after the triggering
      insert, the deleted key absent, the others found. Reword the 3 untrue messages
      (`:478`, `:485`, `:516`).
- [x] `generateHeight` at p = 0.5 and p = 0.25. Tolerance 5σ worked out from n and p. Fix the
      "standard deviation" comment and the "perfectly matches" message.
- [x] Hash expectations computed with `BigInteger` at `Integer`/`Long` MIN and MAX, not with the
      code's own formula.

**Verify (me):**
- `./mvnw verify` green.
- In scratch, each of these must fail `verify` in 5 of 5 runs, and the source is diffed clean
  afterwards:
  - M1 (coin inverted);
  - M2 (delete empties the slot; was 1 in 12);
  - M4 (rehash copies DELETED; was 0 of 200);
  - `(long) a * key` changed to `a * key`.

**Verify (you):** nothing beyond the tests.
**Commit:** `Make the hashing and height tests deterministic and accurate`

### 5. Cover the skip list and MyDataStructure gaps

Files: `IndexableSkipListTest`, `MyDataStructureTest`

- [x] Write the tests for the plan's MyDataStructure scenarios:
  - insert 10..50, delete 30 -> `rank(35)` = 2, `select(2)` = 40, `range(10, 50)` = [10, 20, 40, 50];
  - after deleting 30, `range(30, 50)` is null;
  - delete then re-insert the same value;
  - N = 3 full, delete one -> one insert succeeds, the next fails;
  - N = 1;
  - `range(15, 20)` = [15] and `range(25, 20)` = [];
  - negatives and 0.
- [x] Write the skip-list tests:
  - 200 keys in a seeded shuffled order, delete every third -> invariants hold, and `rank`/`select`
    match a sorted reference for every key;
  - height back to 0 once emptied;
  - `minimum`/`maximum`, both on an empty list (throws) and on values;
  - `successor`/`predecessor`;
  - `calculateExpectedHeight(0.5)` = 1.0 and `calculateExpectedHeight(0.25)` = 3.0.
- [x] If any of them fails on the current code: stop and report (a new bug), don't change the code.
      (None failed.)

**Verify (me):**
- `./mvnw verify` green.
- M3 (was 0 of 200) and M5 each fail `verify` in 5 of 5 scratch runs.

**Verify (you):** nothing beyond the tests.
**Commit:** `Cover the skip list and MyDataStructure gaps`

### 6. Cover the hashing gaps and add the coverage floor

Files: the four hashing tests, `HashingUtilsTest` (new), `pom.xml`

- [ ] Both tables: capacity doubles on exactly the insert that makes the load reach the maximum
      (the spec's wording), not one insert before or after.
- [ ] Probing:
  - a key hashed to the last slot wraps around to slot 0;
  - re-inserting reuses a DELETED slot;
  - on an empty table, `search` returns null and `delete` returns false.
- [ ] Chained: delete one of three keys in a bucket.
- [ ] `pickHash(30)` stays in range. `ModularHash.pickHash(0)` hashes to 0.
- [ ] `genPrime`: 200 draws, all within bounds and passing `BigInteger.isProbablePrime(50)`.
- [ ] `mod` with negatives, for both the `int` and `long` versions.
- [ ] Add the JaCoCo `check` goal: BUNDLE line coverage minimum = measured value rounded down to the
      nearest 5%.

**Verify (me):**
- `./mvnw verify` green; record the coverage figure.
- With the floor temporarily at 1.00, `verify` fails with JaCoCo's coverage message; then restore
  it.

**Verify (you):** open `target/site/jacoco/index.html`.
**Commit:** `Cover the hashing gaps and enforce a coverage floor`

### 7. Fix: range() returns the tail sentinel

Files: `MyDataStructureTest`, `composite/MyDataStructure.java`

- [ ] Write the failing test: elements 5, 15, 25, 35, 45; `range(5, Integer.MAX_VALUE)` ->
      [5, 15, 25, 35, 45]
- [ ] Run it and see it fail for the right reason: the extra 2147483647
- [ ] Fix: stop the walk at the tail, the only level-0 node with no next

**Verify (me):** `./mvnw verify` green, including the new test.
**Verify (you):** nothing beyond the tests.
**Commit:** `Keep the tail sentinel out of MyDataStructure.range results`

### 8. Fix: multiplicative hash with k = 0 hashes out of range

Files: `MultiplicativeShiftingHashTest`, `hashing/MultiplicativeShiftingHash.java`

- [ ] Write the failing test: `pickHash(0).hash(x)` == 0 for keys 0, 42, -1, Long.MIN_VALUE and
      Long.MAX_VALUE. Plus a table built with k = 0 that inserts and finds keys.
- [ ] Run it and see it fail for the right reason: Java treats `>>> 64` as `>>> 0`
- [ ] Fix: k = 0 returns 0

**Verify (me):** `./mvnw verify` green, including the new tests.
**Verify (you):** nothing beyond the tests.
**Commit:** `Hash into [0, 1) when the multiplicative hash has k = 0`

### 9. Docs and memory sync

- [ ] README (now a one-line title in UTF-16): convert it to UTF-8 and add a "Build and test"
      section covering:
  - `./mvnw verify`, with the test count and coverage figure re-measured;
  - the report's path;
  - `./mvnw -q exec:java` for the experiments.
- [ ] Memory: the project keeps none yet. Add one only for something the repo doesn't record.

**Verify (me):** each changed claim checked against the code or a command
**Verify (you):** read the diff
**Commit:** `Document building, testing and coverage in the README`

## Files

- **To change:**
  - every `src/*.java` moves to `src/main/java/com/tomer/datastructures/<package>/` (step 1).
  - `MyDataStructure.range` and `MultiplicativeShiftingHash.Functor.hash` (steps 7–8).
  - `.gitignore` and `README.md`.
- **New:**
  - `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`;
  - `experiments/HashingExperiments.java`;
  - the test classes under `src/test/java/com/tomer/datastructures/{skiplist,hashing,composite}/`,
    including `SkipListInvariants` and `FixedHashFactory`.
- **To reuse:**
  - From `dynamic-sets-implementation`: `pom.xml` (structure and JaCoCo block), `mvnw`, `mvnw.cmd`,
    `.mvn/` and the `.gitignore` Maven block, plus its test style (`TreeInvariants` as the model
    for `SkipListInvariants`).
  - `Tester.validateAllLevelsWidth` (`Tester.java:648`): becomes `SkipListInvariants`.
  - Every `check()` message in `Tester.java`: carried over as an assertion message.
  - The `HashFactory` constructor parameter on both tables: the injection point for `FixedHashFactory`.

## Verification

- `./mvnw clean verify` -> BUILD SUCCESS, every test passing, the JaCoCo check passing, and the
  report at `target/site/jacoco/index.html`.
- `./mvnw -q exec:java` -> the four experiment tables.
- In a scratch copy, each of M1–M5 and both fixed bugs fail `verify` when re-introduced.
- In IntelliJ, the whole suite runs green from the test runner.

## How to work this plan

- **Resume:** be on the branch above, read this whole file, and take the first step not `[x]`.
  `git log --oneline` on this file shows one commit per finished step; check the ticks against it.
- **One step per request.** For "do 3 and 4", commit each once its checks pass unless it needs
  the user's eyes or ears; the last one waits.
- **Mark the step *(in progress)*** before starting. Wrong branch, or changes that belong to no
  step: stop and ask.
- **Tests first** for logic: see the new test fail for the right reason, then implement.
- **Stop uncommitted:** run *Verify (me)*, mark it *(built, awaiting your ok)*, add a Session log
  line naming any choice this plan didn't settle, and report: what changed, the check and its
  result, what the user should check, what you decided on your own, and "ok to commit it, or tell
  me what to change".
- **The user's ok commits it** ("ok", "approved", or moving on): tick `[x]` with the date, stage
  the step's files and this file by path, and use the step's *Commit* line. Never push.
- **Feedback** on a waiting step: change it, check again, keep it waiting.
- **Plan wrong?** Do the right thing and note it under Deviations; ask first if it changes a
  decision or the scope.
- **Last step committed:** set **Status:** to `done` and add a closing note under the title.

## Progress

- [x] **Plan approved** (2026-10-07)
- [x] **1. Maven build, standard layout, packages** (2026-10-07)
- [x] **2. Run the experiments from their own main** (2026-10-07)
- [x] **3. Move all 116 checks to JUnit** (2026-10-07)
- [x] **4. Fix the audit findings in the existing tests** (2026-10-08)
- [x] **5. Cover the skip list and MyDataStructure gaps** (2026-10-08)
- [ ] **6. Cover the hashing gaps and add the coverage floor**
- [ ] **7. Fix: range() returns the tail sentinel**
- [ ] **8. Fix: multiplicative hash with k = 0 hashes out of range**
- [ ] **9. Docs and memory sync**

### Deviations

- **1:** The plan listed no `.gitattributes`; I added the sibling's.
  - Why: it pins `mvnw` to LF. This machine has `core.autocrlf=true`, and a CRLF `mvnw` fails
    under `/bin/sh`.
  - Left out its `*.md text` line: `README.md` is UTF-16, and forcing it to text would insert CR
    bytes on checkout and corrupt it. Step 9 restores the line once the README is UTF-8.
- **4:** The resize-after-delete test hashes each key to key mod m, not every key to slot 0.
  - Why: with every key in one chain, each insert after a delete reuses the first DELETED cell, so
    none is left when the resize comes and M4 goes unseen. Key mod m puts each key in a slot the
    test picks.
- **4:** `ChainedHashTableTest` is unchanged, although the step's Files line names the four
  hashing tests. None of findings 3, 4, 6 and 7 is about the chained table; its gaps are in step 6.
  Its message "doubled … after exceeding load factor" is loose: the table doubles when the load
  reaches 1.5. Step 6's boundary test covers that.
- **5:** `calculateExpectedHeight` is tested in a new `skiplist/SkipListUtilsTest`, not in
  `IndexableSkipListTest`: the test style is one test class per production class, and the method
  is on `SkipListUtils`.

### Session log

- **2026-10-07:** Plan approved. Baseline: 116 of 116 Tester checks passing (exit 0), HEAD `24aed09`.
- **2026-10-07:** Step 1 built.
  - `./mvnw verify`: BUILD SUCCESS on Maven 3.9.16 / JDK 21, JUnit 6.1.3 resolved, 0 JUnit tests
    yet. Surefire does not pick up Tester.
  - Tester from `target/`: 116 PASS, 0 FAIL, exit 0.
  - All 16 files are staged as renames (74–98% similarity). `mvnw` is staged 100755 with LF.
  - Choices the plan didn't settle:
    - No `-Xlint:all` in the compiler config. The sibling's comment says its sources compile
      warning-free; these have unchecked generic-array warnings, which are out of scope.
    - The exec plugin is declared with no configuration until step 2.
- **2026-10-07:** Step 2 built.
  - Behaviour pinned first: the experiments' output from step 1's Tester run, numbers masked
    (34 lines).
  - `./mvnw -q exec:java` exit 0, output identical to the pin.
  - `./mvnw clean verify` exit 0.
  - Tester: 116 PASS, 0 FAIL, exit 0, 281 ms wall (was 11,084 ms).
  - Choice the plan didn't settle: the experiments' text is kept byte-for-byte, including the
    leading blank line, so this stays a pure move.
- **2026-10-07:** Step 3 built.
  - `./mvnw clean verify`: BUILD SUCCESS, 66 tests in 6 classes, 0 failures.
  - The message script read Tester's 116 `check()` calls (112 distinct messages); all 112 appear
    in `src/test/java` at least as often.
  - JaCoCo line coverage: 68.9% total (353/512).

    | Package | Line coverage |
    |---|---|
    | composite | 100% (34/34) |
    | hashing | 84.0% (163/194) |
    | skiplist | 79.3% (134/169) |
    | core | 75.9% (22/29) |
    | experiments | 0% (0/86) |

  - M5 in scratch: `verify` exit 1, 7 failures + 4 errors out of 66. All 6 classes ran; the NPEs
    fail only their own tests.
  - Choices the plan didn't settle:
    - Each test replays the original scenario up to its check, so it builds the exact state that
      check saw.
    - `&&` checks over several keys are split into one assertion per key, with `(key N)` added to
      the message.
    - Null-then-key checks become `assertNotNull` + `assertEquals`, both carrying the message.
  - Open for step 6: the floor's figure includes `experiments` (benchmark code, 0% by design).
  - IntelliJ first failed with "package org.junit.jupiter.api does not exist".
    - Cause: the pre-Maven `probabilistic-data-structures.iml` survived the Maven import and still
      marked all of `src/` as production source; `.idea/modules.xml` still pointed at it.
    - Fix: with the project closed, both git-ignored files were deleted, matching
      dynamic-sets-implementation.
    - Then all tests passed in IntelliJ's runner (you checked).
- **2026-10-07:** Step 4 built.
  - Resize points measured first (jshell on the real classes): the probing Integer scenario
    doubles at insert 30, the Long one at insert 400, the chained Integer one at insert 70.
  - `./mvnw clean verify`: BUILD SUCCESS, 74 tests (was 66), 0 failures. Line coverage 69.1%
    (354/512); hashing 84.5% (164/194), the other packages unchanged.
  - Planted bugs in scratch, 5 runs each; every run exited 1:

    | Bug | Caught | By |
    |---|---|---|
    | M1 coin inverted | 5 of 5 | `generateHeight` at p = 0.25, height 0 |
    | M2 delete empties the slot | 5 of 5 | `skipsADeletedCell`, `skipsARunOfDeletedCells` |
    | M4 rehash copies DELETED | 5 of 5 | `dropsDeletedCells` (NPE hashing the marker's null key) |
    | `(long) a * key` to `a * key` | 5 of 5 | `ModularHashTest.formula` at 42 and MAX every run, at MIN in 3 |

    `git diff src/main` was empty afterwards.
  - Choices the plan didn't settle:
    - The 3 DELETED-skip checks moved from the random-hash groups into a new "every key hashed
      to slot 0" group. The multiple-DELETED check now uses Integer keys, not Long: once the hash
      is fixed, the key type never reaches the probing code.
    - Besides the 3 named messages, the 7 per-key "migrated … after rehash" messages now read
      "Every live key is found after the resize (key N)". Of those keys, only 10 and 100 were in
      the table when it resized.
    - Parameterized with `@ValueSource`, as the sibling does: `generateHeight` over p = 0.5 and
      0.25, the hash formulas over 42, MIN and MAX.
    - The height tolerance is per level, 5·sqrt(q(1−q)/n) with q = p(1−p)^h. For p = 0.5 at
      height 0 that is 0.0025, where it was 0.01.
- **2026-10-08:** Step 5 built.
  - `./mvnw clean verify`: BUILD SUCCESS, 90 tests (was 74), 0 failures. None of the 16 new tests
    failed on the current code, so no new bug.
  - Line coverage 71.3% (365/512); skiplist 85.8% (145/169), was 79.3%. The other packages are
    unchanged.
  - Planted bugs in scratch, 5 runs each; every run exited 1:
    - M3 (delete skips the skip list): 5 of 5. The same 3 new tests failed every run:
      `orderQueries`, `reinsert` and `capacityOne`.
    - M5 (insert doesn't widen the links above the new node): 5 of 5, with 10 to 14 failing tests
      a run. The new `deleteEveryThird` failed in all 5.
    - `git diff src/main` was empty afterwards.
  - Choices the plan didn't settle:
    - The 200 keys are 0, 10, …, 1990, shuffled with `new Random(42)`. "Every third" means
      positions 0, 3, 6, … of the shuffled order, so the deletions are scattered across the list.
    - `rank` is checked for all 200 keys, deleted ones included, against a count of the remaining
      keys below each one.
    - The emptying test first asserts that the 200 inserts raised the height above 0, so it can't
      pass without exercising the shrink. The chance of that setup failing is 2^-200.
