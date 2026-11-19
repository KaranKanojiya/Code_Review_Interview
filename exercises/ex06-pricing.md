# Exercise 6 — Multi-file bug hunt (Amazon assessment style)

**Format:** a small multi-file codebase and one failing scenario test. Several root causes in different files combine into one wrong number. Newer Amazon assessments run this in an IDE with an AI assistant that won't give the answer directly; the skill is narrowing down systematically.
**Time box:** 30 minutes.
**Files:** [`src/main/java/review/ex06_pricing/`](../src/main/java/review/ex06_pricing)

## The prompt

> Customers report wrong cart totals: sometimes too high, sometimes too low, and the pricing service is being called more than expected.
> `CartPricerTest.typicalCartsArePricedCorrectly` reproduces it. Find **every** root cause and fix it with the smallest safe change.

## Your task

1. Run `./gradlew practice -Pex=ex06 --tests '*typicalCarts*'` and read the first failing assertion.
2. Form a hypothesis, check it (read code, add a temporary assertion or print), fix, re-run. Repeat until green.
3. Keep a short log: symptom → hypothesis → evidence → fix. Interviewers grade the process.
4. Run all of `./gradlew practice -Pex=ex06`.
5. Model answer: [solutions/answers/ex06-pricing.md](../solutions/answers/ex06-pricing.md).
