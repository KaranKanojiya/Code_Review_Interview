package review.ex02_customers;

public record CustomerSummary(long id, String name, String email, String passwordHash, int orderCount) {
}
