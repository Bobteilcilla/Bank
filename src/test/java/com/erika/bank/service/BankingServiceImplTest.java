package com.erika.bank.service;


import com.erika.bank.model.Money;
import com.erika.bank.model.TransactionType;
import com.erika.bank.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BankingServiceImplTest {

    @Test
    void create_account_sets_id_and_initial_balance(){
        BankingService service= new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Erika", Money.of("0.00"));

        assertNotNull(id);
        assertFalse(id.isBlank());
        assertEquals(Money.of("0.00"), service.getBalance(id));
        assertEquals(0, service.getTransactions(id).size());
    }

    @Test
    void create_account_with_initial_deposit_records_transaction() {

        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Erika", Money.of("100.00"));

        assertEquals(Money.of("100.00"), service.getBalance(id));
        assertEquals(1, service.getTransactions(id).size());
    }

    @Test
    void deposit_records_correct_transaction_with_no_funds() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Maria", Money.of("0.00"));

        service.deposit(id, Money.of("50.00"));

        var transactions = service.getTransactions(id);

        assertEquals(1, transactions.size());

        var tx = transactions.get(0);

        assertEquals(id, tx.getAccountId());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(Money.of("50.00"), tx.getAmount());
        assertEquals(Money.of("50.00"), service.getBalance(id));
    }

    @Test
    void deposit_records_correct_transaction_with_funds(){

        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Maria", Money.of("100.00"));

        service.deposit(id, Money.of("50.00"));

        var transactions = service.getTransactions(id);

        assertEquals(2, transactions.size());

        var tx = transactions.get(1); // second transaction

        assertEquals(id, tx.getAccountId());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(Money.of("50.00"), tx.getAmount());
    }

    @Test
    void deposit_increases_balance_and_records_transaction() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Paco", Money.of("0.00"));

        service.deposit(id, Money.of("50.00"));

        assertEquals(Money.of("50.00"), service.getBalance(id));
        assertEquals(1, service.getTransactions(id).size());

    }

    @Test
    void withdraw_decreases_balance_and_records_transaction() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Paco", Money.of("100.00"));

        service.withdraw(id, Money.of("50.00"));

        assertEquals(Money.of("50.00"), service.getBalance(id));
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

    @Test
    void transfer_to_same_account_throws(){
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());
        String accountId = service.createAccount("Erika", Money.of("20.00"));

        assertThrows(IllegalArgumentException.class, () -> service.transfer(accountId, accountId, Money.of("10.00")));
    }

    @Test
    void transfer_more_than_balance_throws(){
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());
        String fromAccountId = service.createAccount("Erika", Money.of("100.00"));
        String toAccountId = service.createAccount("Manolo", Money.of("50.00"));

        assertThrows(IllegalArgumentException.class, () -> service.transfer(fromAccountId, toAccountId, Money.of("200.00")));
    }

    @Test
    void transfer_moves_money_between_accounts_and_records_transactions(){
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());
        String fromAccountId = service.createAccount("Erika", Money.of("100.00"));
        String toAccountId = service.createAccount("Manolo", Money.of("0.00"));

        service.transfer(fromAccountId, toAccountId, Money.of("20.00"));

        assertEquals(Money.of("20.00"), service.getBalance(toAccountId));
        assertEquals(Money.of("80.00"), service.getBalance(fromAccountId));

        var fromTransactions = service.getTransactions(fromAccountId);
        var toTransactions = service.getTransactions(toAccountId);

        assertEquals(2, fromTransactions.size());
        assertEquals(1, toTransactions.size());

        var withdrawTx = fromTransactions.get(1);
        var depositTx = toTransactions.get(0);

        assertEquals(TransactionType.WITHDRAW, withdrawTx.getType());
        assertEquals(fromAccountId, withdrawTx.getAccountId());
        assertEquals(Money.of("20.00"), withdrawTx.getAmount());

        assertEquals(TransactionType.DEPOSIT, depositTx.getType());
        assertEquals(toAccountId, depositTx.getAccountId());
        assertEquals(Money.of("20.00"), depositTx.getAmount());

    }

    @Test
    void withdraw_zero_or_negative_throws(){

        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        String id = service.createAccount("Erika", Money.of("40.00"));

        assertThrows(IllegalArgumentException.class, () -> service.withdraw(id, Money.of("-10.00")));
        assertThrows(IllegalArgumentException.class, () -> service.withdraw(id, Money.of("0.00")));

    }

    @Test
    void get_balance_for_unknown_account_throws(){
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.getBalance("unknown-account"));
    }

    @Test
    void deposit_to_unknown_account_throws() {
        BankingService service = new BankingServiceImpl(new InMemoryAccountRepository());

        assertThrows(IllegalArgumentException.class,
                () -> service.deposit("unknown-account", Money.of("10.00")));
    }
}
