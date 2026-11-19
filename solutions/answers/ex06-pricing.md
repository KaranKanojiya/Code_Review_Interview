# Model answer — Exercise 6: multi-file pricing bug

Three independent root causes in three files produce one failing test. Fixed code: [`solutions/java/review/ex06_pricing/`](../java/review/ex06_pricing).

## Debugging log (the process interviewers grade)

1. **Symptom:** first assertion fails: total is 54.99, expected 50.00.
   **Hypothesis:** shipping charged at exactly 50.00. **Evidence:** `CartPricer` uses `compareTo(threshold) > 0`; requirement says "50.00 or more". **Fix:** `>= 0`.
2. **Symptom:** next assertion: price source called 3 times, expected 2.
   **Hypothesis:** the cache misses for equal keys. `PriceCache` is a `ConcurrentHashMap<ProductKey, ...>`; two `new ProductKey("SKU-1","EU")` should hit. **Evidence:** `ProductKey` overrides `equals` but not `hashCode`, so equal keys land in different buckets. **Fix:** add `hashCode` with the same fields (or make it a record).
3. **Symptom:** 15% off 99.99 gives 84, expected 84.99.
   **Hypothesis:** precision lost in `DiscountPolicy`. **Evidence:** `amount.intValue()` truncates cents and the division is integer division. **Fix:** BigDecimal arithmetic with `divide(100, 2, HALF_UP)`, and validate 0–100.

## Findings table

| # | File | Finding | Test |
|---|---|---|---|
| 1 | `ProductKey` | `equals` without `hashCode`: hash-based collections break (cache misses, duplicates in sets) | `equalKeysHaveEqualHashCodes` |
| 2 | `DiscountPolicy` | `intValue()` + integer division truncate money | `discountKeepsTheCents` |
| 3 | `CartPricer` | `>` vs `>=` at the free-shipping threshold | `orderOfExactlyTheThresholdShipsFree` |
| 4 | `CartPricer` | (Improvement) no validation of quantity ≤ 0 | — |

## Interview tips for this format

- Read the **first** failing assertion only; fix; re-run. Don't try to read the whole codebase first.
- Say each hypothesis before testing it.
- With an AI assistant available: ask it targeted questions ("where is ProductKey hashed?"), then verify yourself. Graders look at whether you verify.
- This is the same bug family as the reported Citi Karat `Trade.equals/hashCode` question.

## What the tools caught

PMD and SpotBugs both flag `equals` without `hashCode` (#1). Neither catches the money truncation or the boundary.
