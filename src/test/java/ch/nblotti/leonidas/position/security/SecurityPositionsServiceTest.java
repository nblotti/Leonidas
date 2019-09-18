package ch.nblotti.leonidas.position.security;


import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.position.PositionRepository;
import ch.nblotti.leonidas.position.cash.CashPositionService;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteService;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.time.format.DateTimeFormatter;

public class SecurityPositionsServiceTest {


  @MockBean
  private PositionRepository repository;

  @MockBean
  SecurityEntryService securityEntryService;
  @Mock
  DateTimeFormatter dateTimeFormatter;


  @MockBean
  AccountService accountService;


  @MockBean
  FXQuoteService fxQuoteService;

  @MockBean
  QuoteService quoteService;


  @MockBean
  JmsTemplate jmsOrderTemplate;


  @TestConfiguration
  static class SecurityPositionServiceTestContextConfiguration {


    @Bean
    public SecurityPositionService securityPositionService() {

      return new SecurityPositionService();

    }
  }



  @Test
  public void test() {

  }

}
