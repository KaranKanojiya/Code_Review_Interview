package review.ex03_rate_limiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows each client at most {@code maxRequests} requests in any rolling {@code window}.
 * A request made at time t counts during [t, t + window).
 *
 * <p>Still open (would raise in review, not fix here): idle clients are never evicted, so memory
 * grows with the number of distinct client ids; in production use a cache with
 * expire-after-access (e.g. Caffeine) or a shared store (Redis) when there are several instances.
 */
public class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requests = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequests, Duration window, Clock clock) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean tryAcquire(String clientId) {
        Objects.requireNonNull(clientId, "clientId");
        Deque<Instant> timestamps = requests.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (timestamps) {
            Instant now = clock.instant();
            Instant cutoff = now.minus(window);
            while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(cutoff)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    public int trackedClients() {
        return requests.size();
    }
}
