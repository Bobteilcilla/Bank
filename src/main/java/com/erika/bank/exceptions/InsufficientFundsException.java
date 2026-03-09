package com.erika.bank.exceptions;

public class InsufficientFundsException extends BankingException{

    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}
