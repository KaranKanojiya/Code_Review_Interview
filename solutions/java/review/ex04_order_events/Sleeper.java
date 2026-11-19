package review.ex04_order_events;

@FunctionalInterface
public interface Sleeper {

    void sleep(long millis) throws InterruptedException;
}
