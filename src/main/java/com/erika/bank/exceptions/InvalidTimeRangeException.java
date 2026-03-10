package com.erika.bank.exceptions;

public class InvalidTimeRangeException extends BankingException{

    public InvalidTimeRangeException(String time){
        super("Invalid time range: " + time);
    }
}
