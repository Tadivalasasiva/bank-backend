package com.example.bank_backend.repository;

import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Fetch transactions for a specific account (Newest first)
    List<Transaction> findByAccountOrderByTimestampDesc(Account account);

    // Fetch transactions for E-Statement date filtering
    List<Transaction> findByAccountAndTimestampBetweenOrderByTimestampDesc(Account account, LocalDateTime startDate, LocalDateTime endDate);
}