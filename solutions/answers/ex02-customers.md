# Model answer — Exercise 2: customer search and sign-up

**Verdict: Request changes (security blockers).** Fixed code: [`solutions/java/review/ex02_customers/`](../java/review/ex02_customers).

| # | Severity | Where | Finding | Test |
|---|---|---|---|---|
| 1 | Blocker | `CustomerRepository.searchByName` | **SQL injection**: `name` concatenated into SQL. `x' OR 1=1 --` returns every customer and comments out the LIMIT. Use bind parameters (`?`) and escape LIKE wildcards | `searchIsNotVulnerableToSqlInjection` |
| 2 | Blocker | `create` | **Plaintext password stored** in `password_hash`: the injected `PasswordEncoder` is never used. Encode with BCrypt/Argon2 | `createReturns201WithLocationAndStoresAHash` |
| 3 | Blocker | `CustomerSummary` | Response DTO includes `passwordHash` (and `create` echoes the plaintext password back). Never expose credentials; build response DTOs field by field | `responseNeverContainsPasswordData` |
| 4 | Major | `search` | **Unbounded page size**: `limit` defaults to 1000 and is not capped; `limit=100000` dumps the table. Cap (e.g. 100) and paginate | `pageSizeIsCapped` |
| 5 | Major | `search` | Blank `name` matches everything (`LIKE '%%'`): validate input | `blankSearchIsRejected` |
| 6 | Major | `search` loop | **N+1**: one `countOrders` query per customer. One grouped query with `IN (:ids)` | (explain, or count queries) |
| 7 | Major | `countOrders` | Also concatenates SQL (a `long`, so not exploitable today, but the pattern gets copied). Find Security Bugs flags both queries | — |
| 8 | Minor | `create` | Returns 200; should be **201 Created** with `Location` | same test as #2 |
| 9 | Minor | `NewCustomer` | No validation (`@Email`, password length) | — |
| 10 | Minor | `searchByName` | No `ORDER BY` with `LIMIT`: results nondeterministic | — |

## How to say #1

"Blocking, security: `searchByName` builds SQL by concatenating `name`. Anyone can send `x' OR 1=1 --` and read every customer, or worse depending on the database user's permissions. JdbcTemplate supports bind parameters: `WHERE name LIKE ?` with `"%" + name + "%"` as an argument. I'd also escape `%` and `_` in user input."

## Questions for the author

- Who calls this endpoint and is it authenticated? Should customers be able to search other customers at all?
- Is there a uniqueness rule on email? (Missing in the PR.)

## Praise

- Using `PreparedStatement` with generated keys in `insert` is right; the same approach just needs to be used everywhere.

## What the tools caught

Find Security Bugs: both SQL injections (`SQL_INJECTION_SPRING_JDBC`). SpotBugs: a possible null from `getKey()`, plus two `EI_EXPOSE_REP2` false positives on constructor injection. **Not caught:** plaintext password, password in the response, unbounded limit, N+1, blank-search behavior, wrong status code.
