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

    private final int transactionCount;
    private final Money totalWithdrawals;
    private final Money totalDeposits;
    private final List<Transaction> transactions;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Account Statement\n")
                .append("-----------------\n")
                .append("Account ID: ").append(accountId).append("\n")
                .append("Owner: ").append(ownerName).append("\n")
                .append("Balance: ").append(balance).append("\n\n");

        sb.append("Summary\n")
                .append("-------\n")
                .append("Transactions: ").append(transactionCount).append("\n")
                .append("Total Deposits: ").append(totalDeposits).append("\n")
                .append("Total Withdrawals: ").append(totalWithdrawals).append("\n\n");

        sb.append("Transactions\n")
                .append("------------\n");

        if (transactions != null) {
            for (Transaction tx : transactions) {
                sb.append(tx).append("\n");
            }
        }

        return sb.toString();
    }
}
