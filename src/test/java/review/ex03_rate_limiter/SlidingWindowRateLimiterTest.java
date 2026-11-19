package review.ex03_rate_limiter;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlidingWindowRateLimiterTest {

    /** The "given" failing test in the Karat-style prompt: start here. */
    @Test
    void allowsExactlyMaxRequestsPerWindow() {
        MutableClock clock = new MutableClock();
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, Duration.ofSeconds(1), clock);

        assertThat(limiter.tryAcquire("c1")).isTrue();
        assertThat(limiter.tryAcquire("c1")).isTrue();
        assertThat(limiter.tryAcquire("c1")).isTrue();
        assertThat(limiter.tryAcquire("c1")).as("4th request in the same window").isFalse();
    }

    @Test
    void requestsExpireExactlyWhenTheWindowHasPassed() {
        MutableClock clock = new MutableClock();
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1, Duration.ofSeconds(1), clock);

        assertThat(limiter.tryAcquire("c1")).isTrue();
        clock.advance(Duration.ofMillis(999));
        assertThat(limiter.tryAcquire("c1")).isFalse();
        clock.advance(Duration.ofMillis(1));
        assertThat(limiter.tryAcquire("c1")).as("exactly one window later").isTrue();
    }

    @Test
    void clientsAreLimitedIndependently() {
        MutableClock clock = new MutableClock();
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1, Duration.ofSeconds(1), clock);

        assertThat(limiter.tryAcquire("c1")).isTrue();
        assertThat(limiter.tryAcquire("c2")).isTrue();
    }

    @Test
    void invalidConfigurationIsRejected() {
        Clock clock = new MutableClock();
        assertThatThrownBy(() -> new SlidingWindowRateLimiter(0, Duration.ofSeconds(1), clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlidingWindowRateLimiter(5, Duration.ZERO, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /** May pass by luck on the buggy version; the thread-safety finding stands either way. */
    @Test
    void concurrentRequestsNeverExceedTheLimit() throws Exception {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(100, Duration.ofHours(1), new MutableClock());
        AtomicInteger allowed = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int t = 0; t < 8; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < 1_000; i++) {
                    if (limiter.tryAcquire("shared")) {
                        allowed.incrementAndGet();
                    }
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        assertThat(allowed.get()).isEqualTo(100);
    }

    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
