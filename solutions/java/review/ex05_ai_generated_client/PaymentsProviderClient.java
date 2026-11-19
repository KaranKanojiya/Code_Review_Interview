package review.ex05_ai_generated_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for the payments provider REST API.
 *
 * <p>Charging money is not idempotent by itself, so every attempt for the same payment carries the
 * same {@code Idempotency-Key}: if a retry reaches the provider after an earlier attempt actually
 * succeeded, the provider returns the original charge instead of charging twice.
 */
@Component
public class PaymentsProviderClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentsProviderClient.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUri;
    private final String apiKey;
    private final Transport transport;
    private final Sleeper sleeper;

    @Autowired
    public PaymentsProviderClient(@Value("${payments.base-uri}") URI baseUri, @Value("${payments.api-key}") String apiKey) {
        this(baseUri, apiKey, defaultTransport(), Thread::sleep);
    }

    public PaymentsProviderClient(URI baseUri, String apiKey, Transport transport, Sleeper sleeper) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("payments.api-key must be configured");
        }
        this.baseUri = baseUri;
        this.apiKey = apiKey;
        this.transport = transport;
        this.sleeper = sleeper;
    }

    private static Transport defaultTransport() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        return request -> {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Transport.Response(response.statusCode(), response.body());
        };
    }

    public HttpRequest buildChargeRequest(ChargeRequest charge) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customer", charge.customerId());
        body.put("amount", charge.amount());
        body.put("currency", charge.currency());
        return HttpRequest.newBuilder(baseUri.resolve("/v1/charges"))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", charge.paymentId())
                .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)))
                .build();
    }

    public ChargeResult charge(ChargeRequest charge) {
        log.info("Charging {}", charge);
        for (int attempt = 1; ; attempt++) {
            Transport.Response response;
            try {
                response = transport.send(buildChargeRequest(charge));
            } catch (IOException e) {
                failOrWait(attempt, "I/O error: " + e.getMessage(), e);
                continue;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PaymentProviderException("Interrupted while charging " + charge.paymentId(), e);
            }

            int status = response.status();
            if (status >= 200 && status < 300) {
                return parse(response.body());
            }
            if (status >= 400 && status < 500 && status != 429) {
                throw new PaymentProviderException("Charge rejected with status " + status);
            }
            failOrWait(attempt, "status " + status, null);
        }
    }

    private void failOrWait(int attempt, String reason, Exception cause) {
        if (attempt >= MAX_ATTEMPTS) {
            throw new PaymentProviderException("Charge failed after " + attempt + " attempts: " + reason, cause);
        }
        log.warn("Charge attempt {} failed ({}); retrying", attempt, reason);
        try {
            sleeper.sleep(200L * (1L << attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentProviderException("Interrupted while waiting to retry", e);
        }
    }

    private static ChargeResult parse(String body) {
        JsonNode json = JSON.readTree(body);
        return new ChargeResult(json.path("id").asString(), json.path("status").asString());
    }
}
