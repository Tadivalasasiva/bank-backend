package com.example.bank_backend.service;

import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.Transaction;
import com.example.bank_backend.entity.TransactionType; // Required for the Enum fix
import com.example.bank_backend.entity.User;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.TransactionRepository;
import com.example.bank_backend.repository.UserRepository; // Required to fetch the user
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository; // Added to fix the findByUser error

    @Autowired
    private EmailService emailService;

    // (Keep your existing requestTransferOtp method here)

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirmTransfer(String senderUsername, String transferRequestId, String otp) {

        // 1. Fetch User then Account (fixes the missing findByUser_Username method)
        User senderUser = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender user not found."));

        Account senderAcc = accountRepository.findByUser(senderUser)
                .orElseThrow(() -> new RuntimeException("Sender account not found."));

        // Mock retrieval: Replace with your actual logic to retrieve the transfer request amount/recipient using transferRequestId
        String receiverAccountNumber = "RETRIEVED_FROM_REQUEST";
        double amount = 100.00;

        Account receiverAcc = accountRepository.findByAccountNumber(receiverAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found."));

        if (senderAcc.getId().equals(receiverAcc.getId())) {
            throw new RuntimeException("Cannot transfer to your own account.");
        }
        if (senderAcc.getBalance() < amount) {
            throw new RuntimeException("Insufficient available balance.");
        }

        // Update Balances
        senderAcc.setBalance(senderAcc.getBalance() - amount);
        receiverAcc.setBalance(receiverAcc.getBalance() + amount);

        accountRepository.save(senderAcc);
        accountRepository.save(receiverAcc);

        // Generate Common Reference ID
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String referenceId = "TXN" + LocalDateTime.now().format(dtf) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Create Sender DEBIT Transaction
        Transaction senderTx = new Transaction();
        senderTx.setAccount(senderAcc);
        senderTx.setType(TransactionType.DEBIT); // FIXED: Uses Enum instead of String
        senderTx.setAmount(amount);
        senderTx.setBalanceAfter(senderAcc.getBalance());
        senderTx.setDescription("Transfer to " + receiverAcc.getUser().getFullName());

        // FIXED: Using typical Spring Boot naming. (See Note below if these still error)
        senderTx.setTransactionId(referenceId);
        senderTx.setTimestamp(LocalDateTime.now());

        transactionRepository.save(senderTx);

        // Create Receiver CREDIT Transaction
        Transaction receiverTx = new Transaction();
        receiverTx.setAccount(receiverAcc);
        receiverTx.setType(TransactionType.CREDIT); // FIXED: Uses Enum instead of String
        receiverTx.setAmount(amount);
        receiverTx.setBalanceAfter(receiverAcc.getBalance());
        receiverTx.setDescription("Transfer from " + senderAcc.getUser().getFullName());

        // FIXED: Using typical Spring Boot naming
        receiverTx.setTransactionId(referenceId);
        receiverTx.setTimestamp(LocalDateTime.now());

        transactionRepository.save(receiverTx);

        return Map.of(
                "message", "Transfer Successful",
                "transactionId", referenceId,
                "newAvailableBalance", senderAcc.getBalance(),
                "amount", amount,
                "recipientName", receiverAcc.getUser().getFullName(),
                "maskedAccountNumber", "XXXXXXX" + receiverAcc.getAccountNumber().substring(receiverAcc.getAccountNumber().length() - 4)
        );
    }
}