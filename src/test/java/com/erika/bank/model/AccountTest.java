package com.erika.bank.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountTest {

    @Test
    void deposit_increasing_balance_and_records_transaction() {
        Account account = new Account(
                "A1",
                "Erica",
                Money.of("100.00")
        );

        Transaction tx = new Transaction(
                "123",
                TransactionType.DEPOSIT,
                "A1",
                Money.of("20.00"),
                Instant.now(),
                "Deposit to account A1"
        );

        account.deposit(Money.of("20.00"), tx);
        assertEquals("120.00", account.getBalance().toString());
        assertEquals(1, account.getTransactions().size());

    }
}
