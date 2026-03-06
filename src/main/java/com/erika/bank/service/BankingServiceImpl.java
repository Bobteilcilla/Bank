package com.erika.bank.service;

import com.erika.bank.model.Money;
import com.erika.bank.model.Transaction;
import com.erika.bank.model.Account;
import com.erika.bank.model.TransactionType;
import com.erika.bank.repository.AccountRepository;


import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BankingServiceImpl implements BankingService {

    private final AccountRepository repo;

    public BankingServiceImpl(AccountRepository repo){
        this.repo = Objects.requireNonNull(repo, "Repo cannot be null");
    }

    @Override
    public String createAccount(String ownerName, Money initialDeposit) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be null/blank");
        }
        Objects.requireNonNull(initialDeposit, "Initial deposit cannot be null");

        String accountId = UUID.randomUUID().toString();

        Account account = new Account(accountId, ownerName, Money.of("0.00"));

        //We record the initial deposit as a transaction if the initialDeposit > 0
        if ( initialDeposit.getAmount().signum() > 0 ){
            Transaction tx = new Transaction (
                    UUID.randomUUID().toString(),
                    TransactionType.DEPOSIT,
                    accountId,
                    initialDeposit,
                    Instant.now()
            );
            account.deposit(initialDeposit, tx);
        }
        repo.save(account);
        return accountId;
    }

    @Override
    public Money getBalance(String accountId) {

        return findAccount(accountId).getBalance();
    }

    @Override
    public void deposit(String accountId, Money amount) {
        Account account = findAccount(accountId);

        requirePositive(amount, "Deposit");
        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                accountId,
                amount,
                Instant.now()
        );
        account.deposit(amount, tx);
        repo.save(account);
    }

    @Override
    public void withdraw(String accountId, Money amount) {

        Account account = findAccount(accountId);

        requirePositive(amount, "Withdraw");
        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.WITHDRAW,
                accountId,
                amount,
                Instant.now()
        );
        account.withdraw(amount, tx);
        repo.save(account);
    }

    @Override
    public void transfer(String fromAccountId, String toAccountId, Money amount) {
        requirePositive(amount, "Transfer");

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account fromAccount = findAccount(fromAccountId);
        Account toAccount = findAccount(toAccountId);

        Transaction txWithdraw = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.WITHDRAW,
                fromAccountId,
                amount,
                Instant.now()
        );

        Transaction txDeposit = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                toAccountId,
                amount,
                Instant.now()
        );

        fromAccount.withdraw(amount, txWithdraw);
        toAccount.deposit(amount, txDeposit);

        repo.save(fromAccount);
        repo.save(toAccount);
    }

    @Override
    public List<Transaction> getTransactions(String accountId) {
        return findAccount(accountId).getTransactions();
    }

    @Override
    public List<Transaction> getTransactionsPerType(String accountId, TransactionType expectedType) {

        List<Transaction> transactionsPerType = new ArrayList<>();

        if (expectedType == null) {
            throw new IllegalArgumentException("The transaction type cannot be null");
        }

        List<Transaction> transactions = getTransactions(accountId);
        for (Transaction tx : transactions) {
            if (tx.getType() == expectedType) {
                transactionsPerType.add(tx);
            }
        }
        return transactionsPerType;
    }

    @Override
    public List<Transaction> getTransactionsBetween(String accountId, Instant fromTime, Instant toTime) {
        List<Transaction> transactionsPerTimeRange = new ArrayList<>();

        if (fromTime == null || toTime == null) {
            throw new IllegalArgumentException("The timeframe cannot be null");
        }

        if (fromTime.isAfter(toTime)) {
            throw new IllegalArgumentException("fromTime must be before toTime");
        }

        List<Transaction> transactions = getTransactions(accountId);
        for (Transaction tx : transactions) {
            Instant checkTime = tx.getTimestamp();
            if (!checkTime.isBefore(fromTime) && !checkTime.isAfter(toTime)) {
                transactionsPerTimeRange.add(tx);
            }
        }
        return transactionsPerTimeRange;
    }

    @Override
    public List<Transaction> getTransactionsFromLastDays(String accountId, int days) {
        List<Transaction> recentTransactions = new ArrayList<>();

        if (days < 0) {
            throw new IllegalArgumentException("Limit must be a positive number");
        }

        if (days == 0) {
           return getTransactions(accountId);
        }
        long toSeconds = (long) days * 24 * 60 * 60;
        Instant timeLimit = Instant.now().minusSeconds(toSeconds);

        List<Transaction> transactions = getTransactions(accountId);
        for (Transaction tx : transactions) {
            if (tx.getTimestamp().isAfter(timeLimit)) {
                recentTransactions.add(tx);
            }
        }
        return recentTransactions;
    }

    private Account findAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account id cannot be null or blank");
        }

        return repo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    private void requirePositive(Money amount, String operationName) {
        if (amount == null) {
            throw new IllegalArgumentException(operationName + " amount cannot be null");
        }
        if (amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(operationName + " amount must be > 0");
        }
    }

}
