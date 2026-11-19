# Exercise 4 — "Order events consumer with retries" (messaging PR)

**Format:** a message handler. Every distributed-systems interviewer probes the same things here: duplicates, ordering, retries, and what happens to a message on failure.
**Time box:** 20 minutes + 5 minutes verdict.
**Files:** [`src/main/java/review/ex04_order_events/`](../src/main/java/review/ex04_order_events) (read the Javadoc on `OrderEventHandler` first: it states the ack contract).

## The PR, as the author wrote it

> **ORD-301: Reserve inventory when an order is placed**
>
> - Listens to order events from the queue
> - Saves the latest status for the order dashboard
> - On PLACED, reserves inventory; the inventory service is flaky so we retry 3 times with backoff
> - Never throws, so a bad message can't block the queue

## Your task

Same steps as exercise 1. Run `./gradlew practice -Pex=ex04`. Model answer: [solutions/answers/ex04-order-events.md](../solutions/answers/ex04-order-events.md).

<details><summary>Hint</summary>

The queue delivers at-least-once and not always in order. Read "never throws" together with the ack contract.
</details>
