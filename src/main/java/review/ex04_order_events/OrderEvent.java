package review.ex04_order_events;

/** Message from the orders queue. {@code version} increases with every change to the order. */
public record OrderEvent(String eventId, String orderId, long version, String status, int quantity) {
}
