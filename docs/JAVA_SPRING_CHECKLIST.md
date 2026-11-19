# Java + Spring review checklist (red flags and what to say)

Scan for these patterns. Each line: **red flag → why → what to suggest**. The exercise where it appears is in brackets.

## Correctness and data

- `double`/`float` for money → binary rounding errors → `BigDecimal`, scale and `RoundingMode` explicit [ex01]
- `BigDecimal.equals` → compares scale (`1.0` ≠ `1.00`) → `compareTo` [snippets]
- `intValue()` / integer division on amounts → truncation → BigDecimal arithmetic [ex06]
- `>` vs `>=` at a threshold, `<=` vs `<` on balances → off-by-one → test the exact boundary [ex01, ex03, ex06]
- `equals` without `hashCode` (or different fields) → broken HashMap/HashSet → same fields in both, or a record [ex06]
- Mutating state before validation finishes → partial updates → validate and load everything first [ex01]
- `LocalDateTime.now()` for timestamps across zones → ambiguity → `Instant`, inject `Clock` [ex03]
- `Optional.get()` without check, `.orElse(null)` → NPE → `orElseThrow` with a domain exception
- String comparison with `==` → identity, not value → `equals`

## Failure handling

- `catch (Exception e)` + log + `return null/false` → caller can't tell failure from success → throw specific exceptions [ex01, ex05]
- HTTP 200 with an error message → clients and alerts see success → proper status + `ProblemDetail` [ex01]
- Consumer that never throws → failed messages acked and lost → rethrow after retries so the broker redelivers / dead-letters [ex04]
- Retrying everything → wasted calls on permanent errors → retry only transient (5xx, 429, timeouts) [ex04, ex05]
- Retrying a POST/charge without an idempotency key → duplicates → idempotency key, same on every retry [ex05]
- Only `status == 200` treated as success → 201/204 retried → `2xx` range [ex05]
- No timeouts on HTTP/DB clients → threads hang → connect + request timeouts [ex05]
- `catch (InterruptedException) {}` → can't shut down → `Thread.currentThread().interrupt()` and stop [ex04, ex05]
- Exceptions without cause (`new X(msg)` inside a catch) → lost stack trace → pass the cause

## Security

- SQL built with `+` (JdbcTemplate, JPQL, native) → injection → bind parameters [ex02]
- Passwords stored plain or hashed with MD5/SHA → `PasswordEncoder` (BCrypt/Argon2) [ex02]
- Entities/DTOs exposing `passwordHash`, internal ids, PII → dedicated response DTOs [ex02]
- Secrets in code, defaults like `changeme`, or in logs → config/secret manager; fail fast if missing [ex05]
- Logging request objects with account numbers/PII → mask, log ids only [ex01]
- User input in logs unsanitized → log injection (CRLF) → sanitize/structured logging [ex01]
- Missing authorization: user can act on any id → check ownership ("IDOR")
- `@CrossOrigin("*")`, CSRF disabled with cookie auth, permissive security config

## Concurrency and state

- Mutable instance fields in `@Service`/`@Controller` (singletons) → shared across requests → no state, or `AtomicX`/metrics [ex01]
- `HashMap`/`ArrayList`/`SimpleDateFormat` shared between threads → corruption → `ConcurrentHashMap`, `DateTimeFormatter` [ex03]
- Check-then-act (`if (!map.containsKey(k)) map.put(...)`) → race → `computeIfAbsent`/`putIfAbsent`
- Read-modify-write on balances/stock without locking → lost updates → `SELECT ... FOR UPDATE`, `@Version`, or atomic SQL update [ex01]
- Locks acquired in inconsistent order → deadlock → lock in a fixed order [ex01 solution]
- Double-checked locking without `volatile`
- `parallelStream()` with shared mutable state or in a web request

## Spring specifics

- `@Transactional` on private methods or called via `this` → not applied (proxy) → public method on the bean, called from outside [ex01]
- `@Transactional` + checked exception → no rollback by default → `rollbackFor` or unchecked exceptions
- Field injection (`@Autowired` on fields) → hidden dependencies, hard to test → constructor injection
- Two constructors, none `@Autowired` → Spring can't choose → startup failure [ex05]
- Missing `@Valid` on `@RequestBody` → validation annotations ignored [ex01, ex02]
- `@RequestParam` limits with no cap → unbounded queries [ex02]
- Lazy JPA associations accessed outside a transaction → `LazyInitializationException`; or in loops → N+1 [ex02]
- `open-in-view` left on in APIs → hidden queries during serialization

## Performance and resources

- Query inside a loop → N+1 → one batched query (`IN`), join fetch, or entity graph [ex02]
- `findAll()` / no `LIMIT` → memory and latency blow-up → pagination, cap page size [ex02]
- Resources not closed (streams, connections, `HttpClient` in a loop) → leaks → try-with-resources, reuse clients
- `Thread.sleep` in request/consumer threads → blocked capacity → scheduled retry, broker delay [ex04]
- String concatenation in loops → `StringBuilder` (only matters in hot loops)
- Unbounded caches/maps keyed by user input → memory leak → bounded cache with eviction [ex03]

## Tests

- No tests for risky logic ("tests in a follow-up PR") → ask for them in this PR [ex01]
- Test mocks the class under test → tests nothing [ex05]
- Tests without assertions, or asserting only "not null"
- Only the happy path → ask for boundary, failure and concurrency cases
- Tests depend on real time/network → inject `Clock`, fake transports [ex03, ex05]

## Readability (last, briefly)

- Methods doing several things (SRP), magic numbers/strings, misleading names, commented-out code, duplicated logic.

## My personal misses

Add every finding you missed in practice here, with the pattern that would have caught it.

- 
