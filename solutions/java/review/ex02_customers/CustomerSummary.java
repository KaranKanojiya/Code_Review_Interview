package review.ex02_customers;

/** API response: a deliberate subset of the stored customer. Never the password hash. */
public record CustomerSummary(long id, String name, String email, int orderCount) {
}
