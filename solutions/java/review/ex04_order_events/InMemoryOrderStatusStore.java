package review.ex04_order_events;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOrderStatusStore implements OrderStatusStore {

    private final Map<String, OrderState> states = new ConcurrentHashMap<>();
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    @Override
    public Optional<OrderState> find(String orderId) {
        return Optional.ofNullable(states.get(orderId));
    }

    @Override
    public void save(OrderState state) {
        states.put(state.orderId(), state);
    }

    @Override
    public boolean isProcessed(String eventId) {
        return processed.contains(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        processed.add(eventId);
    }
}
