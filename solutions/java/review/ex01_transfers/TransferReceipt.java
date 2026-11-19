package review.ex01_transfers;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferReceipt(String transferId, String fromAccount, String toAccount, BigDecimal amount, Instant completedAt) {
}
