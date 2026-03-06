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
    private final List<Transaction> recentTransactions;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Account Statement\n");
        sb.append("-----------------\n");
        sb.append("Account ID: ").append(accountId).append("\n");
        sb.append("Owner: ").append(ownerName).append("\n");
        sb.append("Balance: ").append(balance).append("\n\n");

        sb.append("Summary\n");
        sb.append("-------\n");
        sb.append("Transactions: ").append(transactionsCount).append("\n");
        sb.append("Total Deposits: ").append(totalDeposits).append("\n");
        sb.append("Total Withdrawals: ").append(totalWithdraws).append("\n\n");

        sb.append("Transactions\n");
        sb.append("------------\n");

        for (Transaction tx : recentTransactions) {
            sb.append(tx.getTimestamp())
                    .append(" | ")
                    .append(tx.getType())
                    .append(" | ")
                    .append(tx.getAmount())
                    .append("\n");
        }

        return sb.toString();
    }
}
