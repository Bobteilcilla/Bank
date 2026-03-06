package com.erika.bank.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AccountStatement {

    private final String accountId;
    private final String ownerName;
    private final Money balance;

    private final int transactionsCount;
    private final Money totalWithdraws;
    private final Money totalDeposits;
    private final List<Transaction> recentTransaction;


}
