package review.ex06_pricing;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PriceCache {

    private final PriceSource source;
    private final Map<ProductKey, BigDecimal> prices = new ConcurrentHashMap<>();

    public PriceCache(PriceSource source) {
        this.source = source;
    }

    public BigDecimal price(ProductKey key) {
        return prices.computeIfAbsent(key, source::priceOf);
    }
}
