package review.ex05_ai_generated_client;

import java.io.IOException;
import java.net.http.HttpRequest;

/** Thin seam over the HTTP client so the retry logic can be tested without a network. */
@FunctionalInterface
public interface Transport {

    Response send(HttpRequest request) throws IOException, InterruptedException;

    record Response(int status, String body) {
    }
}
