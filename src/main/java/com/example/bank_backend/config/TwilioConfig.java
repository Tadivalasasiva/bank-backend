package com.example.bank_backend.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    private static final Logger log = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${twilio.account-sid:mock_sid}")
    private String accountSid;

    @Value("${twilio.auth-token:mock_token}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        if (accountSid != null && !accountSid.startsWith("mock_") && !accountSid.isBlank()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio client initialized successfully.");
        } else {
            log.warn("Twilio credentials not configured; voice operations will run in mock mode.");
        }
    }
}