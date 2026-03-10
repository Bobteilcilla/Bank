package com.erika.bank.exceptions;

public class InvalidAmountException extends BankingException{

    public InvalidAmountException(String operation){
            super(operation + " amount must be greater than zero");
    }
}
