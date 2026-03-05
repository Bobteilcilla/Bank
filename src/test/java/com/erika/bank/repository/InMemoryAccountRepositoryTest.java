package com.erika.bank.repository;

import com.erika.bank.model.Account;
import com.erika.bank.model.Money;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryAccountRepositoryTest {

    @Test
    void save_and_findById_works() {

        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        Account account = new Account(
                "A2",
                "Juan",
                Money.of("100.00")
        );

        repo.save(account);

        assertTrue(repo.findById(account.getId()).isPresent());
        assertEquals(account.getId(), repo.findById(account.getId()).get().getId());
    }

    @Test
    void existsById_works() {

        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        Account account = new Account("A1", "Erika", Money.of("50.00"));
        repo.save(account);

        assertTrue(repo.existsById(account.getId()));
        assertFalse(repo.existsById("UNKNOWN"));
    }

    @Test
    void findAllAccounts_works(){

        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        Account a1 = new Account("A1", "Erika", Money.of("20.00"));
        repo.save(a1);

        Account a2 = new Account("A4", "Juani", Money.of("40.00"));
        repo.save(a2);

        List<Account> accounts = repo.findAll();

        assertEquals(2, accounts.size());
        assertTrue(accounts.contains(a1));
        assertTrue(accounts.contains(a2));
    }
}
