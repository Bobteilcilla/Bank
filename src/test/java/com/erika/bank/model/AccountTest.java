package com.erika.bank.model;

import org.junit.jupiter.api.Test;

public class AccountTest {

    @Test
    void deposit_increasing_balance_and_records_transaction() {
        Account a = new Account("A1","Erica", Money.of("100.00"));


    }
}
