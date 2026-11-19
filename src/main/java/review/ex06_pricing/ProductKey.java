package review.ex06_pricing;

public final class ProductKey {

    private final String sku;
    private final String region;

    public ProductKey(String sku, String region) {
        this.sku = sku;
        this.region = region;
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
    public String toString() {
        return sku + "@" + region;
    }
}
