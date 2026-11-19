# Exercise 2 — "Customer search and sign-up API" (security-focused PR)

**Format:** a REST controller plus a JDBC repository. Common in bank and fintech review rounds: security findings are what interviewers listen for.
**Time box:** 20 minutes + 5 minutes verdict.
**Files:** [`src/main/java/review/ex02_customers/`](../src/main/java/review/ex02_customers)

## The PR, as the author wrote it

> **CUST-77: Customer search and create endpoints**
>
> - `GET /customers?name=al&limit=50` searches by name and shows how many orders each customer has
> - `POST /customers` creates a customer
> - Used plain JdbcTemplate because JPA felt heavy for two queries
> - `PasswordEncoder` bean is injected for later use
>
> Works against the dev database.

## Your task

Same steps as exercise 1. Run `./gradlew practice -Pex=ex02`. Model answer: [solutions/answers/ex02-customers.md](../solutions/answers/ex02-customers.md).

<details><summary>Hint</summary>

Think like an attacker sending the `name` parameter, then like a DBA watching the query log, then like a security auditor reading the JSON response.
</details>
