package com.erika.bank.exceptions;

public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
    }

}