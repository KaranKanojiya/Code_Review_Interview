package review.ex04_order_events;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderEventHandlerTest {

    private InMemoryOrderStatusStore store;
    private CountingInventory inventory;

    @BeforeEach
    void setUp() {
        store = new InMemoryOrderStatusStore();
        inventory = new CountingInventory();
    }

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void duplicateDeliveryReservesInventoryOnce() {
        OrderEventHandler handler = new OrderEventHandler(inventory, store, millis -> { });
        OrderEvent placed = new OrderEvent("evt-1", "order-1", 1, "PLACED", 2);

        handler.handle(placed);
        handler.handle(placed);

        assertThat(inventory.calls).isEqualTo(1);
    }

    @Test
    void staleEventDoesNotOverwriteNewerState() {
        OrderEventHandler handler = new OrderEventHandler(inventory, store, millis -> { });

        handler.handle(new OrderEvent("evt-2", "order-1", 2, "SHIPPED", 2));
        handler.handle(new OrderEvent("evt-1", "order-1", 1, "PLACED", 2));

        assertThat(store.find("order-1").orElseThrow().status()).isEqualTo("SHIPPED");
        assertThat(inventory.calls).isZero();
    }

    @Test
    void permanentErrorIsNotRetried() {
        inventory.failure = () -> new InvalidOrderException("unknown SKU");
        OrderEventHandler handler = new OrderEventHandler(inventory, store, millis -> { });

        assertThatThrownBy(() -> handler.handle(new OrderEvent("evt-1", "order-1", 1, "PLACED", 2)))
                .isInstanceOf(InvalidOrderException.class);
        assertThat(inventory.calls).isEqualTo(1);
    }

    @Test
    void exhaustedRetriesAreReportedSoTheMessageIsRedelivered() {
        inventory.failure = () -> new TransientInventoryException("timeout");
        OrderEventHandler handler = new OrderEventHandler(inventory, store, millis -> { });

        assertThatThrownBy(() -> handler.handle(new OrderEvent("evt-1", "order-1", 1, "PLACED", 2)))
                .isInstanceOf(TransientInventoryException.class);
        assertThat(inventory.calls).isEqualTo(3);
        assertThat(store.isProcessed("evt-1")).isFalse();
        assertThat(store.find("order-1")).as("no PLACED state without a reservation").isEmpty();
    }

    @Test
    void interruptIsPreservedAndStopsRetrying() {
        inventory.failure = () -> new TransientInventoryException("timeout");
        OrderEventHandler handler = new OrderEventHandler(inventory, store, millis -> {
            throw new InterruptedException("shutdown");
        });

        assertThatThrownBy(() -> handler.handle(new OrderEvent("evt-1", "order-1", 1, "PLACED", 2)))
                .isInstanceOf(RuntimeException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        assertThat(inventory.calls).isEqualTo(1);
    }

    static final class CountingInventory implements InventoryClient {
        int calls;
        Supplier<RuntimeException> failure;

        @Override
        public void reserve(String orderId, int quantity) {
            calls++;
            if (failure != null) {
                throw failure.get();
            }
        }
    }
}
