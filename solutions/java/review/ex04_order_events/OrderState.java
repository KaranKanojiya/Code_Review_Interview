package review.ex04_order_events;

public record OrderState(String orderId, String status, long version) {
}
