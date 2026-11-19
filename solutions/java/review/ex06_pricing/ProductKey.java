package review.ex06_pricing;

import java.util.Objects;

/** Used as a HashMap key: equals and hashCode must use the same fields. (A record would do this for free.) */
public final class ProductKey {

    private final String sku;
    private final String region;

    public ProductKey(String sku, String region) {
        this.sku = Objects.requireNonNull(sku);
        this.region = Objects.requireNonNull(region);
    }

    public String getSku() {
        return sku;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductKey other)) {
            return false;
        }
        return sku.equals(other.sku) && region.equals(other.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, region);
    }

    @Override
    public String toString() {
        return sku + "@" + region;
    }
}
