package review.ex01_transfers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** toString is overridden so account numbers never reach logs in full. */
public record TransferRequest(
        @NotBlank String fromAccount,
        @NotBlank String toAccount,
        @NotNull @Positive BigDecimal amount) {

    @Override
    public String toString() {
        return "TransferRequest[from=" + mask(fromAccount) + ", to=" + mask(toAccount) + "]";
    }

    private static String mask(String account) {
        if (account == null || account.length() <= 4) {
            return "****";
        }
        return "****" + account.substring(account.length() - 4);
    }
}
