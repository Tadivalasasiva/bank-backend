package com.example.bank_backend.repository;

import com.example.bank_backend.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByPhoneNumberAndIsUsedFalseOrderByExpiryTimeDesc(String phoneNumber);
}