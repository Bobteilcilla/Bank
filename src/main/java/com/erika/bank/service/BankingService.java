package com.erika.bank.service;

import com.erika.bank.model.Money;
import com.erika.bank.model.Transaction;

import java.util.*;

public interface BankingService {

    String createAccount(String ownerName, Money initialDeposit);

    Money getBalance(String accountId);

    void deposit(String accountId, Money amount);

    void withdraw(String accountId, Money amount);

    List<Transaction> getTransactions(String accountId);

}
