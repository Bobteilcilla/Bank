package com.erika.bank.exceptions;

public class InvalidTransferTarget extends BankingException{

    public InvalidTransferTarget(String accountId){
        super("The target account cannot be the same as the initiating one");
    }
}
