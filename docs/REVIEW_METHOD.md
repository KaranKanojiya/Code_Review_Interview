# A repeatable code review method (for work and for interviews)

## Why code review feels hard

Most people struggle for the same few reasons, and each has a fix:

| Symptom | What's going wrong | Fix |
|---|---|---|
| "I read it top to bottom and nothing jumps out" | Reading like prose instead of hunting | Read the **PR description and entry points first**, then follow the data, using the passes below |
| "I found 15 things but missed the big one" | Every comment feels equally important | **Risk first**: correctness, data loss, security. Style last or never |
| "I go quiet while I think" | In interviews, silence scores zero | Narrate a fixed script (below); thinking out loud *is* the answer |
| "I'm not sure if it's really a bug" | Fear of being wrong | Phrase it as a question with a concrete scenario: "What happens if `toAccount` doesn't exist?" |
| "I point out problems but not solutions" | Half a comment | Every issue gets a suggested fix, even one line |
| "I run out of time" | No time box | Use the 45-minute plan below |

## The 7 passes (risk-first)

Do them in this order. Most interview PRs hide their blockers in passes 1–4.

1. **Intent and design** — What is the change supposed to do (ticket/description)? Does the code do that? Is it in the right place (controller vs service vs repository)? Anything missing (tests, validation, migration)?
2. **Correctness and data** — Follow one request end to end. Edge cases: null, empty, zero, negative, max, boundary (`>` vs `>=`), duplicates. Money as `BigDecimal`. Time zones. What if step 2 fails after step 1 succeeded?
3. **Failure handling** — What does the caller see on failure? Swallowed exceptions, `return null`, `catch (Exception)`, 200 on error, retries of non-idempotent operations, missing timeouts, interrupts.
4. **Security** — Untrusted input into SQL/shell/paths/logs, secrets in code or logs, PII in logs/responses, authorization (can user A act on user B's data?), plaintext passwords.
5. **Concurrency and state** — Mutable fields in singleton beans, check-then-act races, non-thread-safe collections/formatters, transactions and locking, `@Transactional` pitfalls (private/self-invocation, checked exceptions don't roll back).
6. **Performance and resources** — N+1 queries, unbounded results/pagination, loading everything into memory, unclosed resources, blocking calls on hot paths.
7. **Tests and maintainability** — Do tests exist, and could they fail if the code were wrong? Naming, duplication, single responsibility, dead code. Last, and briefly.

## The narration script (say this out loud in an interview)

1. "Let me restate what this PR does: …" (shows you read the description)
2. "I'll start from the entry point, `TransferController.transfer`, and follow the request."
3. For each finding: **where → what → why it matters → fix**.
   > "In `doTransfer`, the debit is saved before we load the target account. If the target doesn't exist, the money disappears from the source. I'd load and validate both first, then debit and credit together."
4. Mark severity as you go: "This one blocks the merge." / "This is minor."
5. Ask questions when unsure: "Is this repository backed by a database with row locking?"
6. Close with a verdict and a summary: "I'd request changes. Three blockers: money in `double`, debit before validation, failures returned as 200. The rest are follow-ups."

## Severity labels

Use [Conventional Comments](https://conventionalcomments.org/) style labels, with Google's severity wording ([Google eng-practices: comments](https://google.github.io/eng-practices/review/reviewer/comments.html)):

| Label | Meaning | Example |
|---|---|---|
| `issue (blocking):` | Must fix before merge: bug, data loss, security | Plaintext passwords stored |
| `issue:` / `suggestion:` | Should fix; could be a follow-up | N+1 query |
| `question:` | You suspect a problem; ask with a scenario | "What happens on a client retry?" |
| `nit:` | Trivial, optional | Naming |
| `praise:` | Something genuinely good (be specific) | "Nice use of constructor injection" |
| `thought:` / `FYI:` | Non-blocking idea for later | Move to a metric later |

## Comment template

```
issue (blocking): <one-line problem>

<where> does <what>. <scenario that breaks>. <impact on users/data/security>.
Suggestion: <concrete fix, ideally a code snippet>.
```

Example:

```
issue (blocking): SQL injection in searchByName

`name` is concatenated into the SQL string, so `x' OR 1=1 --` returns every customer.
Suggestion: use a bind parameter — `WHERE name LIKE ?` with "%" + name + "%" as the argument.
```

## The approve / request-changes decision

Google's standard: approve once the change **definitely improves the overall code health**, even if it isn't perfect ([eng-practices: standard](https://google.github.io/eng-practices/review/reviewer/standard.html)). In practice:

- **Request changes** if there's any blocker: incorrect results, data loss, security hole, a failure that's silently swallowed, or missing tests for risky code.
- **Approve with comments** if only suggestions/nits remain.
- In interviews, always state the verdict and the 2–3 reasons. Interviewers explicitly ask "would you approve this?"

## 45-minute interview plan (for a 120–150 line PR)

| Minutes | Do |
|---|---|
| 0–3 | Read the description; restate the intent; ask 1–2 clarifying questions |
| 3–8 | Skim structure: entry point, classes, annotations, dependencies |
| 8–30 | Passes 1–5, narrating; note findings with severity as you go |
| 30–37 | Passes 6–7 quickly; note 1–2 test gaps |
| 37–42 | Summarize: blockers first, verdict |
| 42–45 | Answer pushback; offer to fix the top blocker live |

## What interviewers score (from published rubrics)

- **Found the load-bearing issue** (the one bug that matters), not just style.
- **Prioritized**: separates "bug" from "could be cleaner" from "wouldn't block".
- **Justified** each comment with a concrete scenario and trade-off.
- **Tone**: comments a teammate would welcome.
- **Knows what to leave out**: one rubric's most senior signal is "what would you not bring up?"

Sources: [Clarity-Hire rubric](https://clarity-hire.com/blog/code-review-interview-questions-rubric), [Metaheuristic: code review is the interview](https://www.metaheuristic.co/blog/code-review-is-the-interview/), [Interview AiBox: AI-era code review interviews](https://interviewaibox.co/en/blog/ai-era-code-review-interview-guide-2026).

## Practicing deliberately

1. Do an exercise cold, timed, out loud (record yourself).
2. Compare your findings with the model answer: which pass would have caught each miss?
3. Add every miss to your personal list at the bottom of [JAVA_SPRING_CHECKLIST.md](JAVA_SPRING_CHECKLIST.md).
4. Redo the exercise 3 days later without notes.
5. At work: review one teammate PR a day using the 7 passes, and post comments with labels.
