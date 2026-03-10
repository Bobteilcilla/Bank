package com.erika.bank.service;


import com.erika.bank.exceptions.AccountNotFoundException;
import com.erika.bank.exceptions.InsufficientFundsException;
import com.erika.bank.exceptions.InvalidAmountException;
import com.erika.bank.exceptions.InvalidTimeRangeException;
import com.erika.bank.exceptions.InvalidTransferTargetException;
import com.erika.bank.model.AccountStatement;
import com.erika.bank.model.Money;
import com.erika.bank.model.TransactionType;
import com.erika.bank.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class BankingServiceImplTest {

    private BankingService service;
    private Clock clock;

    @BeforeEach
    void setUp() {

        clock = Clock.fixed(Instant.parse("2026-03-09T10:00:00Z"), ZoneOffset.UTC);
        service = new BankingServiceImpl(new InMemoryAccountRepository(), clock);
    }

    @Test
    void create_account_invalid_inputs() {

        assertAll("create account invalid inputs",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.createAccount(null, Money.of("0.00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.createAccount("", Money.of("0.00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.createAccount("Erica", null))
        );
    }

    @Test
    void create_account_owner_name_is_blank() {

        assertThrows(IllegalArgumentException.class, () -> service.createAccount(
                " ",
                Money.of("30.00")
        ));

    }

    @Test
    void create_account_sets_id_and_initial_balance() {

        String id = service.createAccount("Erika", Money.of("0.00"));

        assertNotNull(id);
        assertFalse(id.isBlank());
        assertEquals(Money.of("0.00"), service.getBalance(id));
        assertEquals(0, service.getTransactions(id).size());
    }

    @Test
    void create_account_with_initial_deposit_records_transaction() {

        String id = service.createAccount("Erika", Money.of("100.00"));

        var transactions = service.getTransactions(id);

        assertEquals(1, transactions.size());

        var tx = transactions.get(0);

        assertEquals(id, tx.getAccountId());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(Money.of("100.00"), tx.getAmount());
        assertEquals(Money.of("100.00"), service.getBalance(id));
    }

    @Test
    void deposit_records_correct_transaction_with_no_funds() {

        String id = service.createAccount("Maria", Money.of("0.00"));

        service.deposit(id, Money.of("50.00"));

        var transactions = service.getTransactions(id);

        assertEquals(1, transactions.size());

        var tx = transactions.get(0);

        assertEquals(id, tx.getAccountId());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(Money.of("50.00"), tx.getAmount());
        assertEquals("Deposit to account " + id, tx.getDescription());
        assertEquals(Money.of("50.00"), service.getBalance(id));
    }

    @Test
    void deposit_records_correct_transaction_with_funds() {

        String id = service.createAccount("Maria", Money.of("100.00"));

        service.deposit(id, Money.of("50.00"));

        var transactions = service.getTransactions(id);

        assertEquals(2, transactions.size());

        var tx = transactions.get(1); // second transaction

        assertEquals(id, tx.getAccountId());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(Money.of("50.00"), tx.getAmount());
    }

    @Test
    void deposit_increases_balance_and_records_transaction() {

        String id = service.createAccount("Paco", Money.of("0.00"));

        service.deposit(id, Money.of("50.00"));

        assertEquals(Money.of("50.00"), service.getBalance(id));
        assertEquals(1, service.getTransactions(id).size());

    }

    @ParameterizedTest
    @CsvSource({
            "100.00, 50.00, 50.00",
            "100.00, 20.00, 80.00",
            "200.00, 100.00, 100.00"
    })
    void withdraw_decreases_balance_and_records_transaction(
            String initialBalance,
            String withdrawAmount,
            String expectedBalance
    ) {

        String id = service.createAccount("Paco", Money.of(initialBalance));

        service.withdraw(id, Money.of(withdrawAmount));

        assertEquals(Money.of(expectedBalance), service.getBalance(id));
        assertEquals(2, service.getTransactions(id).size());

        var tx = service.getTransactions(id).get(1);
        assertEquals("Withdraw from account " + id, tx.getDescription());

    }

    @Test
    void withdraw_more_than_balance_throws() {

        String id = service.createAccount("Paco", Money.of("30.00"));

        assertThrows(InsufficientFundsException.class, () -> service.withdraw(id, Money.of("50.00")));
    }

    @Test
    void deposit_zero_throws_invalid_amount() {

        String id = service.createAccount("Erika", Money.of("0.00"));

        assertThrows(InvalidAmountException.class, () -> service.deposit(id, Money.of("0.00")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "-10.00"})
    void deposit_negative_amount_fails_in_money_constructor(String amount) {
        assertThrows(IllegalArgumentException.class, () -> service.deposit("any", Money.of(amount)));
    }

    @Test
    void transfer_to_same_account_throws() {

        String accountId = service.createAccount("Erika", Money.of("20.00"));

        assertThrows(InvalidTransferTargetException.class, () -> service.transfer(accountId, accountId, Money.of("10.00")));
    }

    @Test
    void transfer_more_than_balance_throws() {

        String fromAccountId = service.createAccount("Erika", Money.of("100.00"));
        String toAccountId = service.createAccount("Manolo", Money.of("50.00"));

        assertThrows(InsufficientFundsException.class, () -> service.transfer(fromAccountId, toAccountId, Money.of("200.00")));
    }

    @Test
    void transfer_moves_money_between_accounts_and_records_transactions() {

        String fromAccountId = service.createAccount("Erika", Money.of("100.00"));
        String toAccountId = service.createAccount("Manolo", Money.of("0.00"));

        service.transfer(fromAccountId, toAccountId, Money.of("20.00"));

        assertEquals(Money.of("20.00"), service.getBalance(toAccountId));
        assertEquals(Money.of("80.00"), service.getBalance(fromAccountId));

        var fromTransactions = service.getTransactions(fromAccountId);
        var toTransactions = service.getTransactions(toAccountId);

        assertEquals(2, fromTransactions.size());
        assertEquals(1, toTransactions.size());

        var withdrawTx = fromTransactions.get(1);
        var depositTx = toTransactions.get(0);

        assertEquals(TransactionType.WITHDRAW, withdrawTx.getType());
        assertEquals(fromAccountId, withdrawTx.getAccountId());
        assertEquals(Money.of("20.00"), withdrawTx.getAmount());
        assertEquals("Withdraw from account " + fromAccountId, withdrawTx.getDescription());

        assertEquals(TransactionType.DEPOSIT, depositTx.getType());
        assertEquals(toAccountId, depositTx.getAccountId());
        assertEquals(Money.of("20.00"), depositTx.getAmount());

    }

    @Test
    void withdraw_zero_throws_invalid_amount() {

        String id = service.createAccount("Erika", Money.of("40.00"));

        assertThrows(InvalidAmountException.class, () -> service.withdraw(id, Money.of("0.00")));

    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "-10.00"})
    void withdraw_negative_amount_fails_in_money_constructor(String amount) {
        assertThrows(IllegalArgumentException.class, () -> service.withdraw("any", Money.of(amount)));
    }

    @Test
    void get_balance_for_unknown_account_throws() {

        assertThrows(AccountNotFoundException.class,
                () -> service.getBalance("unknown-account"));
    }

    @Test
    void deposit_to_unknown_account_throws() {

        assertThrows(AccountNotFoundException.class,
                () -> service.deposit("unknown-account", Money.of("10.00")));
    }

    @Test
    void transfer_with_null_source_account_throws() {

        String toAccountId = service.createAccount("Manolo", Money.of("50.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(null, toAccountId, Money.of("10.00")));
    }

    @Test
    void transfer_with_null_target_account_throws() {

        String fromAccountId = service.createAccount("Erika", Money.of("50.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(fromAccountId, null, Money.of("10.00")));
    }

    @Test
    void get_transactions_from_last_days_zero_returns_empty_list() {
        String id = service.createAccount("Erika", Money.of("50.00"));

        assertTrue(service.getTransactionsFromLastDays(id, 0).isEmpty());
    }

    @Test
    void get_transactions_from_last_days_includes_transaction_exactly_at_cutoff() {
        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        BankingService writer = createServiceAt(repo, Instant.parse("2026-03-08T10:00:00Z"));

        String id = writer.createAccount("Erika", Money.of("10.00")); // creates 1 deposit tx at txTime

        BankingService reader = createServiceAt(repo, Instant.parse("2026-03-09T10:00:00Z"));

        var result = reader.getTransactionsFromLastDays(id, 1);

        assertEquals(1, result.size()); // included at exact cutoff
    }

    @Test
    void get_transactions_between_includes_transaction_exactly_at_from_time() {
        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        Instant fromTime = Instant.parse("2026-03-08T10:00:01Z");
        BankingService writer = createServiceAt(repo, fromTime);

        String id = writer.createAccount("Erika", Money.of("10.00")); // creates 1 deposit tx at txTime

        Instant toTime = Instant.parse("2026-03-09T10:00:00Z"); // exactly +1 day
        BankingService reader = createServiceAt(repo, toTime);

        var result = reader.getTransactionsBetween(id, fromTime, toTime);

        assertEquals(1, result.size()); // included at exact cutoff
    }

    @Test
    void get_transactions_between_includes_transaction_exactly_at_to_time() {
        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        Instant fromTime = Instant.parse("2026-03-08T10:00:00Z");
        Instant toTime = Instant.parse("2026-03-09T10:00:00Z");

        BankingService writer = createServiceAt(repo, toTime);

        String id = writer.createAccount("Erika", Money.of("10.00")); // creates 1 deposit tx at txTime

        var result = writer.getTransactionsBetween(id, fromTime, toTime);

        assertEquals(1, result.size()); // included at exact cutoff
    }

    @Test
    void get_transactions_from_last_days_excludes_transaction_before_cutoff() {
        InMemoryAccountRepository repo = new InMemoryAccountRepository();

        BankingService writer = createServiceAt(repo, Instant.parse("2026-03-08T09:59:59Z"));

        String id = writer.createAccount("Erika", Money.of("10.00")); // creates 1 deposit tx at txTime

        BankingService reader = createServiceAt(repo, Instant.parse("2026-03-09T10:00:00Z"));

        var result = reader.getTransactionsFromLastDays(id, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void get_transactions_per_type_returns_only_deposits() {
        String fromId = service.createAccount("Erika", Money.of("100.00"));

        service.deposit(fromId, Money.of("50.00"));
        service.withdraw(fromId, Money.of("20.00"));

        var deposits = service.getTransactionsPerType(fromId, TransactionType.DEPOSIT);

        assertEquals(2, deposits.size());
        assertTrue(deposits.stream().allMatch(tx -> tx.getType() == TransactionType.DEPOSIT));
    }

    @Test
    void get_transactions_per_type_returns_only_withdrawals() {
        String fromId = service.createAccount("Erika", Money.of("100.00"));
        String toId = service.createAccount("Sebastian", Money.of("0.00"));

        service.withdraw(fromId, Money.of("20.00"));
        service.transfer(fromId, toId, Money.of("10.00"));

        var withdrawals = service.getTransactionsPerType(fromId, TransactionType.WITHDRAW);

        assertEquals(2, withdrawals.size());
        assertTrue(withdrawals.stream().allMatch(tx -> tx.getType() == TransactionType.WITHDRAW));
    }

    @Test
    void get_transactions_per_type_with_null_type_throws() {
        String accountId = service.createAccount("Erika", Money.of("100.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.getTransactionsPerType(accountId, null));
    }

    @Test
    void get_transactions_between_invalid_ranges_throw() {
        String accountId = service.createAccount("Erika", Money.of("10.00"));
        Instant now = Instant.now(clock);

        assertAll("invalid time ranges",
                () -> assertThrows(InvalidTimeRangeException.class,
                        () -> service.getTransactionsBetween(accountId, null, now)),
                () -> assertThrows(InvalidTimeRangeException.class,
                        () -> service.getTransactionsBetween(accountId, now, null)),
                () -> assertThrows(InvalidTimeRangeException.class,
                        () -> service.getTransactionsBetween(accountId, now.plusSeconds(1), now))
        );
    }

    @Test
    void account_statement_totals_are_correct_for_mixed_flow() {

        String fromId = service.createAccount("Erika", Money.of("100.00")); // creates 1 deposit tx at txTime

        String toId =  service.createAccount("Sebastian", Money.of("50.00"));

        service.deposit(fromId, Money.of("50.00"));
        service.deposit(fromId, Money.of("30.00"));

        service.withdraw(fromId, Money.of("20.00"));

        service.transfer(fromId, toId, Money.of("10.00"));

        AccountStatement statement = service.getAccountStatement(fromId);

        assertEquals(5, service.getTransactions(fromId).size());
        assertEquals(Money.of("180.00"),statement.getTotalDeposits());
        assertEquals(Money.of("30.00"),statement.getTotalWithdrawals());
        assertEquals(Money.of("150.00"), service.getBalance(fromId));

    }

    @Test
    void account_statement_transactions_are_unmodifiable() {
        String id = service.createAccount("Erika", Money.of("10.00"));
        service.deposit(id, Money.of("5.00"));
        AccountStatement statement = service.getAccountStatement(id);

        assertThrows(UnsupportedOperationException.class, () -> statement.getTransactions().clear());
    }

    private BankingService createServiceAt(InMemoryAccountRepository repo, Instant instant){
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        return new BankingServiceImpl(repo, clock);
    }
}
