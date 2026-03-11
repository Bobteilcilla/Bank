package com.erika.bank.repository;

import com.erika.bank.exceptions.AccountNotFoundException;
import com.erika.bank.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SqlLiteAccountRepositoryTest {

    @TempDir
    private Path tempDir;

    private Path dbFile;

    private SqlLiteAccountRepository repo;
    private Clock clock;

    @BeforeEach
    void setUp() throws Exception {
        clock = Clock.fixed(Instant.parse("2026-03-09T10:00:00Z"), ZoneOffset.UTC);
        dbFile = Files.createTempFile(tempDir, "bank-", ".db");
        repo = new SqlLiteAccountRepository(dbFile.toString());
    }

    private SqlLiteAccountRepository reloadRepo() {
        return new SqlLiteAccountRepository(dbFile.toString());
    }

    @Test
    void existsById_returns_true_after_save_and_false_for_missing(){

        assertFalse(repo.existsById("missing"));

        Account account = new Account("A1", "Erika", Money.of("0.00"));
        repo.save(account);

        assertTrue(repo.existsById("A1"));
    }

    @Test
    void save_and_findById_persists_account_and_transactions() {

        Account account = new Account("A1", "Erika", Money.of("0.00"));

        Transaction tx = new Transaction(
                "123",
                TransactionType.DEPOSIT,
                "A1",
                Money.of("50.00"),
                Instant.now(clock),
                "Deposit of 50.00 to Account A1"

        );
        account.deposit(Money.of("50.00"),tx);
        repo.save(account);

        Optional<Account> loaded = repo.findById("A1");

        assertTrue(loaded.isPresent());

        Account stored = loaded.orElseThrow(() -> new AssertionError("Account A1 should exist"));
        assertEquals("A1", stored.getId());
        assertEquals("Erika", stored.getOwnerName());
        assertEquals(Money.of("50.00"), stored.getBalance());
        assertEquals(1, stored.getTransactions().size());
        assertEquals(TransactionType.DEPOSIT, stored.getTransactions().get(0).getType());
        assertEquals(Money.of("50.00"), stored.getTransactions().get(0).getAmount());

        // To fully prove DB Persistence
        SqlLiteAccountRepository reloaded = reloadRepo();
        Optional<Account> reloadById = reloaded.findById("A1");
        assertTrue(reloadById.isPresent());

    }

    @Test
    void findAll_returns_all_accounts_with_reconstructed_balances(){

        Account account1 = new Account("A1", "Erika", Money.of("0.00"));

        Transaction tx1 = new Transaction(
                "123",
                TransactionType.DEPOSIT,
                "A1",
                Money.of("150.00"),
                Instant.now(clock),
                "Deposit of 150.00 to Account A1"

        );

        Transaction tx2 = new Transaction(
                "133",
                TransactionType.DEPOSIT,
                "A1",
                Money.of("60.00"),
                Instant.now(clock),
                "Deposit of 60.00 to Account A1"

        );

        account1.deposit(Money.of("150.00"),tx1);
        account1.deposit(Money.of("60.00"),tx2);
        repo.save(account1);


        Account account2 = new Account("A2", "Manuel", Money.of("0.00"));

        Transaction tx12 = new Transaction(
                "126",
                TransactionType.DEPOSIT,
                "A2",
                Money.of("50.00"),
                Instant.now(clock),
                "Deposit of 50.00 to Account A1"

        );

        account2.deposit(Money.of("50.00"),tx12);
        repo.save(account2);


        // Now asserting

        Optional<Account> loaded1 = repo.findById("A1");
        assertTrue(loaded1.isPresent());

        Optional<Account> loaded2 = repo.findById("A2");
        assertTrue(loaded2.isPresent());

        SqlLiteAccountRepository reloaded = reloadRepo();
        List<Account> all = reloaded.findAll();

        Account loadedA1 = all.stream().filter(a -> a.getId().equals("A1")).findFirst().orElseThrow();
        Account loadedA2 = all.stream().filter(a -> a.getId().equals("A2")).findFirst().orElseThrow();

        assertEquals(Money.of("210.00"), loadedA1.getBalance());
        assertEquals(2, loadedA1.getTransactions().size());

        assertEquals(Money.of("50.00"), loadedA2.getBalance());
        assertEquals(1, loadedA2.getTransactions().size());

    }

    @Test
    void save_overwrites_transactions_for_same_account_snapshot() {

        Account account = new Account("A1", "Erika", Money.of("0.00"));

        Transaction tx1 = new Transaction(
                "tx1",
                TransactionType.DEPOSIT,
                "A1",
                Money.of("100.00"),
                Instant.now(clock),
                "Deposit of 100.00 to Account A1"

        );

        Transaction tx2 = new Transaction(
                "tx2",
                TransactionType.DEPOSIT,
                "A1",
                Money.of("50.00"),
                Instant.now(clock),
                "Deposit of 50.00 to Account A1"

        );

        account.deposit(Money.of("100.00"), tx1);
        account.deposit(Money.of("50.00"), tx2);

        repo.save(account);

        Transaction tx3 = new Transaction(
                "tx3",
                TransactionType.WITHDRAW,
                "A1",
                Money.of("20.00"),
                Instant.now(clock),
                "Deposit of 20.00 to Account A1"

        );

        account.withdraw(Money.of("20.00"), tx3);
        repo.save(account);

        SqlLiteAccountRepository reload = reloadRepo();

        Optional<Account> ac1 = reload.findById("A1");
        assertTrue(ac1.isPresent());
        Account ac = ac1.orElseThrow(() -> new AssertionError("Account A1 should exist"));

        assertEquals( Money.of("130.00"), ac.getBalance());
        assertEquals(3, ac.getTransactions().size());
        assertEquals("tx1", ac.getTransactions().get(0).getId());
        assertEquals("tx2", ac.getTransactions().get(1).getId());
        assertEquals("tx3", ac.getTransactions().get(2).getId());
    }

}
