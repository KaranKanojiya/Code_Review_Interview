package review.ex06_pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiscountPolicy {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Applies a percentage discount, e.g. 15 means 15% off. Result rounded half-up to cents. */
    public BigDecimal apply(BigDecimal amount, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("percent must be between 0 and 100");
        }
        return amount.multiply(BigDecimal.valueOf(100 - percent)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
