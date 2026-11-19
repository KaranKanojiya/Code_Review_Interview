# Exercise 1 — "Add money transfer endpoint" (JPMC CodePair style)

**Format:** about 140 lines of Spring code, reviewed out loud. Say what you'd ask, what you'd change and whether you'd approve.
**Time box:** 25 minutes reviewing + 5 minutes for your verdict.
**Files:** [`src/main/java/review/ex01_transfers/`](../src/main/java/review/ex01_transfers) (start with `TransferController`, then `TransferService`, then `Account`).

## The PR, as the author wrote it

> **TRANS-482: Add POST /transfers**
>
> Adds an endpoint to move money between two accounts.
> - `TransferController` accepts `{fromAccount, toAccount, amount}`
> - `TransferService` checks the balance, debits and credits, and is `@Transactional`
> - Errors are caught and logged so the endpoint never returns a 500
> - Counts processed transfers for the dashboard
>
> Tested manually with Postman. Unit tests to follow in a separate PR.

## Your task

1. Read the PR description first, then the files. Narrate as you go.
2. Write your comments as you would post them (see the format in [docs/REVIEW_METHOD.md](../docs/REVIEW_METHOD.md)): label, location, problem, why it matters, suggested fix.
3. Rank them: which ones block the merge?
4. Give a verdict: approve, approve with comments, or request changes.
5. Fix the code in `src/main` and run `./gradlew practice -Pex=ex01` until it's green.
6. Compare with [solutions/answers/ex01-transfers.md](../solutions/answers/ex01-transfers.md).

<details><summary>Hint (only if stuck after 15 minutes)</summary>

Follow the money: what type holds it, what happens if the second account doesn't exist, what the caller sees on failure, and what "transactional" really does on a private method.
</details>
