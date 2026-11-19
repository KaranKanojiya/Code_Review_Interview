package review.ex04_order_events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Called by the queue listener for every message. The message is acknowledged (deleted from the
 * queue) as soon as this method returns normally; if it throws, the message is redelivered and
 * eventually moved to the dead-letter queue.
 */
@Component
public class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);
    private static final int MAX_ATTEMPTS = 3;

    private final InventoryClient inventory;
    private final OrderStatusStore statusStore;
    private final Sleeper sleeper;

    public OrderEventHandler(InventoryClient inventory, OrderStatusStore statusStore, Sleeper sleeper) {
        this.inventory = inventory;
        this.statusStore = statusStore;
        this.sleeper = sleeper;
    }

    public void handle(OrderEvent event) {
        log.info("Handling event {} for order {}", event.eventId(), event.orderId());
        statusStore.save(new OrderState(event.orderId(), event.status(), event.version()));

        if ("PLACED".equals(event.status())) {
            reserveWithRetry(event);
        }
    }

    private void reserveWithRetry(OrderEvent event) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                inventory.reserve(event.orderId(), event.quantity());
                return;
            } catch (Exception e) {
                log.warn("Reservation failed for order {} (attempt {})", event.orderId(), attempt);
                try {
                    sleeper.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    // ignore and try again
                }
            }
        }
        log.error("Giving up on order {}", event.orderId());
    }
}
