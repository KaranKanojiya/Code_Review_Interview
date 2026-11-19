package review.ex05_ai_generated_client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentsProviderClientTest {

    private static final URI BASE = URI.create("https://payments.example.test");
    private static final ChargeRequest CHARGE = new ChargeRequest("pay-123", "cus-9", new BigDecimal("49.99"), "EUR");
    private static final String OK_BODY = "{\"id\":\"ch_1\",\"status\":\"succeeded\"}";

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void everyRequestHasATimeout() {
        HttpRequest request = client(new FakeTransport()).buildChargeRequest(CHARGE);
        assertThat(request.timeout()).isPresent();
    }

    @Test
    void retriesOfTheSamePaymentCarryTheSameIdempotencyKey() {
        PaymentsProviderClient client = client(new FakeTransport());
        HttpRequest first = client.buildChargeRequest(CHARGE);
        HttpRequest retry = client.buildChargeRequest(CHARGE);

        assertThat(first.headers().firstValue("Idempotency-Key")).isPresent();
        assertThat(retry.headers().firstValue("Idempotency-Key")).isEqualTo(first.headers().firstValue("Idempotency-Key"));
    }

    @Test
    void anySuccessStatusIsASuccessAndIsNotRetried() {
        FakeTransport transport = new FakeTransport().respond(201, OK_BODY);

        ChargeResult result = client(transport).charge(CHARGE);

        assertThat(result).isEqualTo(new ChargeResult("ch_1", "succeeded"));
        assertThat(transport.calls).as("a retried POST would charge the customer twice").hasSize(1);
    }

    @Test
    void clientErrorsAreNotRetried() {
        FakeTransport transport = new FakeTransport().respond(400, "{\"error\":\"card_declined\"}");

        assertThatThrownBy(() -> client(transport).charge(CHARGE)).isInstanceOf(PaymentProviderException.class);
        assertThat(transport.calls).hasSize(1);
    }

    @Test
    void exhaustedServerErrorsFailLoudlyInsteadOfReturningNull() {
        FakeTransport transport = new FakeTransport().respond(503, "").respond(503, "").respond(503, "");

        assertThatThrownBy(() -> client(transport).charge(CHARGE)).isInstanceOf(PaymentProviderException.class);
        assertThat(transport.calls).hasSize(3);
    }

    @Test
    void interruptStopsRetryingAndIsPreserved() {
        FakeTransport transport = new FakeTransport().respond(503, "").respond(503, "").respond(503, "");
        PaymentsProviderClient client = new PaymentsProviderClient(BASE, "test-key", transport, millis -> {
            throw new InterruptedException("shutdown");
        });

        assertThatThrownBy(() -> client.charge(CHARGE)).isInstanceOf(RuntimeException.class);
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        assertThat(transport.calls).hasSize(1);
    }

    private static PaymentsProviderClient client(Transport transport) {
        return new PaymentsProviderClient(BASE, "test-key", transport, millis -> { });
    }

    static final class FakeTransport implements Transport {
        final List<HttpRequest> calls = new ArrayList<>();
        private final Deque<Response> responses = new ArrayDeque<>();

        FakeTransport respond(int status, String body) {
            responses.add(new Response(status, body));
            return this;
        }

        @Override
        public Response send(HttpRequest request) {
            calls.add(request);
            return responses.isEmpty() ? new Response(500, "") : responses.poll();
        }
    }
}
