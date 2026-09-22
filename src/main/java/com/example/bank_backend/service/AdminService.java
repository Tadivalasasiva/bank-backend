package com.example.bank_backend.service;

import com.example.bank_backend.dto.AccountResponse;
import com.example.bank_backend.dto.AccountStatusUpdateRequest;
import com.example.bank_backend.dto.TransactionResponse;
import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.AccountStatus;
import com.example.bank_backend.entity.Transaction;
import com.example.bank_backend.exception.ResourceNotFoundException;
import com.example.bank_backend.repository.AccountRepository;
import com.example.bank_backend.repository.TransactionRepository;
import com.example.bank_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminService(UserRepository userRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::mapToAccountDTO);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getGlobalTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::mapToTransactionDTO);
    }

    @Transactional
    public String updateAccountStatus(String accountNumber, AccountStatusUpdateRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));

        account.setStatus(AccountStatus.valueOf(request.getStatus().toUpperCase()));
        accountRepository.save(account);
        return "Account status updated successfully to " + request.getStatus();
    }

    private AccountResponse mapToAccountDTO(Account account) {
        AccountResponse dto = new AccountResponse();
        dto.setAccountNumber(account.getAccountNumber());
        if (account.getBalance() != null) {
            dto.setBalance(BigDecimal.valueOf(account.getBalance()));
        } else {
            dto.setBalance(BigDecimal.ZERO);
        }
        dto.setStatus(account.getStatus() != null ? account.getStatus().toString() : "ACTIVE");
        return dto;
    }

    private TransactionResponse mapToTransactionDTO(Transaction tx) {
        TransactionResponse dto = new TransactionResponse();
        return dto;
    }
}