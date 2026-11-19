package review.ex01_transfers;

import java.math.BigDecimal;
import java.util.Objects;

/** Money is BigDecimal end to end; never double. */
public class Account {

    private final String id;
    private BigDecimal balance;

    public Account(String id, BigDecimal openingBalance) {
        this.id = Objects.requireNonNull(id);
        this.balance = Objects.requireNonNull(openingBalance);
    }

    public String getId() {
        return id;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public synchronized void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public synchronized void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
