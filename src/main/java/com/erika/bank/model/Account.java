package com.erika.bank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Account {

    private final String id;
    private final String ownerName;

    private Money balance;
    private final List<Transaction> transactions = new ArrayList<>();


    public Account(String id, String ownerName, Money initialBalance ){
        if(id == null || id.isBlank()){
            throw new IllegalArgumentException("Account id cannot be empty or null");
        }
        if(ownerName == null || ownerName.isBlank()){
            throw new IllegalArgumentException("Account ownerName cannot be empry or null");
        }
       this.id = id;
        this.ownerName = ownerName;
        this.balance = Objects.requireNonNull(initialBalance, "Initial balance cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getOwnerName(){
        return ownerName;
    }

    public Money getBalance() {
        return balance;
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
        if( balance.getAmount().compareTo(amount.getAmount()) < 0){
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount);
        transactions.add(tx);
    }

    private void validatePositive(Money amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
    }

    private void requireTx(Transaction tx, TransactionType expectedType){
        Objects.requireNonNull(tx, "Transaction cannot be null");
        if(tx.getType() != expectedType) {
            throw new IllegalArgumentException("Transaction type mus be " + expectedType);
        }
        if(!id.equals(tx.getAccountId())) {
            throw new IllegalArgumentException("Transaction accountId must match this account");
        }
    }
}
