package com.erika.bank.exceptions;

import com.erika.bank.model.Money;

public class InvalidAmountException extends BankingException{

    public InvalidAmountException(String operation){
            super(operation + " amount must be greater than zero");
    }
}
