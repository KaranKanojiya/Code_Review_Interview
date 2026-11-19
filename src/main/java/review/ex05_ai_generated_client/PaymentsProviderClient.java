package review.ex05_ai_generated_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client for the payments provider REST API.
 *
 * <p>Generated with an AI coding assistant from the ticket: "Call the provider to charge the
 * customer's card. The provider is sometimes flaky, so retry on failure."
 */
@Component
public class PaymentsProviderClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentsProviderClient.class);
    private static final int MAX_RETRIES = 3;

    private final URI baseUri;
    private final String apiKey;
    private final Transport transport;
    private final Sleeper sleeper;

    public PaymentsProviderClient(@Value("${payments.base-uri}") URI baseUri,
                                  @Value("${payments.api-key:changeme-default-key}") String apiKey) {
        this(baseUri, apiKey, defaultTransport(), Thread::sleep);
    }

    public PaymentsProviderClient(URI baseUri, String apiKey, Transport transport, Sleeper sleeper) {
        this.baseUri = baseUri;
        this.apiKey = apiKey;
        this.transport = transport;
        this.sleeper = sleeper;
    }

    private static Transport defaultTransport() {
        HttpClient client = HttpClient.newHttpClient();
        return request -> {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Transport.Response(response.statusCode(), response.body());
        };
    }

    public HttpRequest buildChargeRequest(ChargeRequest charge) {
        String body = "{\"customer\":\"" + charge.customerId() + "\",\"amount\":" + charge.amount()
                + ",\"currency\":\"" + charge.currency() + "\"}";
        return HttpRequest.newBuilder(baseUri.resolve("/v1/charges"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public ChargeResult charge(ChargeRequest charge) {
        log.info("Charging {} with API key {}", charge, apiKey);
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                Transport.Response response = transport.send(buildChargeRequest(charge));
                if (response.status() == 200) {
                    return parse(response.body());
                }
                log.warn("Provider returned status {}", response.status());
            } catch (Exception e) {
                log.warn("Charge attempt {} failed", attempt, e);
            }
            try {
                sleeper.sleep(500);
            } catch (InterruptedException e) {
                log.warn("Retry sleep interrupted");
            }
        }
        return null;
    }

    private ChargeResult parse(String body) {
        String[] parts = body.split("\"");
        return new ChargeResult(parts[3], parts[7]);
    }
}
