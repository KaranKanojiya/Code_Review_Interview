package review.ex04_order_events;

public class TransientInventoryException extends RuntimeException {

    public TransientInventoryException(String message) {
        super(message);
    }
}
