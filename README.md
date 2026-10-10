# probabilistic-data-structures

[![CI](https://github.com/tomerba6/probabilistic-data-structures/actions/workflows/ci.yml/badge.svg)](https://github.com/tomerba6/probabilistic-data-structures/actions/workflows/ci.yml)

## Build and test

Requires JDK 17 or newer. Maven is not required: the wrapper downloads it.

```bash
./mvnw verify          # 171 tests, the test and coverage reports, and the coverage floor
./mvnw -q exec:java    # the hashing experiments (Tasks 3.5-3.8): average timings per load factor
```

On Windows, use `mvnw.cmd` in place of `./mvnw`.

CI runs the same `verify` on JDK 17, 21 and 25 for every push and pull request to `main`. Each
JDK's run uploads its HTML test report as an artifact, also when a test fails, and the JDK 17 run
uploads the coverage report when the build passes.

`verify` writes an HTML test report, with a row for every test, to `target/reports/surefire.html`.
A failing test stops the build before that report is written; `./mvnw surefire-report:report-only`
then writes it from the results the failed run left in `target/surefire-reports`.

`verify` also writes the JaCoCo coverage report to `target/site/jacoco/index.html`, and fails the build
if line coverage drops below 90%. Coverage is about 94% of lines. It can move by a line between
runs, because one line, in rehashing, runs only when a random hash happens to collide two keys.

The `experiments` package is left out of the report and the floor. It is benchmark code: its
timings are printed for reading, not asserted, so no test runs it.
