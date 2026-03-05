package com.erika.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collector;

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
}
