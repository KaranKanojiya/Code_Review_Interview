package review.ex06_pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CartPricerTest {

    /** The single failing test you're handed in the Amazon-style prompt. Find every root cause. */
    @Test
    void typicalCartsArePricedCorrectly() {
        CountingPriceSource source = new CountingPriceSource(Map.of(
                "SKU-1", "19.99", "SKU-2", "10.02", "SKU-3", "99.99"));
        CartPricer pricer = new CartPricer(new PriceCache(source), new DiscountPolicy());

        CartTotal atThreshold = pricer.price(List.of(
                new CartLine(new ProductKey("SKU-1", "EU"), 1),
                new CartLine(new ProductKey("SKU-1", "EU"), 1),
                new CartLine(new ProductKey("SKU-2", "EU"), 1)), 0);
        assertThat(atThreshold.total()).as("50.00 exactly ships free").isEqualByComparingTo("50.00");
        assertThat(source.calls).as("SKU-1 priced once, then cached").isEqualTo(2);

        CartTotal discounted = pricer.price(List.of(new CartLine(new ProductKey("SKU-3", "EU"), 1)), 15);
        assertThat(discounted.total()).as("15% off 99.99").isEqualByComparingTo("84.99");
    }

    @Test
    void equalKeysHaveEqualHashCodes() {
        assertThat(new ProductKey("SKU-1", "EU")).hasSameHashCodeAs(new ProductKey("SKU-1", "EU"));
    }

    @Test
    void discountKeepsTheCents() {
        assertThat(new DiscountPolicy().apply(new BigDecimal("99.99"), 15)).isEqualByComparingTo("84.99");
        assertThat(new DiscountPolicy().apply(new BigDecimal("10.00"), 0)).isEqualByComparingTo("10.00");
    }

    @Test
    void orderOfExactlyTheThresholdShipsFree() {
        CartPricer pricer = new CartPricer(new PriceCache(new CountingPriceSource(Map.of("SKU-1", "50.00"))), new DiscountPolicy());

        CartTotal total = pricer.price(List.of(new CartLine(new ProductKey("SKU-1", "EU"), 1)), 0);

        assertThat(total.shipping()).isEqualByComparingTo("0");
    }

    static final class CountingPriceSource implements PriceSource {
        private final Map<String, BigDecimal> prices = new HashMap<>();
        int calls;

        CountingPriceSource(Map<String, String> pricesBySku) {
            pricesBySku.forEach((sku, price) -> prices.put(sku, new BigDecimal(price)));
        }

        @Override
        public BigDecimal priceOf(ProductKey key) {
            calls++;
            return prices.get(key.getSku());
        }
    }
}
