package com.example.bank_backend.service;

import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.Transaction;
import com.example.bank_backend.entity.User;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.TransactionRepository;
import com.example.bank_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    // Requirement 13, 14 & 15: Retrieve ONLY the logged-in user's independent transactions
    public List<Transaction> getMyTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    // Requirement 16: E-Statement filtered by date for the specific user
    public List<Transaction> getStatementByDateRange(String username, LocalDateTime fromDate, LocalDateTime toDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository.findByAccountAndTimestampBetweenOrderByTimestampDesc(account, fromDate, toDate);
    }
}