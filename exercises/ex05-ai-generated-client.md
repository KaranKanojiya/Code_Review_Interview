# Exercise 5 — Review AI-generated code (AI-assisted round style)

**Format:** newer rounds give you code an AI assistant wrote (or let you use one) and grade how well you **verify** it: requirements, failure modes, security, tests. The code looks clean; the risk is in assumptions.
**Time box:** 20 minutes + 5 minutes verdict.
**Files:** [`src/main/java/review/ex05_ai_generated_client/`](../src/main/java/review/ex05_ai_generated_client)

## The PR, as the author wrote it

> **PAY-918: Payments provider client (generated with AI assistant)**
>
> Ticket: "Call the provider to charge the customer's card. The provider is sometimes flaky, so retry on failure."
>
> I asked the assistant for a client with retries and it produced `PaymentsProviderClient`. It compiles and the generated test passes:
>
> ```java
> @Test
> void chargeReturnsResult() {
>     PaymentsProviderClient client = mock(PaymentsProviderClient.class);
>     when(client.charge(any())).thenReturn(new ChargeResult("ch_1", "succeeded"));
>     assertThat(client.charge(new ChargeRequest("p1", "c1", BigDecimal.TEN, "EUR")).status()).isEqualTo("succeeded");
> }
> ```

## Your task

1. Review the client **and** the generated test above.
2. For each finding, say what evidence would prove it (a test you'd write).
3. Verdict: accept, revise, or reject.
4. Fix and run `./gradlew practice -Pex=ex05`. Model answer: [solutions/answers/ex05-ai-generated-client.md](../solutions/answers/ex05-ai-generated-client.md).

<details><summary>Hint</summary>

What happens to the customer's card if the first POST succeeded but the response was lost? What does the provider return for a created charge? What does the test above actually test?
</details>
