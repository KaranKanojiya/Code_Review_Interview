# Talking about this project in an interview

This repo is different from your other labs: it's a **practice kit**, not a system you built to learn a technology. You review pull requests at work; this kit is where you sharpened a repeatable method under interview conditions. You'll rarely pitch it as a project. It helps most when they ask "how do you review code?" or "how do you prepare?"

## Where it fits in your story

| Kind | What it is | How to bring it up |
|---|---|---|
| Work experience | Reviewing teammates' pull requests (use the number your resume states) | Lead with this for "how do you review code?" Give one real example of a review comment that mattered |
| **Practice kit (this repo)** | Six deliberately buggy Java and Spring PRs with tests, model answers and a review method | "I practice reviewing with a kit I built…" |

## The pitch

**30 seconds**

> "I review pull requests regularly at work. To get sharper for interview-style reviews, I built a practice kit: six realistic buggy Spring pull requests, such as a money transfer endpoint, customer search, a rate limiter, an event consumer and an AI-generated payments client, each with tests that stay red until the bugs are fixed. I review them out loud with a seven-pass, risk-first method: intent first, then correctness and data, failure handling, security, concurrency, performance, and tests and style last. I also measured what static analysis tools catch: about 7 of roughly 50 findings."

**2 minutes (if they say "tell me more")**

1. **Why:** "Reviewing in an interview is different from at work: a timer, unfamiliar code, and you have to narrate. I wanted a repeatable method, not luck."
2. **The method:** seven risk-first passes, severity labels (`issue (blocking):`, `nit:`), a comment template, and a clear approve or request-changes rule. See [REVIEW_METHOD.md](REVIEW_METHOD.md).
3. **The part I'm proudest of:** pick one:
   - *Measuring the tools:* PMD, SpotBugs and Find Security Bugs caught things like `equals` without `hashCode` and SQL injection, but missed money as `double`, debit-before-validate, `@Transactional` on a private method, and non-idempotent retries. Tools catch patterns; reviewers catch intent and design.
   - *Reviewing AI-generated code:* exercise 5 is a plausible-looking client with non-idempotent retries, a 201 treated as failure, secrets in logs, no timeouts and a test that tests nothing. That's what reviewing AI output looks like in 2026.
4. **How I practice:** one exercise a day with a timer, fix it until the tests pass, compare with the model answer, record which pass would have caught each miss, redo it three days later.

## Answers to likely questions

**"How do you approach a code review?"**
Use the method, not the project: "First I read the PR description and the tests to understand intent. Then I review risk-first: correctness and data, failure handling, security, concurrency and state, performance, and tests and maintainability last. I label severity so the author knows what blocks the merge." Give a real work example if you have one; use an exercise from this kit if you don't.

**"Tell me about a review comment that mattered."** Use a real work example. If you don't have one ready, prepare one before the interview; don't substitute an exercise from this kit as if it were real.

**"How do you review AI-generated code?"** Exercise 5 and the AI section of [TOOLS.md](TOOLS.md): check retries and idempotency, timeouts, error handling, secrets in logs, and whether the tests actually assert anything.

**"Did you use AI to build this?"**
> "Yes, I used an AI coding assistant to help write the exercises. But I've worked through each review myself, and I can walk through any of them." Only say this if it's true.

## Self-check: explain these without notes

| Topic | Where | Must be able to explain |
|---|---|---|
| The method | [REVIEW_METHOD.md](REVIEW_METHOD.md) | The seven passes in order and why risk comes first; when you approve |
| Money and transactions | `exercises/ex01-transfers.md` | Why not `double`; why `@Transactional` on a private method does nothing; validate before debit |
| Security | `exercises/ex02-customers.md` | SQL injection fix; password storage and leaking hashes in responses; unbounded pages |
| Concurrency | `exercises/ex03-rate-limiter.md` | The off-by-one, the window boundary and the thread-safety fix |
| Messaging | `exercises/ex04-order-events.md` | Swallowed failures, idempotency, out-of-order events, handling `InterruptedException` |
| AI-generated code | `exercises/ex05-ai-generated-client.md` | Idempotency keys for retries, timeouts, treating 2xx correctly, secrets in logs |
| Multi-file debugging | `exercises/ex06-pricing.md` | `equals` without `hashCode`; integer money math; boundary bug |
| Tools | [TOOLS.md](TOOLS.md) | What static analysis caught and missed here, and where tools fit in a pipeline |

Practice: `git checkout -- src/main`, pick an exercise, review it out loud in 25 minutes, then `./gradlew practice -Pex=exNN`.

## Don't say

- That the exercises are real bugs from your workplace, or that this kit's findings are your work review examples.
- A review count your resume doesn't support.

## Before making the repo public

- [ ] Commits use a personal or GitHub "noreply" email: `git log --format='%ae' | sort -u`.
- [ ] No company names, internal hostnames, tokens or private paths: `git grep -nIiE 'token|secret|password|internal'` and read what it finds. The deliberately bad code (plaintext passwords, secrets in logs) is expected.
- [ ] Remote URL has no token in it (`git remote -v`).
- [ ] `./gradlew test` passes on a fresh clone.
