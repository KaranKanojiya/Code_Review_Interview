# Code Review Interview Kit (Java + Spring)

Practice for the rounds where you **review or debug existing code** instead of writing new code: JPMC CodePair PR reviews, Citi Karat "fix the bug + 3 improvements", Amazon's multi-file debugging assessment, "which snippet goes to production?", and reviewing AI-generated code.

> **6 realistic buggy PRs** | **34 tests that fail until you fix them** | **model answers with exact wording** | **review method, checklist, tools guide**

## How it works

```
src/main/java/review/exNN_*/     the pull request you review (deliberately buggy)
solutions/java/review/exNN_*/    reference fixes (same classes and API)
src/test/java/review/exNN_*/     one test suite for both
exercises/exNN-*.md              the PR description and your task
solutions/answers/exNN-*.md      model answer: ranked findings, what to say, verdict
```

```bash
./gradlew practice -Pex=ex01   # tests against YOUR code: red until you fix the PR
./gradlew test                 # same tests against the reference solutions: always green (34/34)
./gradlew pmdMain spotbugsMain # what static analysis finds in the buggy code (reports in build/reports)
```

## The exercises

| # | PR | Interview format | Main themes | Tests |
|---|---|---|---|---|
| [01](exercises/ex01-transfers.md) | Money transfer endpoint | JPMC CodePair | money as double, debit-before-validate, `@Transactional` on private method, 200 on failure, PII in logs, races | 9 |
| [02](exercises/ex02-customers.md) | Customer search and sign-up | Security-focused review | SQL injection, plaintext password, password in response, unbounded page, N+1, status codes | 5 |
| [03](exercises/ex03-rate-limiter.md) | Rate limiter | Citi Karat (bug + 3 improvements) | off-by-one, window boundary, thread safety, validation | 5 |
| [04](exercises/ex04-order-events.md) | Order events consumer | Messaging/distributed systems | swallowed failures, idempotency, out-of-order events, retry policy, interrupts | 5 |
| [05](exercises/ex05-ai-generated-client.md) | AI-generated payments client | AI-output review | non-idempotent retries, 201 as failure, null on failure, secrets in logs, no timeouts, useless test | 6 |
| [06](exercises/ex06-pricing.md) | Cart pricing | Amazon multi-file debugging | `equals` without `hashCode`, integer money math, boundary bug | 4 |

Plus [docs/SNIPPET_DUELS.md](docs/SNIPPET_DUELS.md): 14 "which goes to production?" pairs.

## How to practice (one exercise per day)

1. Read `exercises/exNN-*.md`. Start a timer (20–30 minutes).
2. Review **out loud** using the 7 passes in [docs/REVIEW_METHOD.md](docs/REVIEW_METHOD.md). Write comments with labels (`issue (blocking):`, `nit:`…). Give a verdict.
3. Fix the code in `src/main`; run `./gradlew practice -Pex=exNN` until green.
4. Compare with `solutions/answers/exNN-*.md`. For every miss, note which pass would have caught it and add it to "My personal misses" in [docs/JAVA_SPRING_CHECKLIST.md](docs/JAVA_SPRING_CHECKLIST.md).
5. Reset and redo it in 3 days: `git checkout -- src/main`.

## Docs

- [docs/REVIEW_METHOD.md](docs/REVIEW_METHOD.md) — why reviews feel hard, the 7 risk-first passes, narration script, severity labels, comment template, approve/reject rule, 45-minute plan, what interviewers score
- [docs/JAVA_SPRING_CHECKLIST.md](docs/JAVA_SPRING_CHECKLIST.md) — red flags with what to say, mapped to the exercises
- [docs/INTERVIEW_FORMATS.md](docs/INTERVIEW_FORMATS.md) — JPMC, Citi Karat, Amazon, Morningstar, AI rounds (with candidate-report sources) + Karat Java fundamentals answers
- [docs/TOOLS.md](docs/TOOLS.md) — tools by layer, AI reviewers in 2026, and **measured** results: what PMD/SpotBugs/Find Security Bugs caught in these PRs and what they missed
- [docs/SNIPPET_DUELS.md](docs/SNIPPET_DUELS.md) — 14 snippet pairs

## Setup

JDK 21 only; Gradle wrapper included, Maven Central only. No Docker, no database server (tests use in-memory H2). Same setup steps as the other prep projects.
