package com.gft.banking.infrastructure.persistence;

import com.gft.banking.domain.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("SELECT t FROM Transfer t " +
            "JOIN FETCH t.fromAccount " +
            "JOIN FETCH t.toAccount " +
            "WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId")
    Page<Transfer> findAccountHistory(@Param("accountId") Long accountId, Pageable pageable);
}