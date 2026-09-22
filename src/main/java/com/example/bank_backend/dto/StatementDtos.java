package com.example.bank_backend.dto;

import java.util.List;

public class StatementDtos {

    public static class StatementRequest {
        private String fromDate;
        private String toDate;

        public StatementRequest() {}

        public StatementRequest(String fromDate, String toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        public String getFromDate() { return fromDate; }
        public void setFromDate(String fromDate) { this.fromDate = fromDate; }
        public String getToDate() { return toDate; }
        public void setToDate(String toDate) { this.toDate = toDate; }
    }

    public static class StatementSummaryResponse {
        private String customerName;
        private String accountNumber;
        private String maskedAccountNumber;
        private String accountType;
        private String fromDate;
        private String toDate;
        private Double openingBalance;
        private Double totalCredits;
        private Double totalDebits;
        private Double closingBalance;
        private List<StatementTransactionItem> transactions;

        public StatementSummaryResponse(String customerName, String accountNumber, String maskedAccountNumber,
                                        String accountType, String fromDate, String toDate, Double openingBalance,
                                        Double totalCredits, Double totalDebits, Double closingBalance,
                                        List<StatementTransactionItem> transactions) {
            this.customerName = customerName;
            this.accountNumber = accountNumber;
            this.maskedAccountNumber = maskedAccountNumber;
            this.accountType = accountType;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.openingBalance = openingBalance;
            this.totalCredits = totalCredits;
            this.totalDebits = totalDebits;
            this.closingBalance = closingBalance;
            this.transactions = transactions;
        }

        public String getCustomerName() { return customerName; }
        public String getAccountNumber() { return accountNumber; }
        public String getMaskedAccountNumber() { return maskedAccountNumber; }
        public String getAccountType() { return accountType; }
        public String getFromDate() { return fromDate; }
        public String getToDate() { return toDate; }
        public Double getOpeningBalance() { return openingBalance; }
        public Double getTotalCredits() { return totalCredits; }
        public Double getTotalDebits() { return totalDebits; }
        public Double getClosingBalance() { return closingBalance; }
        public List<StatementTransactionItem> getTransactions() { return transactions; }
    }

    public static class StatementTransactionItem {
        private String transactionId;
        private String date;
        private String description;
        private Double debit;
        private Double credit;
        private Double balance;
        private String type;

        public StatementTransactionItem(String transactionId, String date, String description,
                                        Double debit, Double credit, Double balance, String type) {
            this.transactionId = transactionId;
            this.date = date;
            this.description = description;
            this.debit = debit;
            this.credit = credit;
            this.balance = balance;
            this.type = type;
        }

        public String getTransactionId() { return transactionId; }
        public String getDate() { return date; }
        public String getDescription() { return description; }
        public Double getDebit() { return debit; }
        public Double getCredit() { return credit; }
        public Double getBalance() { return balance; }
        public String getType() { return type; }
    }
}