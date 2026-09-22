package com.example.bank_backend.service;

import com.example.bank_backend.dto.StatementDtos.*;
import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.Transaction;
import com.example.bank_backend.entity.TransactionType;
import com.example.bank_backend.entity.User;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.TransactionRepository;
import com.example.bank_backend.repository.UserRepository;
import com.example.bank_backend.util.PdfStatementGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StatementService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public StatementService(UserRepository userRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    private String maskAccountNumber(String accNum) {
        if (accNum == null || accNum.length() < 4) {
            return "XXXX";
        }
        return "XXXXXXX" + accNum.substring(accNum.length() - 4);
    }

    public StatementSummaryResponse generateStatementSummary(String userEmail, String fromDateStr, String toDateStr) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User identity verification failed."));
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account record not found."));

        LocalDate fromDate = (fromDateStr != null && !fromDateStr.isBlank())
                ? LocalDate.parse(fromDateStr)
                : LocalDate.of(2026, 8, 1);

        LocalDate toDate = (toDateStr != null && !toDateStr.isBlank())
                ? LocalDate.parse(toDateStr)
                : LocalDate.now();

        List<Transaction> allTransactions = transactionRepository.findAll();
        List<StatementTransactionItem> items = new ArrayList<>();

        double totalCredits = 0.0;
        double totalDebits = 0.0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        allTransactions.sort(Comparator.comparing(
                tx -> tx.getTimestamp() != null ? tx.getTimestamp() : LocalDateTime.now(),
                Comparator.reverseOrder()
        ));

        for (Transaction tx : allTransactions) {
            // FIXED: Only uses getAccount() now
            boolean belongsToAccount = (tx.getAccount() != null && tx.getAccount().getId().equals(account.getId()));

            if (belongsToAccount) {
                LocalDateTime txTime = tx.getTimestamp() != null ? tx.getTimestamp() : LocalDateTime.now();
                LocalDate txDate = txTime.toLocalDate();

                if (!txDate.isBefore(fromDate) && !txDate.isAfter(toDate)) {
                    boolean isDebit = tx.getType() == TransactionType.DEBIT
                            || tx.getType() == TransactionType.WITHDRAWAL
                            || tx.getType() == TransactionType.TRANSFER;

                    Double debit = isDebit ? tx.getAmount() : null;
                    Double credit = !isDebit ? tx.getAmount() : null;

                    if (debit != null) totalDebits += debit;
                    if (credit != null) totalCredits += credit;

                    // FIXED: Uses getTransactionId() instead of getReferenceNumber()
                    String referenceId = tx.getTransactionId() != null
                            ? tx.getTransactionId()
                            : "TXN" + tx.getId();

                    items.add(new StatementTransactionItem(
                            referenceId,
                            txTime.format(dtf),
                            tx.getDescription(),
                            debit,
                            credit,
                            tx.getBalanceAfter() != null ? tx.getBalanceAfter() : account.getBalance(),
                            tx.getType().name()
                    ));
                }
            }
        }

        double closingBalance = account.getBalance();
        double openingBalance = closingBalance - totalCredits + totalDebits;

        return new StatementSummaryResponse(
                user.getFullName(),
                account.getAccountNumber(),
                maskAccountNumber(account.getAccountNumber()),
                "Savings Account",
                fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                openingBalance,
                totalCredits,
                totalDebits,
                closingBalance,
                items
        );
    }

    public ByteArrayInputStream generateStatementPdf(String userEmail) {
        StatementSummaryResponse summary = generateStatementSummary(userEmail, null, null);
        return PdfStatementGenerator.generatePdf(summary);
    }
}