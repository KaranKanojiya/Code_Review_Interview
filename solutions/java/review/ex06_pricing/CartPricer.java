package review.ex06_pricing;

import java.math.BigDecimal;
import java.util.List;

/** Requirement: orders of 50.00 or more (after discount) ship free; otherwise shipping is 4.99. */
public class CartPricer {

    static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    static final BigDecimal SHIPPING_FEE = new BigDecimal("4.99");

    private final PriceCache prices;
    private final DiscountPolicy discounts;

    public CartPricer(PriceCache prices, DiscountPolicy discounts) {
        this.prices = prices;
        this.discounts = discounts;
    }

    public CartTotal price(List<CartLine> lines, int discountPercent) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartLine line : lines) {
            if (line.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive for " + line.product());
            }
            subtotal = subtotal.add(prices.price(line.product()).multiply(BigDecimal.valueOf(line.quantity())));
        }
        BigDecimal discounted = discounts.apply(subtotal, discountPercent);
        BigDecimal shipping = discounted.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : SHIPPING_FEE;
        return new CartTotal(subtotal, discounted, shipping, discounted.add(shipping));
    }
}
