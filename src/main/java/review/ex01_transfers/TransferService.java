package review.ex01_transfers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accounts;
    private int transfersProcessed;

    public TransferService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public TransferReceipt transfer(TransferRequest request) {
        log.info("Processing transfer request: {}", request);
        try {
            return doTransfer(request);
        } catch (Exception e) {
            log.error("Transfer failed");
            return null;
        }
    }

    @Transactional
    private TransferReceipt doTransfer(TransferRequest request) {
        Account from = accounts.findById(request.fromAccount())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccount()));

        if (from.getBalance().compareTo(request.amount()) <= 0) {
            throw new InsufficientFundsException(request.fromAccount());
        }
        from.debit(request.amount());
        accounts.save(from);

        Account to = accounts.findById(request.toAccount())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccount()));
        to.credit(request.amount());
        accounts.save(to);

        transfersProcessed++;
        return new TransferReceipt(UUID.randomUUID().toString(), request.fromAccount(), request.toAccount(),
                request.amount(), Instant.now());
    }

    public int getTransfersProcessed() {
        return transfersProcessed;
    }
}
