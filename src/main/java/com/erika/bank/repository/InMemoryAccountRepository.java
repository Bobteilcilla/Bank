package com.erika.bank.repository;

import com.erika.bank.model.Account;

import java.util.*;


public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts = new HashMap<>();
    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public void save(Account account) {
        if(account == null){
            throw new IllegalArgumentException("The account cannot be null");
        }
        accounts.put(account.getId(), account);
    }

    @Override
    public boolean existsById(String id) {
        return accounts.containsKey(id);
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }
}
