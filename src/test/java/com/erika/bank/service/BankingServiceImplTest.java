package com.erika.bank.service;

import com.erika.bank.model.Account;
import com.erika.bank.model.Money;
import com.erika.bank.model.TransactionType;
import com.erika.bank.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BankingServiceImplTest {

    @Test
    void createAccount_setId_setsInitialBalance(){
        BankingService service= new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Erika", Money.of("0.00"));

        assertNotNull(id);
        assertFalse(id.isBlank());
        assertEquals("0.00", service.getBalance(id).toString());
        assertEquals(0, service.getTransactions(id).size());
    }

    @Test
    void createAccount_with_initial_deposit_records_transaction() {

        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Erika", Money.of("100.00"));

        assertEquals("100.00", service.getBalance(id).toString());
        assertEquals(1, service.getTransactions(id).size());
    }

    @Test
    void deposit_increases_balance_and_records_transaction() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Paco", Money.of("0.00"));

        service.deposit(id, Money.of("50.00"));

        assertEquals("50.00", service.getBalance(id).toString());
        assertEquals(1, service.getTransactions(id).size());

    }

    @Test
    void withdraw_decreases_balance_and_records_transaction() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Paco", Money.of("100.00"));

        service.withdraw(id, Money.of("50.00"));

        assertEquals("50.00", service.getBalance(id).toString());
        assertEquals(2, service.getTransactions(id).size());
    }

    @Test
    void withdraw_more_than_balance_throws(){
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Paco", Money.of("30.00"));

        assertThrows(IllegalArgumentException.class, () -> service.withdraw(id, Money.of("50.00")));
    }

    @Test
    void deposit_zero_or_negative_throws() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());
        String id = service.createAccount("Erika", Money.of("0.00"));

        assertThrows(IllegalArgumentException.class, () -> service.deposit(id, Money.of("0.00")));
        assertThrows(IllegalArgumentException.class, () -> service.deposit(id, Money.of("-1.00")));
    }
}
