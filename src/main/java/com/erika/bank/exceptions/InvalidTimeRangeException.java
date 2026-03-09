package com.erika.bank.exceptions;

import java.time.Instant;

public class InvalidTimeRangeException extends BankingException{

    public InvalidTimeRangeException(String time){
        super("Invalid time range: " + time);
    }
}
