# Model answer — Exercise 3: rate limiter (Karat style)

Fixed code: [`solutions/java/review/ex03_rate_limiter/SlidingWindowRateLimiter.java`](../java/review/ex03_rate_limiter/SlidingWindowRateLimiter.java).

## Part 1 — the bug (say this before you type)

"The test expects the 4th request to be rejected with a limit of 3, but it's allowed. The check is `timestamps.size() > maxRequests`: with 3 stored timestamps, `3 > 3` is false, so a 4th is added. It should be `>=`. That's the off-by-one the customer reported."

Fix: `if (timestamps.size() >= maxRequests) return false;`

## Part 2 — improvements in priority order

| # | Improvement | Why | Test |
|---|---|---|---|
| 1 | **Thread safety**: `HashMap` + `ArrayList` mutated from many request threads. Use `ConcurrentHashMap.computeIfAbsent` and synchronize per client | Gateway filters run concurrently; corrupt state or `ConcurrentModificationException`, and over-admission | `concurrentRequestsNeverExceedTheLimit` |
| 2 | **Window boundary**: `isBefore(cutoff)` keeps a request made exactly one window ago. Use `!isAfter(cutoff)` and document "[t, t + window)" | Off-by-one at the edge; a client is blocked a tick too long | `requestsExpireExactlyWhenTheWindowHasPassed` |
| 3 | **Validate constructor input**: `maxRequests <= 0`, zero/negative/null window, null clock | Fail fast on misconfiguration instead of rejecting everyone silently | `invalidConfigurationIsRejected` |
| 4 | `ArrayList.remove(0)` is O(n); use `ArrayDeque` | Cheap win for high limits | — |
| 5 | Idle clients are never removed: memory grows with distinct client ids. Use a cache with expire-after-access, or periodic eviction | Unbounded memory under many clients or spoofed ids | — |
| 6 | Single-instance only: behind several gateway instances each enforces its own limit. Shared store (Redis with a sorted set or a token bucket in Lua) | Correct limits in production | — |

Good to mention: the class already takes a `Clock`, which is what makes it testable. Praise that.

## Karat process tips

- Ask clarifying questions immediately (time is short): "Is the window inclusive at the boundary?"
- State the bug before fixing; interviewers stop candidates who code first.
- Run the test after every change. Leave time to explain the remaining improvements even if you can't implement them all.
