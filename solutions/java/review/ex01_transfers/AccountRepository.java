package review.ex01_transfers;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(String id);

    void save(Account account);
}
