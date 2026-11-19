package review.ex06_pricing;

import java.math.BigDecimal;

public class DiscountPolicy {

    /** Applies a percentage discount, e.g. 15 means 15% off. */
    public BigDecimal apply(BigDecimal amount, int percent) {
        int discounted = amount.intValue() * (100 - percent) / 100;
        return BigDecimal.valueOf(discounted);
    }
}
