# Code review and debugging rounds: formats by company

Compiled 19 November 2026 from candidate reports (Reddit, LeetCode Discuss, Glassdoor) and interview-prep write-ups. These are other people's experiences, not company statements; ask your recruiter for the exact format.

| Company | Round | Format | Practice with |
|---|---|---|---|
| JPMorgan Chase | Superday **CodePair** | ~120–150 lines of Java/Spring on HackerRank; review out loud: what you'd ask, suggest, and whether you'd approve. Then a DSA problem | ex01, ex02, then any exercise timed |
| Citi | **Karat** first round (60 min) | ~5 Java/Spring questions, then a given codebase: fix a failing test, then improvements or 1–2 new methods; explain before coding | ex03, ex06 + fundamentals below |
| Amazon | Online assessment (newer variant) | Debug or build in a multi-file repository with an AI assistant that won't hand over the answer | ex06, ex05 |
| Morningstar | Techno-managerial | "Which of these two snippets goes to production, and why?" | [SNIPPET_DUELS.md](SNIPPET_DUELS.md) |
| AI-allowed rounds (Meta, Canva, Shopify per your workbook research) | Coding with AI | Graded on reviewing and fixing AI output, not on prompting | ex05 |
| Lead roles (JPMC, Mastercard, HSBC postings) | Any | Postings ask you to lead AI-assisted development and validate AI output for correctness and security | ex05 + REVIEW_METHOD |

---

## JPMorgan Chase — CodePair PR review

**What candidates report**
- "I was given around 120–150 lines of Java Spring code and asked to review it for improvements and potential issues. The focus areas included exception handling, if-else logic, adherence to the SRP, annotations, and overall code quality." (March 2026) — [r/JPMorganChase](https://www.reddit.com/r/JPMorganChase/comments/1s6rw57/interview_experience_experienced_software/)
- "You basically just have to talk out loud with the interviewer as you're looking through the code. You have to say what you'd ask, what you'd suggest, and if you'd approve it." — [r/leetcode](https://www.reddit.com/r/leetcode/comments/1qes8vt/jpmorgan_interview_experience_swe2swe3/)
- Bangalore SWE2: comments added directly in the code; included compile errors, naming, data hiding, modular functions; then asked to run the code — [LeetCode Discuss](https://leetcode.com/discuss/post/7313013/jpmorgan-chase-co-swe-2-associate-javapy-r44y/)
- Prep guides list hardcoded credentials, SQL injection, `volatile`/`synchronized` misuse and SRP violations, and say explaining **why** scores higher — [TechPrep](https://www.techprep.app/blog/jpmorgan-interview-process)

**How to win it**
1. Restate the PR intent; follow the request from the controller down (see [REVIEW_METHOD.md](REVIEW_METHOD.md)).
2. Lead with correctness, failure handling, security, concurrency. Mention style only at the end.
3. Give a clear verdict: "Request changes: three blockers…".
4. Be ready to defend each point under pushback ("why not just catch Exception?").
5. Expect a possible "now run it/fix it" step: keep compile errors and imports in mind.

---

## Citi — Karat

**What candidates report**
- One hour: intro and project (5 min), Java fundamentals (~15 min), coding (~40 min) — [Glassdoor](https://www.glassdoor.co.uk/Interview/Citi-Senior-Software-Developer-Interview-Questions-EI_IE8843.0,4_KO5,30.htm)
- "5 Java theory based questions and then … resolve a bug and make 3 improvements" on a restaurant order delivery system (July 2026) — same Glassdoor page
- "Theory questions: equals() and hashCode(), dependency injection, writing testable classes, JVM heap generations… then a large codebase to fix a bug, then implement 2 methods. Before coding, I had to explain the approach." (May 2026) — [r/interviews](https://www.reddit.com/r/interviews/comments/1ovlz56/karat_interview_for_java_dev_at_citi_bank/)
- Bug examples reported: wrong data type (timestamp String vs float); a `Trade` whose `equals`/`hashCode` used only `symbol`, so distinct trades collapsed in a `HashSet` — [Glassdoor (London)](https://www.glassdoor.ca/Interview/Citi-Senior-Java-Developer-Interview-Questions-EI_IE8843.0,4_KO5,26.htm), [InterviewFox](https://interviewfox.ai/interview-questions/citi-karat-oa-guide/)

**How to win it**
- It's cramped: ask clarifying questions immediately, explain before coding (they enforce it), run the test after every change.
- Read the failing assertion, form a hypothesis, check it, fix the smallest thing.
- For "improvements", give a prioritized list with one-line reasons first, then implement the top ones.

<a id="citi-karat"></a>
### Karat Java fundamentals: quick answers

| Question | Answer in 2–3 sentences |
|---|---|
| `equals`/`hashCode` contract? | Equal objects must have equal hash codes; use the same fields in both. Break it and HashMap/HashSet lookups fail (exercise 6, the reported `Trade` bug). |
| Checked vs unchecked exceptions? | Checked must be declared/handled (recoverable conditions, I/O); unchecked (`RuntimeException`) signal programming or domain errors. Spring's `@Transactional` rolls back on unchecked by default, not checked. |
| `final` on class / method / variable? | Class can't be subclassed; method can't be overridden; variable can't be reassigned (the object it points to can still change). Effectively final = never reassigned, so lambdas can capture it. |
| How does Spring DI work? | The container creates beans and injects their dependencies, preferably through the constructor, so classes don't build their own collaborators. Makes code testable: pass fakes in tests. |
| How do you write a testable class? | Constructor-injected dependencies, no static/global state, inject `Clock` and I/O seams, small methods with clear inputs/outputs (see the `Clock`, `Sleeper`, `Transport` seams in the exercises). |
| JVM heap generations? | Young (Eden + survivor spaces) for new objects, collected often; objects that survive several collections are promoted to the old generation, collected less often. G1/ZGC manage regions but keep the generational idea. |
| `HashMap` vs `ConcurrentHashMap`? | HashMap isn't thread-safe; CHM allows concurrent reads and fine-grained locked writes, and offers atomic `computeIfAbsent`/`merge`. |

---

## Amazon — debugging in a repository (newer OA variant)

From your workbook's candidate-report research: the newer online assessment asks you to debug or build code in a multi-file repository with an AI assistant that won't give the answer directly; a Spring Boot option exists. Practice: exercise 6 (multi-file root causes) and exercise 5 (verifying AI output). Keep a symptom → hypothesis → evidence → fix log; it's what makes the process visible.

---

## Morningstar — "which snippet goes to production?"

From your playbook's research: "which of two snippets goes to production and why?" Practice with [SNIPPET_DUELS.md](SNIPPET_DUELS.md): pick one, name the failure scenario of the other, and mention what you'd still change in the winner.

---

## AI-assisted and AI-output review rounds

Common guidance across 2026 write-ups:
- Treat AI output as an untrusted patch: restate the contract, check every changed file, prove important bugs with tests, check dependencies and security, and finish with accept / revise / reject — [CSWork CLEAR framework](https://cswork.org/library/article/how-to-review-ai-generated-code-in-a-technical-interview-bugs-tests-security-and-trade-offs)
- "Passing tests are evidence, not proof": a generated test can mirror the implementation's mistake — same source; exercise 5 shows one.
- Interviewers pick your strongest comment and argue against it; comments pasted from an AI collapse under that — [Clarity-Hire](https://clarity-hire.com/blog/code-review-interview-questions-rubric)
- GitHub's own docs say Copilot reviews can miss problems and should be validated by a human — cited in [nrml](https://nrml.dev/articles/reviewing-ai-generated-code)
