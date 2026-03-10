package com.erika.bank.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountTest {

    @Test
    void create_account_initial_balance_null(){

        assertThrows(IllegalArgumentException.class, () -> new Account(
                "A1",
                "Erika",
                null
        ));

    }

    @Test
    void create_account_id_is_blank(){

        assertThrows(IllegalArgumentException.class, () -> new Account(
                "",
                "Erika",
                Money.of("20.00")
        ));

    }

    @Test
    void create_account_owner_name_blank(){

        assertThrows(IllegalArgumentException.class, () -> new Account(
                "A3",
                "",
                Money.of("30.00")
        ));

    }

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
