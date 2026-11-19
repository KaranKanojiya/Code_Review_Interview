# Model answer — Exercise 5: AI-generated payments client

**Verdict: Reject as-is / revise.** It's plausible and clean-looking, but it can charge customers multiple times and hide failures. Fixed code: [`solutions/java/review/ex05_ai_generated_client/PaymentsProviderClient.java`](../java/review/ex05_ai_generated_client/PaymentsProviderClient.java).

## Start with the generated test

"The test mocks the class under test, then asserts on the mock's canned answer. It can't fail no matter what `PaymentsProviderClient` does; it tests Mockito. I'd delete it and write tests against a fake transport: success, 4xx, 5xx, timeout, interrupt."

## Findings

| # | Severity | Finding | Evidence (test) |
|---|---|---|---|
| 1 | Blocker | **Retrying a non-idempotent POST without an idempotency key.** If attempt 1 succeeded but the response was lost (timeout), attempt 2 charges again. Send `Idempotency-Key: <paymentId>`, same on every retry | `retriesOfTheSamePaymentCarryTheSameIdempotencyKey` |
| 2 | Blocker | **Only `200` counts as success.** Providers commonly return `201 Created`; the client then retries a successful charge 3 times (3 charges) and returns null. Treat 2xx as success | `anySuccessStatusIsASuccessAndIsNotRetried` |
| 3 | Blocker | **Returns `null` after all failures.** Callers will NPE or, worse, treat it as "nothing happened". Throw a typed exception | `exhaustedServerErrorsFailLoudlyInsteadOfReturningNull` |
| 4 | Blocker | **Secret in logs**: `log.info("... with API key {}", apiKey)`. Also a hard-coded fallback key (`changeme-default-key`) means a missing config silently uses a fake key. Remove; fail fast when unset | (review) |
| 5 | Major | **No timeouts**: `HttpClient.newHttpClient()` with no connect timeout and requests with no timeout. A hung provider blocks request threads forever | `everyRequestHasATimeout` |
| 6 | Major | **Retries 4xx** (card declined, validation): pointless and can trigger provider rate limits/fraud flags. Retry only 5xx, 429 and I/O errors | `clientErrorsAreNotRetried` |
| 7 | Major | **Interrupt swallowed** during back-off | `interruptStopsRetryingAndIsPreserved` |
| 8 | Major | **JSON built by string concatenation**: a `"` in `customerId` breaks the body or injects fields. Use a JSON mapper | (review) |
| 9 | Major | **Fragile parsing** (`body.split("\"")[3]`): breaks on any field-order change. Parse JSON properly | (review) |
| 10 | Major | **Two constructors, neither marked `@Autowired`**: Spring can't choose and the app fails to start. Mark the injection constructor | (review) |
| 11 | Minor | Fixed 500 ms sleep: use exponential back-off with jitter | — |

## How to talk about AI-generated code

- Review it like any untrusted patch; don't argue about AI itself.
- Check the ticket against the code: "retry on failure" was interpreted literally, without the payments domain knowledge that retries need idempotency.
- For every finding, name the test that would prove it. "Tests pass" is evidence only if the tests could fail.

## What the tools caught

PMD: `catch (Exception)`, an `HttpClient` not closed, "don't use threads" (noise). **Not caught:** double charging, 201-as-failure, null return, logged secret, missing timeouts, the useless test.
