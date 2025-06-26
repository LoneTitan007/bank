package com.common.bank.repository;

import com.common.bank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByRefId(String refId);
    
    List<Transaction> findBySourceAccountRefId(String sourceAccountRefId);
    
    List<Transaction> findByDestinationAccountRefId(String destinationAccountRefId);
    
    List<Transaction> findBySourceAccountIdOrDestinationAccountId(Long sourceAccountId, Long destinationAccountId);
}
