# Snippet duels: "which one goes to production?"

Morningstar-style prompt: two snippets, pick one, explain. A strong answer has three parts: **which one**, **the scenario where the other fails**, **what you'd still change in the winner**. Answers are folded; decide before opening.

---

### 1. Money

```java
// A
double total = price * quantity;
// B
BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
```

<details><summary>Answer</summary>

**B.** `double` can't represent most decimal fractions (0.1), so totals drift and comparisons fail. Still change: make sure `price` is a `BigDecimal` from the source (not `BigDecimal.valueOf(someDouble)` after the damage), and set scale/rounding where you divide.
</details>

### 2. Comparing BigDecimal

```java
// A
if (amount.equals(BigDecimal.ZERO)) { ... }
// B
if (amount.compareTo(BigDecimal.ZERO) == 0) { ... }
```

<details><summary>Answer</summary>

**B.** `equals` compares scale too: `new BigDecimal("0.00").equals(BigDecimal.ZERO)` is false. Same trap with `BigDecimal` as a HashMap key. Alternative: `amount.signum() == 0`.
</details>

### 3. Injection style

```java
// A
@Service
public class OrderService {
    @Autowired private OrderRepository repository;
}
// B
@Service
public class OrderService {
    private final OrderRepository repository;
    public OrderService(OrderRepository repository) { this.repository = repository; }
}
```

<details><summary>Answer</summary>

**B.** Constructor injection makes dependencies explicit and `final`, lets you construct the class in a plain unit test, and fails at startup if something's missing. Field injection hides dependencies and needs reflection or a Spring context to test.
</details>

### 4. Shared formatter

```java
// A
private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
// B
private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
```

<details><summary>Answer</summary>

**B.** `SimpleDateFormat` keeps mutable state and corrupts results when shared between request threads. `DateTimeFormatter` is immutable and thread-safe. Still change: be explicit about zone/locale where it matters.
</details>

### 5. Querying

```java
// A
jdbc.query("SELECT * FROM users WHERE email = '" + email + "'", mapper);
// B
jdbc.query("SELECT id, email, name FROM users WHERE email = ?", mapper, email);
```

<details><summary>Answer</summary>

**B.** A is SQL injection. B binds the parameter and selects only needed columns (no accidental password hash). Still change: an index on `email` if it's a lookup path.
</details>

### 6. Optional

```java
// A
User user = repository.findById(id).get();
// B
User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
```

<details><summary>Answer</summary>

**B.** `get()` throws a generic `NoSuchElementException` that becomes a 500. B throws a domain exception you can map to 404 and log meaningfully.
</details>

### 7. Exception handling

```java
// A
try { client.send(request); }
catch (Exception e) { log.error("failed"); }
// B
try { client.send(request); }
catch (IOException e) { throw new DeliveryException("Sending " + request.id() + " failed", e); }
```

<details><summary>Answer</summary>

**B.** A swallows every failure (including bugs like NPE) and loses the stack trace; the caller thinks it worked. B catches what it can handle, keeps the cause, and lets the caller decide.
</details>

### 8. Counting in a singleton

```java
// A
private int requestCount;
public void handle() { requestCount++; }
// B
private final AtomicInteger requestCount = new AtomicInteger();
public void handle() { requestCount.incrementAndGet(); }
```

<details><summary>Answer</summary>

**B.** Spring beans are singletons; `++` on an `int` from many threads loses updates. Even better for metrics: a Micrometer counter.
</details>

### 9. Resource handling

```java
// A
BufferedReader reader = new BufferedReader(new FileReader(path));
String line = reader.readLine();
reader.close();
// B
try (BufferedReader reader = Files.newBufferedReader(path)) {
    String line = reader.readLine();
}
```

<details><summary>Answer</summary>

**B.** If `readLine` throws, A never closes the file (leak). try-with-resources always closes, even on exceptions.
</details>

### 10. Lazy initialization

```java
// A
private static Config instance;
public static Config get() {
    if (instance == null) { instance = load(); }
    return instance;
}
// B
private static class Holder { static final Config INSTANCE = load(); }
public static Config get() { return Holder.INSTANCE; }
```

<details><summary>Answer</summary>

**B.** A races: two threads can both load, and without `volatile` another thread may see a partially constructed object. B uses the class-loading guarantee (initialization-on-demand holder). In Spring, just make it a bean.
</details>

### 11. Transaction placement

```java
// A
public void placeOrder(Order o) { save(o); }
@Transactional private void save(Order o) { orders.save(o); stock.reserve(o); }
// B
@Transactional public void placeOrder(Order o) { orders.save(o); stock.reserve(o); }
```

<details><summary>Answer</summary>

**B.** Spring applies `@Transactional` through a proxy: private methods and calls via `this` bypass it, so A runs without a transaction and can save the order without reserving stock. Still check: rollback on checked exceptions needs `rollbackFor`.
</details>

### 12. Retrying a payment

```java
// A
for (int i = 0; i < 3; i++) {
    try { return provider.charge(card, amount); } catch (Exception e) { sleep(500); }
}
// B
String key = payment.id();
return retry.onlyTransient().maxAttempts(3).backoff(exponential())
        .run(() -> provider.charge(card, amount, key));
```

<details><summary>Answer</summary>

**B.** A retries every error (including "card declined") and, without an idempotency key, a lost response leads to a second charge. B retries only transient failures with back-off and sends the same idempotency key every time. (Real code: Resilience4j `Retry` or Spring Retry.)
</details>

### 13. Streams with side effects

```java
// A
List<String> names = new ArrayList<>();
users.parallelStream().forEach(u -> names.add(u.name()));
// B
List<String> names = users.stream().map(User::name).toList();
```

<details><summary>Answer</summary>

**B.** A mutates a non-thread-safe list from parallel threads: lost elements or exceptions, and nondeterministic order. B is correct and clearer; parallel streams rarely help in request handling.
</details>

### 14. Entity in the API

```java
// A
@GetMapping("/users/{id}") public User get(@PathVariable long id) { return repo.findById(id).orElseThrow(); }
// B
@GetMapping("/users/{id}") public UserView get(@PathVariable long id) { return UserView.from(repo.findById(id).orElseThrow()); }
```

<details><summary>Answer</summary>

**B.** Returning the JPA entity exposes every field (password hash, internal flags), couples the API to the schema, and can trigger lazy-loading during serialization. A dedicated response type controls exactly what leaves the service. Still check: authorization (can the caller see this user?).
</details>
