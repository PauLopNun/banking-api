package com.gft.banking.infrastructure.persistence;

import com.gft.banking.domain.model.Account;
import com.gft.banking.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOwner(User owner);
    boolean existsByOwnerNameAndOwner(String ownerName, User owner);
    Optional<Account> findByIdAndOwner(Long id, User owner);
}