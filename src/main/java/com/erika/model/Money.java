package com.erika.model;

import java.math.BigDecimal;

public class Money {
    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        if(amount == null){
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if(amount.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public BigDecimal getAmount(){
        return amount;
    }

}
