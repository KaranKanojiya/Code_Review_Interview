package review.ex04_order_events;

public interface InventoryClient {

    /**
     * @throws TransientInventoryException on timeouts or 5xx: worth retrying
     * @throws InvalidOrderException       when the order can never be reserved (unknown SKU, bad quantity)
     */
    void reserve(String orderId, int quantity);
}
