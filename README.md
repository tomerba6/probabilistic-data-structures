# probabilistic-data-structures

[![CI](https://github.com/tomerba6/probabilistic-data-structures/actions/workflows/ci.yml/badge.svg)](https://github.com/tomerba6/probabilistic-data-structures/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/tomerba6/probabilistic-data-structures)](LICENSE)
[![Java](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ftomerba6%2Fprobabilistic-data-structures%2Fmain%2Fpom.xml&query=%2F%2F*%5Blocal-name%28%29%3D%27maven.compiler.release%27%5D&label=Java&suffix=%2B&logo=openjdk&color=orange)](pom.xml)

Randomized data structures in Java: a skip list that answers rank and select, two hash tables whose
hash functions are drawn at random, and a structure that combines them.

## Contents

- [Overview](#overview)
- [Tech stack](#tech-stack)
- [Quick start](#quick-start)
- [Testing](#testing)
- [Project structure](#project-structure)
- [License](#license)

## Overview

- **Indexable skip list.** Node heights are drawn at random, and every link stores how many level-0
  steps it spans, so besides search, insert and delete the list answers rank (how many keys are
  smaller) and select (the key at a position) in expected O(log n).
- **Hash tables.** One resolves collisions by chaining, the other by linear probing. Each draws its
  hash function at random from the family it is given, ((a x + b) mod p) mod m for `Integer` keys or
  the top k bits of a x for `Long` keys, and draws a new one each time it doubles.
- **`MyDataStructure`.** A skip list and a chained hash table holding the same values: membership
  tests in expected constant time; insert, delete, rank and select in expected O(log n); range
  queries in time proportional to their result.
- **Experiments.** Timings of inserts and searches in both hash tables at several load factors.

```java
MyDataStructure ds = new MyDataStructure(100);   // room for up to 100 values
ds.insert(30); ds.insert(10); ds.insert(20); ds.insert(40);
ds.contains(20);    // true
ds.rank(25);        // 2: the values 10 and 20 are below 25
ds.select(0);       // 10, the smallest value
ds.range(10, 30);   // [10, 20, 30]
```

## Tech stack

- Java 17 or newer, with no runtime dependencies
- Maven 3.9, run through the Maven wrapper
- JUnit 6 for the tests, JaCoCo for coverage, javadoc for the doc check
- GitHub Actions for CI, on JDK 17, 21 and 25

## Quick start

Requires JDK 17 or newer. Maven is not required: the wrapper downloads it.

```bash
git clone https://github.com/tomerba6/probabilistic-data-structures.git
cd probabilistic-data-structures
./mvnw verify          # 190 tests, the test and coverage reports, the coverage floor, and the doc check
./mvnw -q exec:java    # the hashing experiments (Tasks 3.5-3.8): average timings per load factor
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.

## Testing

`./mvnw verify` runs the tests, writes the reports and runs the checks below.

`verify` writes an HTML test report, with a row for every test, to `target/reports/surefire.html`.
A failing test stops the build before that report is written; `./mvnw surefire-report:report-only`
then writes it from the results the failed run left in `target/surefire-reports`.

`verify` also writes the JaCoCo coverage report to `target/site/jacoco/index.html`, and fails the build
if line coverage drops below 90%. Coverage can move by a line between runs, because one line, in
rehashing, runs only when a random hash happens to collide two keys.

The `experiments` package is left out of the report and the floor. It is benchmark code: its
timings are printed for reading, not asserted, so no test runs it.

`verify` also checks the doc comments: javadoc fails the build when a public or protected declaration
has no doc comment, or has one it cannot parse, such as an unescaped `<`. The API docs it writes land
in `target/reports/apidocs/index.html`.

CI runs the same `verify` on JDK 17, 21 and 25 for every push and pull request to `main`. Each
JDK's run uploads its HTML test report as an artifact, also when a test fails, and the JDK 17 run
uploads the coverage report when the build passes.

## Project structure

```
src/main/java/com/tomer/datastructures/
├── core/          Element and Pair, shared by the other packages
├── hashing/       the hash table interface, the chaining and probing tables, the two hash function families
├── skiplist/      the skip list with rank and select
├── composite/     MyDataStructure, the skip list paired with a chained hash table
└── experiments/   the timing experiments, run with exec:java
src/test/java/     JUnit tests in the same packages, plus two test helpers
.github/workflows/ CI
docs/plans/        the plan the JUnit test suite was built from
```

## License

Released under the MIT License. See [LICENSE](LICENSE).
