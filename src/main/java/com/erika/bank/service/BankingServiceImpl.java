package com.erika.bank.service;

import com.erika.bank.model.Money;
import com.erika.bank.model.Transaction;
import com.erika.bank.model.Account;
import com.erika.bank.model.TransactionType;
import com.erika.bank.repository.AccountRepository;


import java.time.Instant;
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
    public List<Transaction> getTransactions(String accountId) {
        return findAccount(accountId).getTransactions();
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
