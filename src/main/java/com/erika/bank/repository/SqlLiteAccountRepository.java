package com.erika.bank.repository;

import com.erika.bank.model.Account;
import com.erika.bank.model.Money;
import com.erika.bank.model.Transaction;
import com.erika.bank.model.TransactionType;


import java.sql.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SqlLiteAccountRepository implements AccountRepository {

    private final String jdbcUrl;

    public SqlLiteAccountRepository(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("SQLite path or JDBC URL cannot be null/blank");
        }
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            jdbcUrl = "jdbc:sqlite:" + jdbcUrl;
        }
        this.jdbcUrl = jdbcUrl;
        initSchema();
    }

    private void initSchema() {

        String createAccountsSql = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id TEXT PRIMARY KEY,
                    owner_name TEXT NOT NULL
                )
                """;

        String createTransactionsSql = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id TEXT PRIMARY KEY,
                    account_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    amount TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    description TEXT,
                    tx_order INTEGER NOT NULL,
                    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
                    UNIQUE (account_id, tx_order)
                )
                """;

        String createOrderIndexSql = """
                CREATE INDEX IF NOT EXISTS idx_transactions_account_order
                ON transactions(account_id, tx_order)
                """;

        try (Connection connection = openConnection();
             Statement st = connection.createStatement()) {
            st.execute(createAccountsSql);
            st.execute(createTransactionsSql);
            st.execute(createOrderIndexSql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize SQLite schema", e);
        }

    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    @Override
    public Optional<Account> findById(String id) {
        if (id == null)
            return Optional.empty();

        String existsByIdStatement = "SELECT id, owner_name FROM accounts WHERE id = ? LIMIT 1";

        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(existsByIdStatement)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Account account = new Account(
                        rs.getString("id"),
                        rs.getString("owner_name"),
                        Money.of("0.00")
                );

                loadTransactionsForAccount(connection, account);
                return Optional.of(account);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find account: " + id, e);
        }
    }

    @Override
    public void save(Account account) {

        if (account == null) {
            throw new IllegalArgumentException("The account cannot be null");
        }

        String upsertAccountSql = """
                INSERT INTO accounts (id, owner_name)
                VALUES (?,?)
                ON CONFLICT(id) DO UPDATE SET owner_name = excluded.owner_name
                """;

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(upsertAccountSql)) {

                    ps.setString(1, account.getId());
                    ps.setString(2, account.getOwnerName());
                    ps.executeUpdate();
                }
                replaceTransactionsForAccount(connection, account);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save the account: " + account.getId(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null)
            return false;

        String existsByIdStatement = "SELECT 1 FROM accounts WHERE id = ? LIMIT 1";

        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(existsByIdStatement)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check account existence: " + id, e);
        }
    }

    @Override
    public List<Account> findAll() {

        List<Account> accounts = new ArrayList<>();

        String allAccountsStatement = "SELECT id, owner_name FROM accounts";

        try (Connection connection = openConnection();
             Statement st = connection.createStatement()) {

            try (ResultSet rs = st.executeQuery(allAccountsStatement)) {
                while (rs.next()) {

                    Account account = new Account(
                            rs.getString("id"),
                            rs.getString("owner_name"),
                            Money.of("0.00")
                    );
                    loadTransactionsForAccount(connection, account);
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load accounts", e);
        }
        return accounts;
    }

    private void loadTransactionsForAccount(Connection connection, Account account) throws SQLException {

        String loadTransactions = """
                SELECT id, account_id, type, amount, timestamp, description FROM transactions 
                WHERE account_id = ? ORDER BY tx_order ASC
                """;

        try (PreparedStatement ps = connection.prepareStatement(loadTransactions)) {
            ps.setString(1, account.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction(
                            rs.getString("id"),
                            TransactionType.valueOf(rs.getString("type")),
                            rs.getString("account_id"),
                            Money.of(rs.getString("amount")),
                            Instant.parse(rs.getString("timestamp")),
                            rs.getString("description")
                    );

                    if (tx.getType() == TransactionType.DEPOSIT) {
                        account.deposit(tx.getAmount(), tx);
                    } else {
                        account.withdraw(tx.getAmount(), tx);
                    }
                }
            }
        }
    }

    private void replaceTransactionsForAccount(Connection connection, Account account) throws SQLException {

        String deleteTransactionsSQL = """
                DELETE FROM transactions WHERE account_id = ?
                """;

        String insertTransactionsSQL = """
                INSERT INTO transactions (id, account_id, type, amount, timestamp, description, tx_order)
                VALUES (?,?,?,?,?,?,?)
                """;

        try (PreparedStatement delete = connection.prepareStatement(deleteTransactionsSQL)) {
            delete.setString(1, account.getId());
            delete.executeUpdate();

            try (PreparedStatement insert = connection.prepareStatement(insertTransactionsSQL)) {
                int order = 0;
                for (Transaction tx : account.getTransactions()) {
                    insert.setString(1, tx.getId());
                    insert.setString(2, tx.getAccountId());
                    insert.setString(3, tx.getType().name());
                    insert.setString(4, tx.getAmount().toString());
                    insert.setString(5, tx.getTimestamp().toString());
                    insert.setString(6, tx.getDescription());
                    insert.setInt(7, order++);
                    insert.addBatch();
                }
                insert.executeBatch();
            }

        }
    }
}


