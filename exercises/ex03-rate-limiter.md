# Exercise 3 — "Fix the failing test, then make 3 improvements" (Citi Karat style)

**Format (as candidates report it):** about 5 Java fundamentals questions, then a given codebase with a failing unit test. Fix the bug, then propose and implement improvements. You must explain your approach before coding.
**Time box:** 15 minutes for the bug, 20 minutes for improvements.
**File:** [`src/main/java/review/ex03_rate_limiter/SlidingWindowRateLimiter.java`](../src/main/java/review/ex03_rate_limiter/SlidingWindowRateLimiter.java)

## The prompt

> The API gateway uses this rate limiter for every request. A customer reports being able to send one more request than their plan allows.
>
> 1. `SlidingWindowRateLimiterTest.allowsExactlyMaxRequestsPerWindow` is failing. Find and fix the bug.
> 2. Then suggest at least three improvements and implement the ones you think matter most.

## Your task

1. Run `./gradlew practice -Pex=ex03` and read the failure.
2. Say out loud what you think is wrong **before** changing code.
3. Fix it; run again.
4. List your improvements in priority order, with one sentence each on why. Implement the top ones.
5. Compare with [solutions/answers/ex03-rate-limiter.md](../solutions/answers/ex03-rate-limiter.md).

**Warm-up (the Java questions Karat asks first, from candidate reports):** `equals`/`hashCode` contract, checked vs unchecked exceptions, what `final` means on a class/method/variable, how Spring dependency injection works, JVM heap generations. Answers in [docs/INTERVIEW_FORMATS.md](../docs/INTERVIEW_FORMATS.md#citi-karat).
