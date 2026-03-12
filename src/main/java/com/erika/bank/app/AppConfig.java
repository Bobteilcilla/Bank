package com.erika.bank.app;

import com.erika.bank.repository.AccountRepository;
import com.erika.bank.repository.SqlLiteAccountRepository;
import com.erika.bank.service.BankingService;
import com.erika.bank.service.BankingServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public AccountRepository accountRepository(){
        return new SqlLiteAccountRepository("bank.db");
    }

    @Bean
    public BankingService bankingService(AccountRepository repo, Clock clock){
        return new BankingServiceImpl(repo, clock);
    }

}
