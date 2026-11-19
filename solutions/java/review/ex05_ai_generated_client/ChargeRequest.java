package review.ex05_ai_generated_client;

import java.math.BigDecimal;

/** {@code paymentId} is our own unique id for this payment attempt; it doubles as the idempotency key. */
public record ChargeRequest(String paymentId, String customerId, BigDecimal amount, String currency) {

    @Override
    public String toString() {
        return "ChargeRequest[paymentId=" + paymentId + ", amount=" + amount + " " + currency + "]";
    }
}
