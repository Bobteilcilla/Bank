package com.erika.bank.model;

import java.time.Instant;
import java.util.Objects;

public class Transaction {
    private final String id;
    private final TransactionType type;
    private final String accountId;
    private final Money amount;
    private final Instant timestamp;
    private final String description;

    public Transaction(String id, TransactionType type, String accountId, Money amount, Instant timestamp, String description) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The Transaction id cannot be null");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("The Account id cannot be null");
        }

        this.id = id;
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.accountId = accountId;
        this.amount = Objects.requireNonNull(amount, "Transaction amount cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Transaction timestamp cannot be null");
        this.description = description;
    }

    public String getId() {

        return id;
    }

    public TransactionType getType() {

        return type;
    }

    public String getAccountId() {

        return accountId;
    }

    public Money getAmount() {
        return amount;
    }

    public Instant getTimestamp() {

        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return timestamp + " | " + type + " | " + amount + " | " + description;
    }
}

