package com.erika.bank.service;

import com.erika.bank.model.Money;
import com.erika.bank.model.Transaction;
import com.erika.bank.model.Account;
import com.erika.bank.repository.AccountRepository;
import com.erika.bank.repository.InMemoryAccountRepository;

import java.util.List;
import java.util.UUID;

public class BankingServiceImpl implements BankingService {

    private final AccountRepository repo;

    public BankingServiceImpl(AccountRepository repo){
        this.repo = repo;
    }

    @Override
    public String createAccount(String ownerName, Money initialDeposit) {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, ownerName, initialDeposit);
        repo.save(account);
        return accountId;
    }

    @Override
    public Money getBalance(String accountId) {
        return null;
    }

    @Override
    public void deposit(String accountId, Money amount) {

    }

    @Override
    public void withdraw(String accountId, Money amount) {

    }

    @Override
    public List<Transaction> getTransactions(String accountId) {
        return List.of();
    }
}
