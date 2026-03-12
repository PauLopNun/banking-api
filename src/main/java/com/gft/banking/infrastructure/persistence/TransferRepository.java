package com.gft.banking.infrastructure.persistence;

import com.gft.banking.domain.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @Query("SELECT t FROM Transfer t " +
            "JOIN FETCH t.fromAccount " +
            "JOIN FETCH t.toAccount " +
            "WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId " +
            "ORDER BY t.createdAt DESC")
    List<Transfer> findAccountHistory(@Param("accountId") Long accountId);
}