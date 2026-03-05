package com.erika.bank.repository;

import com.erika.bank.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(String id);

    void save(Account account);

    boolean existsById(String id);

    List<Account> findAll();

}
