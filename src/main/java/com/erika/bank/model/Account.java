package com.erika.bank.model;

import com.erika.bank.exceptions.InsufficientFundsException;
import com.erika.bank.exceptions.InvalidAmountException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Account {

    @Getter
    private final String id;
    @Getter
    private final String ownerName;
    @Getter
    private Money balance;
    private final List<Transaction> transactions = new ArrayList<>();


    public Account(String id, String ownerName, Money initialBalance ){
        if(id == null || id.isBlank()){
            throw new IllegalArgumentException("Account id cannot be empty or null");
        }
        if(ownerName == null || ownerName.isBlank()){
            throw new IllegalArgumentException("Account ownerName cannot be empty or null");
        }
       this.id = id;
        this.ownerName = ownerName;
        if (initialBalance == null) {
            throw new IllegalArgumentException("Initial balance cannot be null");
        }
        this.balance = initialBalance;
    }

    public List<Transaction> getTransactions(){
        return Collections.unmodifiableList(transactions);
    }

    public void deposit(Money amount, Transaction tx){
        validatePositive(amount);
        requireTx(tx, TransactionType.DEPOSIT);
        balance = balance.add(amount);
        transactions.add(tx);
    }

    public void withdraw(Money amount, Transaction tx){
        validatePositive(amount);
        requireTx(tx, TransactionType.WITHDRAW);

        // Business rule: cannot go below zero
        if( balance.compareTo(amount) < 0){
            throw new InsufficientFundsException();
        }
        balance = balance.subtract(amount);
        transactions.add(tx);
    }

    private void validatePositive(Money amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if( amount.getAmount().signum() <= 0) {
            throw new InvalidAmountException("Validate positive: ");
        }
    }

    private void requireTx(Transaction tx, TransactionType expectedType){
        Objects.requireNonNull(tx, "Transaction cannot be null");
        if(tx.getType() != expectedType) {
            throw new IllegalArgumentException("Transaction type must be " + expectedType);
        }
        if(!id.equals(tx.getAccountId())) {
            throw new IllegalArgumentException("Transaction accountId must match this account");
        }
    }
}
