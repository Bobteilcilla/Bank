package com.erika.bank.service;

import com.erika.bank.model.Money;
import com.erika.bank.model.Transaction;
import com.erika.bank.model.TransactionType;

import java.time.Instant;
import java.util.*;

public interface BankingService {

    String createAccount(String ownerName, Money initialDeposit);

    Money getBalance(String accountId);

    void deposit(String accountId, Money amount);

    void withdraw(String accountId, Money amount);

    void transfer(String fromAccountId, String toAccountId, Money amount);

    List<Transaction> getTransactions(String accountId);

    List<Transaction> getTransactionsPerType(String accountId, TransactionType expectedType);

    List<Transaction> getTransactionsBetween(String accountId, Instant fromTime, Instant toTime);

    List<Transaction> getTransactionsFromLastDays(String accountId, int days);

}
