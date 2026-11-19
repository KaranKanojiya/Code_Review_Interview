package review.ex01_transfers;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountId) {
        super("Insufficient funds in account " + accountId);
    }
}
