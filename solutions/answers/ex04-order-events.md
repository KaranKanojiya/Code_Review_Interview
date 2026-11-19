# Model answer — Exercise 4: order events consumer

**Verdict: Request changes.** "Never throws" plus "message is acked when the method returns" means every failure silently loses an order. Fixed code: [`solutions/java/review/ex04_order_events/OrderEventHandler.java`](../java/review/ex04_order_events/OrderEventHandler.java).

| # | Severity | Finding | Test |
|---|---|---|---|
| 1 | Blocker | **Failures are swallowed.** After 3 failed attempts it logs and returns normally, so the message is acked and deleted: the order is never reserved and nothing retries it or dead-letters it. Throw after the last attempt | `exhaustedRetriesAreReportedSoTheMessageIsRedelivered` |
| 2 | Blocker | **Not idempotent.** Queues deliver at-least-once; a redelivered PLACED event reserves inventory twice. The store even has `isProcessed`/`markProcessed`, unused | `duplicateDeliveryReservesInventoryOnce` |
| 3 | Blocker | **Out-of-order events overwrite newer state.** A late v1 PLACED after v2 SHIPPED resets the dashboard and reserves stock for a shipped order. Compare versions | `staleEventDoesNotOverwriteNewerState` |
| 4 | Major | **Retries everything**, including `InvalidOrderException` that can never succeed. Retry only transient errors | `permanentErrorIsNotRetried` |
| 5 | Major | **Interrupt swallowed** (`catch (InterruptedException) {}`): shutdown can't stop the consumer and the thread's interrupt status is lost. Restore with `Thread.currentThread().interrupt()` and stop | `interruptIsPreservedAndStopsRetrying` |
| 6 | Major | **State saved before the side effect**: status becomes PLACED even when the reservation fails. Do the work, then save state, then mark processed (one DB transaction for the last two) | asserted in #1's test |
| 7 | Minor | Sleeping in the consumer thread (1 s, 2 s, 3 s) blocks the whole partition/queue consumer. Prefer the broker's redelivery delay or a retry topic for longer back-offs | — |
| 8 | Minor | `"PLACED"` magic string: use an enum | — |

## How to say #1

"Blocking: the Javadoc says the message is acked when `handle` returns. After three failed reservations we log and return, so the message is deleted and the order never gets inventory. Nothing alerts, nothing retries later, and the DLQ never sees it. I'd rethrow after the last attempt so the broker redelivers and eventually dead-letters it, and we alert on DLQ depth."

## Questions for the author

- Is ordering guaranteed per order id (FIFO group / partition key)? Even then, keep the version check.
- What's the DLQ setup and who's alerted?

## What the tools caught

PMD: empty catch block (the swallowed interrupt), `catch (Exception)`. **Not caught:** swallowed final failure, duplicates, stale events, side-effect ordering.
