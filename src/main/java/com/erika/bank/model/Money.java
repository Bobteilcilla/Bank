package com.erika.bank.model;

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

    public Money add(Money other) {
        if (other == null){
            throw new IllegalArgumentException("Money to add cannot be null");
        }
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other){
        if (other == null){
            throw new IllegalArgumentException("Money to subtract cannot be null");
        }
        return new Money(this.amount.subtract(other.amount));
    }

    @Override
    public String toString(){
        return amount.toString();
    }

    public int compareTo(Money other) {
        if (other == null ) throw new IllegalArgumentException("Other money cannot be null");
        return this.amount.compareTo(other.amount);
    }

    public boolean isGreaterThan(Money other) {
        if (other == null ) {
            throw new IllegalArgumentException("Other money cannot be null");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        if (other == null ) {
            throw new IllegalArgumentException("Other money cannot be null");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterOrEqual(Money other) {
        if (other == null ) {
            throw new IllegalArgumentException("Other money cannot be null");
        }
        return this.amount.compareTo(other.amount) >= 0;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Money other)) return false;
        //compareTo ignore scale differences
        return this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        //normalize scale for consistent hashing
        return this.amount.stripTrailingZeros().hashCode();
    }



}
