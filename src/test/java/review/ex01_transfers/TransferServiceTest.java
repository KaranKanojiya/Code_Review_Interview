package review.ex01_transfers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Each test pins down one review finding. Red against src/main, green against solutions/. */
class TransferServiceTest {

    private InMemoryAccountRepository accounts;
    private TransferService service;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryAccountRepository();
        service = new TransferService(accounts);
    }

    @Test
    void moneyArithmeticIsExact() {
        open("A-1001", "0.30");
        open("B-2002", "0.00");

        for (int i = 0; i < 3; i++) {
            service.transfer(new TransferRequest("A-1001", "B-2002", new BigDecimal("0.10")));
        }

        assertThat(balance("A-1001")).isEqualByComparingTo("0.00");
        assertThat(balance("B-2002")).isEqualByComparingTo("0.30");
    }

    @Test
    void canTransferTheWholeBalance() {
        open("A-1001", "100.00");
        open("B-2002", "0.00");

        service.transfer(new TransferRequest("A-1001", "B-2002", new BigDecimal("100.00")));

        assertThat(balance("A-1001")).isEqualByComparingTo("0.00");
    }

    @Test
    void negativeAmountIsRejected() {
        open("A-1001", "100.00");
        open("B-2002", "100.00");

        assertThatThrownBy(() -> service.transfer(new TransferRequest("A-1001", "B-2002", new BigDecimal("-50"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(balance("B-2002")).isEqualByComparingTo("100.00");
    }

    @Test
    void transferToTheSameAccountIsRejected() {
        open("A-1001", "100.00");

        assertThatThrownBy(() -> service.transfer(new TransferRequest("A-1001", "A-1001", BigDecimal.TEN)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownTargetLeavesTheSourceUntouched() {
        open("A-1001", "100.00");

        assertThatThrownBy(() -> service.transfer(new TransferRequest("A-1001", "NOPE", BigDecimal.TEN)))
                .isInstanceOf(AccountNotFoundException.class);
        assertThat(balance("A-1001")).isEqualByComparingTo("100.00");
    }

    @Test
    void failuresAreReportedNotSwallowed() {
        open("A-1001", "5.00");
        open("B-2002", "0.00");

        assertThatThrownBy(() -> service.transfer(new TransferRequest("A-1001", "B-2002", BigDecimal.TEN)))
                .isInstanceOf(InsufficientFundsException.class);
    }

    private void open(String id, String balance) {
        accounts.save(new Account(id, new BigDecimal(balance)));
    }

    private BigDecimal balance(String id) {
        return accounts.findById(id).orElseThrow().getBalance();
    }
}
