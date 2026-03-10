package com.erika.bank.exceptions;

public class InvalidTransferTargetException extends BankingException{

    public InvalidTransferTargetException(String accountId){
        super("The target account cannot be the same as the initiating one" + accountId);
    }
}
