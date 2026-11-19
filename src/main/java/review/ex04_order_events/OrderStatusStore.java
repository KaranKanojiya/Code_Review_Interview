package review.ex04_order_events;

import java.util.Optional;

public interface OrderStatusStore {

    Optional<OrderState> find(String orderId);

    void save(OrderState state);

    boolean isProcessed(String eventId);

    void markProcessed(String eventId);
}
