package account.admin;

import account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findAccountByEmailIgnoreCase(String email);

    List<Account> getAllByOrderByIdAsc();

    Account findFirstByOrderByIdAsc();

}
