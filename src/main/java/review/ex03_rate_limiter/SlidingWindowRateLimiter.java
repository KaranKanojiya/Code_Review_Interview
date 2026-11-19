package review.ex03_rate_limiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows each client at most {@code maxRequests} requests in any rolling {@code window}.
 * Used by the API gateway filter for every incoming request.
 */
public class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final Map<String, List<Instant>> requests = new HashMap<>();

    public SlidingWindowRateLimiter(int maxRequests, Duration window, Clock clock) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
    }

    public boolean tryAcquire(String clientId) {
        Instant now = clock.instant();
        Instant cutoff = now.minus(window);

        List<Instant> timestamps = requests.get(clientId);
        if (timestamps == null) {
            timestamps = new ArrayList<>();
            requests.put(clientId, timestamps);
        }

        while (!timestamps.isEmpty() && timestamps.get(0).isBefore(cutoff)) {
            timestamps.remove(0);
        }

        if (timestamps.size() > maxRequests) {
            return false;
        }
        timestamps.add(now);
        return true;
    }

    public int trackedClients() {
        return requests.size();
    }
}
