package review.ex04_order_events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Called by the queue listener for every message. The message is acknowledged as soon as this
 * method returns normally; if it throws, the message is redelivered and eventually dead-lettered.
 * So: return normally only when the work is really done, and be safe to run twice.
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
        if (statusStore.isProcessed(event.eventId())) {
            log.info("Skipping duplicate event {}", event.eventId());
            return;
        }
        Optional<OrderState> current = statusStore.find(event.orderId());
        if (current.isPresent() && current.get().version() >= event.version()) {
            log.info("Skipping stale event {} (v{} <= v{})", event.eventId(), event.version(), current.get().version());
            statusStore.markProcessed(event.eventId());
            return;
        }

        if ("PLACED".equals(event.status())) {
            reserveWithRetry(event);
        }
        // Side effects first, then state, then the processed marker. With a database, the last two
        // belong in one transaction so a crash can't record "processed" without the new state.
        statusStore.save(new OrderState(event.orderId(), event.status(), event.version()));
        statusStore.markProcessed(event.eventId());
    }

    private void reserveWithRetry(OrderEvent event) {
        for (int attempt = 1; ; attempt++) {
            try {
                inventory.reserve(event.orderId(), event.quantity());
                return;
            } catch (TransientInventoryException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                log.warn("Reservation failed for order {} (attempt {}): {}", event.orderId(), attempt, e.getMessage());
                pause(1000L * attempt);
            }
        }
    }

    private void pause(long millis) {
        try {
            sleeper.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry", e);
        }
    }
}
