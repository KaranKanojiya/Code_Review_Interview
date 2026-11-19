# Code review tools: what to use and what they actually catch

## The honest summary (measured in this repo)

I ran PMD 7.29, SpotBugs and Find Security Bugs 1.14 on the six buggy PRs (`./gradlew pmdMain spotbugsMain`). The model answers list about 50 findings in total; the tools flagged 7 of them:

| Caught by a tool | Missed by every tool |
|---|---|
| `equals` without `hashCode` (ex06) — PMD and SpotBugs | `double` for money, integer-division discount |
| SQL injection via JdbcTemplate (ex02) — **only** with Find Security Bugs | Debit before validating the target account |
| Log injection of user input (ex01) — Find Security Bugs | `@Transactional` on a private method (SonarQube has a rule; PMD/SpotBugs don't) |
| `catch (Exception)` (ex01, ex04, ex05) — PMD | 200 OK on failure; `return null` on failure |
| Empty catch swallowing `InterruptedException` (ex04) — PMD | Non-idempotent retries, 201 treated as failure, no idempotency key |
| `HashMap` in a concurrent class (ex03) — PMD | Plaintext password stored, password hash in the response |
| | Unbounded page size, N+1 queries, off-by-one boundaries |
| | Duplicate and out-of-order message handling |
| | A test that mocks the class under test |

Plus noise: missing `serialVersionUID`, log guards, "literals in if", "don't use threads", and SpotBugs `EI_EXPOSE_REP2` on every constructor-injected dependency.

**Takeaway for interviews:** "I let static analysis and security scanners catch the mechanical issues in CI, so human review can focus on design, correctness, failure handling and security logic, which tools mostly miss." Then give an example from the table.

Reports after running: `build/reports/pmd/main.html`, `build/reports/spotbugs/main.html`.

## The toolbox by layer

### 1. Review platform
- **GitHub pull requests / GitLab merge requests / Bitbucket / Azure DevOps**: inline comments, suggested changes, required reviewers, CODEOWNERS, branch protection (required checks and approvals).
- **Gerrit**: patch-set based review used by Google-style workflows and large open-source projects.
- Habits that matter more than the tool: small PRs (a few hundred lines), a clear description with "how to test", a PR template, CODEOWNERS for critical paths.

### 2. Formatting and style (stop arguing about it)
- **Spotless** with **google-java-format** or **Palantir Java Format**: auto-format in the build; fail CI on drift.
- **Checkstyle**: conventions such as naming and imports, if your team wants them enforced.

### 3. Static analysis (bugs)
- **SpotBugs** (+ **Find Security Bugs** plugin): bytecode analysis, null paths, `equals`/`hashCode`, resource leaks, security patterns.
- **PMD**: source rules: error-prone patterns, empty catches, concurrency smells.
- **Error Prone** (Google) + **NullAway** (Uber): compiler plugins that turn whole bug classes into compile errors; NullAway enforces null-safety with `@Nullable`.
- **SonarQube / SonarQube Cloud** and **SonarQube for IDE** (formerly SonarLint): broad rule set including Spring rules (e.g. non-public `@Transactional`), quality gates on new code, coverage and duplication tracking.
- **IntelliJ inspections**: underrated; run "Inspect Code" before pushing.

### 4. Security
- **Semgrep** or **GitHub CodeQL**: SAST with dataflow; CodeQL is free for public repos on GitHub.
- **Dependency scanning**: Dependabot or Renovate (update PRs), OWASP Dependency-Check, Snyk, GitHub dependency review.
- **Secret scanning**: GitHub secret scanning and push protection, gitleaks, TruffleHog. (Relevant for you: a token in a git remote URL is exactly what these catch.)

### 5. Tests and architecture
- **JaCoCo** (coverage, applied in this build): use it to find untested branches, not as a target number.
- **PIT** (mutation testing): changes your code and checks whether tests fail; finds tests that assert nothing.
- **ArchUnit**: architecture rules as tests ("controllers must not call repositories directly").

### 6. AI review assistants (2026)

| Tool | Notes from 2026 comparisons |
|---|---|
| **GitHub Copilot code review** | Native in GitHub PRs; cheapest if you already have Copilot; described as more diff-scoped |
| **CodeRabbit** | Broad platform support (GitHub, GitLab, Bitbucket, Azure DevOps); combines linters/SAST with an LLM |
| **Cursor Bugbot** | Multi-pass review focused on low false positives; usage-billed |
| **Qodo** (formerly CodiumAI) | Review plus missing-test generation |
| **Greptile** | Indexes the whole codebase; good at cross-file issues, more comments |
| **PR-Agent** (open source) | Self-hosted, bring your own model |

Benchmarks disagree on which is best (rankings change by dataset), so treat them as a first pass. GitHub's own documentation says Copilot reviews can miss problems and need human validation. Sources: [Techplained comparison](https://www.techplained.com/best-ai-code-review-tools), [AIMultiple benchmark](https://aimultiple.com/ai-code-review-tools), [cubic/Martian benchmark summary](https://www.cubic.dev/vs).

## A sensible pipeline for a Java team

1. Pre-commit/IDE: formatter, SonarQube for IDE, IntelliJ inspections.
2. CI on every PR: build + tests + JaCoCo, SpotBugs + Find Security Bugs (or Semgrep/CodeQL), dependency and secret scanning, SonarQube quality gate on new code.
3. Optional AI reviewer as a first pass; humans review design, correctness, failure handling and security logic.
4. Branch protection: required checks + at least one approval + CODEOWNERS for sensitive areas.

## Interview answer: "What tools do you use for code review?"

> "Formatting is automated with Spotless so nobody comments on style. CI runs tests with coverage, SpotBugs with Find Security Bugs, dependency and secret scanning, and a Sonar quality gate on new code. That catches the mechanical issues, like an `equals` without `hashCode` or a concatenated SQL query. Human review then focuses on what tools miss: design, correctness at boundaries, failure handling, idempotency and authorization. I've also tried AI reviewers; they're a useful first pass, but I verify every finding."
