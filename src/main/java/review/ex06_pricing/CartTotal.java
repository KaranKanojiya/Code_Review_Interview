package review.ex06_pricing;

import java.math.BigDecimal;

public record CartTotal(BigDecimal subtotal, BigDecimal discounted, BigDecimal shipping, BigDecimal total) {
}
