package com.erika.bank.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountTest {

    @Test
    void create_account_initial_balance_null() {

        assertThrows(IllegalArgumentException.class, () -> new Account(
                "A1",
                "Erika",
                null
        ));

    }

    @Test
    void create_account_id_is_blank() {

        assertThrows(IllegalArgumentException.class, () -> new Account(
                "",
                "Erika",
                Money.of("20.00")
        ));

    }

    @Test
    void create_account_owner_name_blank() {

        assertThrows(IllegalArgumentException.class, () -> new Account(
                "A3",
                "",
                Money.of("30.00")
        ));

    }

    @Test
    void deposit_with_null_transaction_throws() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        assertThrows(NullPointerException.class, () -> account.deposit(Money.of("20.00"), null));

    }

    @Test
    void deposit_with_wrong_transaction_type_throws() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        Transaction tx = transactionBuilder(
                "123", TransactionType.WITHDRAW, "A1", "20.00",
                Instant.now(), "Deposit to account A5");


        assertThrows(IllegalArgumentException.class, () -> account.deposit(Money.of("20.00"), tx));

    }

    @Test
    void deposit_with_transaction_for_different_account_throws() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        Transaction tx = transactionBuilder(
                "123", TransactionType.DEPOSIT, "A5", "20.00",
                Instant.now(), "Deposit to account A5");


        assertThrows(IllegalArgumentException.class, () -> account.deposit(Money.of("20.00"), tx));

    }

    @Test
    void withdraw_with_null_transaction_throws() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        assertThrows(NullPointerException.class, () -> account.withdraw(Money.of("20.00"), null));

    }

    @Test
    void withdraw_with_wrong_transaction_type_throws() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        Transaction tx = transactionBuilder(
                "123", TransactionType.DEPOSIT, "A1", "20.00",
                Instant.now(), "Withdraw to account A5");
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(Money.of("20.00"), tx));

    }

    @Test
    void withdraw_with_transaction_for_different_account_throws() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        Transaction tx = transactionBuilder(
                "123", TransactionType.WITHDRAW, "A5", "20.00",
                Instant.now(), "Withdraw to account A5");

        assertThrows(IllegalArgumentException.class, () -> account.withdraw(Money.of("20.00"), tx));

    }

    @Test
    void deposit_increasing_balance_and_records_transaction() {

        Account account = accountBuilder("A1", "Erika", "100.00");

        Transaction tx = transactionBuilder(
                "123", TransactionType.DEPOSIT, "A1", "20.00",
                Instant.now(), "Deposit to account A1");


        account.deposit(Money.of("20.00"), tx);
        assertEquals("120.00", account.getBalance().toString());
        assertEquals(1, account.getTransactions().size());

    }

    private Transaction transactionBuilder(String id, TransactionType type, String accountId,
                                           String money, Instant time, String description) {

        return new Transaction(
                id,
                type,
                accountId,
                Money.of(money),
                time,
                description
        );
    }

    private Account accountBuilder(String id, String ownerName, String money) {
        return new Account(
                id,
                ownerName,
                Money.of(money)
        );

    }
}
