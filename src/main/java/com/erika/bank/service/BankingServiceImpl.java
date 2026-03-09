package com.erika.bank.service;

import com.erika.bank.exceptions.AccountNotFoundException;
import com.erika.bank.exceptions.InvalidTimeRangeException;
import com.erika.bank.model.*;
import com.erika.bank.repository.AccountRepository;


import java.nio.channels.AcceptPendingException;
import java.time.Instant;
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
                    Instant.now(),
                    "Deposit to account " + accountId
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
                Instant.now(),
                "Deposit to account " + accountId
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
                Instant.now(),
                "Withdraw from account " + accountId
        );
        account.withdraw(amount, tx);
        repo.save(account);
    }

    @Override
    public void transfer(String fromAccountId, String toAccountId, Money amount) {
        requirePositive(amount, "Transfer");

        Account fromAccount = findAccount(fromAccountId);
        Account toAccount = findAccount(toAccountId);

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Transaction txWithdraw = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.WITHDRAW,
                fromAccountId,
                amount,
                Instant.now(),
                "Withdraw from account " + fromAccountId
        );

        Transaction txDeposit = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                toAccountId,
                amount,
                Instant.now(),
                "Deposit to account " + toAccountId
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
            throw new InvalidTimeRangeException("The timeframe cannot be null");
        }

        if (fromTime.isAfter(toTime)) {
            throw new InvalidTimeRangeException("fromTime must be before toTime");
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
            throw new IllegalArgumentException("Days must be zero or greater");
        }

        if (days == 0) {
           return getTransactions(accountId);
        }
        long toSeconds = (long) days * 24 * 60 * 60;
        Instant timeLimit = Instant.now().minusSeconds(toSeconds);

        List<Transaction> transactions = getTransactions(accountId);
        for (Transaction tx : transactions) {
            if (!tx.getTimestamp().isBefore(timeLimit)) {
                recentTransactions.add(tx);
            }
        }
        return recentTransactions;
    }

    @Override
    public AccountStatement getAccountStatement(String accountId) {
        Account account = findAccount(accountId);
        List<Transaction> transactions = getTransactions(accountId);
        return buildStatement(account, transactions);
    }

    @Override
    public AccountStatement getAccountStatement(String accountId, int days) {
        Account account = findAccount(accountId);
        List<Transaction> transactions = getTransactionsFromLastDays(accountId, days);
        return buildStatement(account, transactions);
    }

    private AccountStatement buildStatement(Account account, List<Transaction> transactions){
        Money totalDeposits = Money.of("0.00");
        Money totalWithdrawals = Money.of("0.00");

        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.DEPOSIT) {
                totalDeposits = totalDeposits.add(tx.getAmount());
            } else if (tx.getType() == TransactionType.WITHDRAW) {
                totalWithdrawals = totalWithdrawals.add(tx.getAmount());
            }
        }

        return AccountStatement.builder()
                .accountId(account.getId())
                .ownerName(account.getOwnerName())
                .balance(account.getBalance())
                .transactionCount(transactions.size())
                .totalDeposits(totalDeposits)
                .totalWithdrawals(totalWithdrawals)
                .transactions(transactions)
                .build();
    }

    private Account findAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account id cannot be null or blank");
        }

        return repo.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
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
