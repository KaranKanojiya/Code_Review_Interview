package review.ex01_transfers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accounts;
    private final AtomicInteger transfersProcessed = new AtomicInteger();

    public TransferService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    /**
     * Validates everything and loads both accounts BEFORE changing anything, so a failure can't
     * leave money debited but not credited. Errors are thrown, not swallowed: the caller decides
     * the HTTP status.
     */
    @Transactional
    public TransferReceipt transfer(TransferRequest request) {
        validate(request);
        Account from = find(request.fromAccount());
        Account to = find(request.toAccount());

        // Lock in a fixed order (by id) so two opposite transfers can't deadlock. With a database
        // this becomes SELECT ... FOR UPDATE in id order, or optimistic locking with @Version.
        Account first = from.getId().compareTo(to.getId()) < 0 ? from : to;
        Account second = first == from ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (from.getBalance().compareTo(request.amount()) < 0) {
                    throw new InsufficientFundsException(from.getId());
                }
                from.debit(request.amount());
                to.credit(request.amount());
                accounts.save(from);
                accounts.save(to);
            }
        }

        transfersProcessed.incrementAndGet();
        TransferReceipt receipt = new TransferReceipt(UUID.randomUUID().toString(), from.getId(), to.getId(),
                request.amount(), Instant.now());
        log.info("Transfer {} completed: {}", receipt.transferId(), request);
        return receipt;
    }

    public int getTransfersProcessed() {
        return transfersProcessed.get();
    }

    private Account find(String id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private static void validate(TransferRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (request.fromAccount() == null || request.fromAccount().equals(request.toAccount())) {
            throw new IllegalArgumentException("Source and target accounts must be different");
        }
    }
}
