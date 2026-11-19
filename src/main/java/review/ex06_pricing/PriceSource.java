package review.ex06_pricing;

import java.math.BigDecimal;

/** Remote pricing service: slow and rate-limited, hence the cache. */
public interface PriceSource {

    BigDecimal priceOf(ProductKey key);
}
