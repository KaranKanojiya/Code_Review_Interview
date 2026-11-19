# AGENTS.md

Code review interview kit: deliberately buggy Spring "PRs" in `src/main`, reference fixes in `solutions/java`, one shared test suite.

## Commands
```bash
./gradlew test                    # tests vs solutions/java (must stay green)
./gradlew practice -Pex=ex01      # tests vs src/main (expected to FAIL until the user fixes them)
./gradlew pmdMain spotbugsMain    # static analysis on the buggy code
```

## Rules
- **Never fix the bugs in `src/main`**: they are the exercises. Fixes go in `solutions/java` only.
- `solutions/java` must keep the same class names and public API as `src/main` (tests compile against main and run against solutions).
- Each exercise is self-contained in `review.exNN_*`; unchanged files are copied verbatim into the solution package.
- Every testable finding has a test that fails on `src/main` and passes on `solutions/java`; untestable findings are documented in `solutions/answers/`.
- If findings change, update `exercises/`, `solutions/answers/` and the measured table in `docs/TOOLS.md`.
- No secrets (even fake ones that look real), absolute paths or company-specific content.
