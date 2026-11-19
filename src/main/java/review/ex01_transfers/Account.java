package review.ex01_transfers;

import java.math.BigDecimal;

public class Account {

    private final String id;
    private double balance;

    public Account(String id, BigDecimal openingBalance) {
        this.id = id;
        this.balance = openingBalance.doubleValue();
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return BigDecimal.valueOf(balance);
    }

    public void debit(BigDecimal amount) {
        balance -= amount.doubleValue();
    }

    public void credit(BigDecimal amount) {
        balance += amount.doubleValue();
    }
}
