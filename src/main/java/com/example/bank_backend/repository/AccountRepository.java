package com.example.bank_backend.repository;

import com.example.bank_backend.entity.Account;
import com.example.bank_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUser(User user);

    Optional<Account> findByUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    Boolean existsByAccountNumber(String accountNumber);
}