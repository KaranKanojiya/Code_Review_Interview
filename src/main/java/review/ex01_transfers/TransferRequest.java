package review.ex01_transfers;

import java.math.BigDecimal;

public record TransferRequest(String fromAccount, String toAccount, BigDecimal amount) {
}
