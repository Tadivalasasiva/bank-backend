package com.example.bank_backend.controller;

import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.Transaction;
import com.example.bank_backend.entity.TransactionType;
import com.example.bank_backend.entity.User;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.TransactionRepository;
import com.example.bank_backend.repository.UserRepository;
import com.example.bank_backend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    // In-memory OTP session map for transfers
    private static class TransferSession {
        String senderEmail;
        String recipientAccount;
        Double amount;
        String otp;
        LocalDateTime expiryTime;
    }

    private static final Map<String, TransferSession> transferSessions = new ConcurrentHashMap<>();

    public AccountController(UserRepository userRepository,
                             AccountRepository accountRepository,
                             TransactionRepository transactionRepository,
                             EmailService emailService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
    }

    @GetMapping("/my-account")
    public ResponseEntity<?> getMyAccount(Authentication authentication) {
        String identifier = authentication.getName();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return ResponseEntity.ok(Map.of(
                "fullName", user.getFullName(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                "accountNumber", account.getAccountNumber(),
                "balance", account.getBalance()
        ));
    }

    @PostMapping("/transfers/lookup-recipient")
    public ResponseEntity<?> lookupRecipient(@RequestBody Map<String, String> body, Authentication authentication) {
        String accountNumber = body.get("accountNumber");
        Account targetAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("No registered account found with number: " + accountNumber));

        User currentUser = userRepository.findByEmail(authentication.getName())
                .or(() -> userRepository.findByUsername(authentication.getName()))
                .orElse(null);

        if (currentUser != null && targetAccount.getUser() != null
                && targetAccount.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Self-transfer is not allowed."));
        }

        String recipientName = (targetAccount.getUser() != null) ? targetAccount.getUser().getFullName() : "Registered Nexora User";
        String maskedAcc = "XXXXXXX" + accountNumber.substring(accountNumber.length() - 4);

        return ResponseEntity.ok(Map.of(
                "recipientName", recipientName,
                "accountNumber", accountNumber,
                "maskedAccountNumber", maskedAcc
        ));
    }

    @PostMapping("/transfers/request-otp")
    public ResponseEntity<?> requestTransferOtp(@RequestBody Map<String, Object> body, Authentication auth) {
        String senderIdentifier = auth.getName();
        User sender = userRepository.findByEmail(senderIdentifier)
                .or(() -> userRepository.findByUsername(senderIdentifier))
                .orElseThrow(() -> new RuntimeException("Sender authentication failed."));

        Account senderAccount = accountRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender account record not found."));

        String recipientAcc = (String) body.get("recipientAccountNumber");
        Double amount = Double.valueOf(body.get("amount").toString());

        if (amount <= 0 || senderAccount.getBalance() < amount) {
            return ResponseEntity.badRequest().body(Map.of("message", "Insufficient account balance."));
        }

        Account recipientAccount = accountRepository.findByAccountNumber(recipientAcc)
                .orElseThrow(() -> new RuntimeException("Invalid recipient account."));

        String otp = String.format("%06d", new Random().nextInt(999999));
        String reqId = "TF_" + UUID.randomUUID().toString();

        TransferSession session = new TransferSession();
        session.senderEmail = sender.getEmail();
        session.recipientAccount = recipientAcc;
        session.amount = amount;
        session.otp = otp;
        session.expiryTime = LocalDateTime.now().plusMinutes(5);
        transferSessions.put(reqId, session);

        String recipientName = recipientAccount.getUser() != null ? recipientAccount.getUser().getFullName() : "Beneficiary";
        String last4 = recipientAcc.substring(recipientAcc.length() - 4);
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        emailService.sendTransferOtpEmail(sender.getEmail(), sender.getFullName(), otp, recipientName, last4, String.format("%.2f", amount), nowStr);

        return ResponseEntity.ok(Map.of(
                "transferRequestId", reqId,
                "message", "Transfer OTP sent to " + sender.getEmail()
        ));
    }

    @PostMapping("/transfers/confirm")
    @Transactional
    public ResponseEntity<?> confirmTransfer(@RequestBody Map<String, String> body, Authentication auth) {
        String reqId = body.get("transferRequestId");
        String otp = body.get("otp");

        TransferSession session = transferSessions.get(reqId);
        if (session == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired transfer request."));
        }

        if (LocalDateTime.now().isAfter(session.expiryTime)) {
            transferSessions.remove(reqId);
            return ResponseEntity.badRequest().body(Map.of("message", "Transfer OTP has expired. Please request a new one."));
        }

        if (!session.otp.equals(otp != null ? otp.trim() : "")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect OTP. Verification failed."));
        }

        User sender = userRepository.findByEmail(session.senderEmail).orElseThrow();
        Account senderAccount = accountRepository.findByUser(sender).orElseThrow();
        Account recipientAccount = accountRepository.findByAccountNumber(session.recipientAccount).orElseThrow();

        if (senderAccount.getBalance() < session.amount) {
            return ResponseEntity.badRequest().body(Map.of("message", "Insufficient funds."));
        }

        // Deduct from sender & credit recipient
        senderAccount.setBalance(senderAccount.getBalance() - session.amount);
        recipientAccount.setBalance(recipientAccount.getBalance() + session.amount);
        accountRepository.save(senderAccount);
        accountRepository.save(recipientAccount);

        // Requirement 10: Generate ONE common transaction ID for BOTH transactions
        String txRef = "TXN" + System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        // Sender transaction record
        Transaction debitTx = new Transaction();
        // FIXED: Only set the single account this transaction belongs to
        debitTx.setAccount(senderAccount);
        debitTx.setAmount(session.amount);
        debitTx.setType(TransactionType.DEBIT); // Or WITHDRAWAL based on your enum
        debitTx.setBalanceAfter(senderAccount.getBalance());
        debitTx.setDescription("TRANSFER TO " + (recipientAccount.getUser() != null ? recipientAccount.getUser().getFullName() : session.recipientAccount));
        // FIXED: Use setTransactionId instead of setReferenceNumber
        debitTx.setTransactionId(txRef);
        debitTx.setTimestamp(now);
        transactionRepository.save(debitTx);

        // Recipient transaction record
        Transaction creditTx = new Transaction();
        // FIXED: Only set the single account this transaction belongs to
        creditTx.setAccount(recipientAccount);
        creditTx.setAmount(session.amount);
        creditTx.setType(TransactionType.CREDIT); // Or DEPOSIT based on your enum
        creditTx.setBalanceAfter(recipientAccount.getBalance());
        creditTx.setDescription("TRANSFER FROM " + sender.getFullName());
        // FIXED: Use the exact same txRef (No "C" appended) to link them properly
        creditTx.setTransactionId(txRef);
        creditTx.setTimestamp(now);
        transactionRepository.save(creditTx);

        transferSessions.remove(reqId);

        String recipientName = (recipientAccount.getUser() != null) ? recipientAccount.getUser().getFullName() : "Customer";
        String dateStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return ResponseEntity.ok(Map.of(
                "transactionId", txRef,
                "amount", session.amount,
                "recipientName", recipientName,
                "maskedAccountNumber", "XXXXXXX" + session.recipientAccount.substring(session.recipientAccount.length() - 4),
                "transactionDate", dateStr,
                "newAvailableBalance", senderAccount.getBalance()
        ));
    }
}