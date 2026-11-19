# Model answer — Exercise 1: money transfer PR

**Verdict: Request changes.** Money can be created or destroyed, and failures look like success to the client. Fixed code: [`solutions/java/review/ex01_transfers/`](../java/review/ex01_transfers).

## Findings in the order I'd raise them

| # | Severity | Where | Finding | Test that proves it |
|---|---|---|---|---|
| 1 | Blocker | `Account` | Money stored as `double`: 0.30 − 0.10 × 3 ≠ 0. Use `BigDecimal` end to end | `moneyArithmeticIsExact` |
| 2 | Blocker | `TransferService.doTransfer` | Debit is saved **before** the target account is loaded; unknown target = money vanishes. Validate and load both accounts first | `unknownTargetLeavesTheSourceUntouched` |
| 3 | Blocker | `doTransfer` | `@Transactional` on a **private** method called from the same class does nothing: Spring proxies only intercept public calls from outside. The transfer is not atomic | (needs a Spring context; explain) |
| 4 | Blocker | `transfer` + controller | `catch (Exception)` returns `null`; controller turns it into **HTTP 200 "Transfer failed"**. Clients and monitoring see success. Throw domain exceptions, map to 404/422/400 | `failuresAreReportedNotSwallowed`, `unknownAccountReturns404NotA200` |
| 5 | Blocker | `transfer` | No validation: negative amount moves money backwards; same-account transfer allowed. Add `@Valid` + checks | `negativeAmountIsRejected`, `transferToTheSameAccountIsRejected`, `invalidRequestReturns400` |
| 6 | Major | `doTransfer` | Off-by-one: `compareTo(...) <= 0` rejects transferring the exact balance | `canTransferTheWholeBalance` |
| 7 | Major | whole flow | Concurrency: two transfers on the same account race (read-check-write). Need row locks (`SELECT ... FOR UPDATE` in id order) or optimistic `@Version` | (explain; flaky to test) |
| 8 | Major | `transfersProcessed++` | Mutable `int` in a singleton bean, incremented from many request threads: lost updates. `AtomicInteger`, or better a Micrometer counter | (explain) |
| 9 | Major | `log.info(... request)` | Logs full account numbers (PII) and unsanitised user input (log injection; Find Security Bugs flags `CRLF_INJECTION_LOGS`). Mask; log the transfer id | (explain) |
| 10 | Major | API design | No idempotency key: a client retry after a timeout transfers twice | (explain) |
| 11 | Minor | controller | `ResponseEntity<Object>` with a String body on error; return a typed body and `ProblemDetail` (RFC 9457) | — |
| 12 | Minor | PR | "Unit tests to follow in a separate PR": tests belong in this PR for money-moving code | — |

## How to say the top ones

- **#1:** "Blocking: `Account` keeps the balance as a `double`. Binary floating point can't represent 0.10 exactly, so balances drift: three transfers of 0.10 from 0.30 leave 2.7e-17. Please use `BigDecimal` for the field and all arithmetic."
- **#2:** "Blocking: the debit is saved before we know the target exists. If `toAccount` is wrong, the money is gone from the source and never arrives. Could we load and validate both accounts first, then debit and credit together?"
- **#3:** "Blocking: `@Transactional` on a private method called via `this` isn't applied: Spring's proxy only sees calls coming through the bean from outside. So there's no transaction at all. Moving the annotation to the public `transfer` method fixes it."
- **#4:** "Blocking: on any failure the endpoint returns 200 with 'Transfer failed'. The caller can't distinguish success, a typo in the account, or insufficient funds, and our error-rate alerts never fire. Let the service throw specific exceptions and map them to 404, 422 and 400."

## Questions for the author

- What should happen if the client times out and retries? (Leads to idempotency keys.)
- Is the repository backed by a database in production? Which isolation level? (Leads to locking.)
- Who reads `transfersProcessed`? (Probably should be a metric.)

## Praise (be specific)

- Constructor injection and small focused classes.
- Separate exceptions for "not found" and "insufficient funds": good start; they just need to reach the client.

## What I'd leave out

Naming of `doTransfer`, missing `serialVersionUID` on the exceptions, log-level guards. Static analysis flags these; they don't matter next to money correctness.

## What the tools caught (see [docs/TOOLS.md](../../docs/TOOLS.md))

PMD: `catch (Exception)`. Find Security Bugs: log injection. **Not caught by any tool:** `double` money, debit-before-validate, `@Transactional` on a private method (SonarQube has a rule for this), the 200-on-failure, missing validation, the off-by-one, the race, idempotency.
