package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.QuoteService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class CashEntryServiceTest {


  @MockBean
  private MarketProcessService marketProcessService;

  @MockBean
  private CashEntryRepository repository;

  @MockBean
  private JmsTemplate jmsOrderTemplate;

  @MockBean
  private QuoteService quoteService;

  @MockBean
  private FXQuoteService fxQuoteService;


  @MockBean
  private AssetService assetService;

  @MockBean
  private AccountService acountService;


  @TestConfiguration
  static class CashEntryServiceTestContextConfiguration {


    @Bean
    public CashEntryService cashEntryService() {

      return new CashEntryService();

    }
  }

  @Autowired
  CashEntryService cashEntryService;

  @Test
  public void findAll() {

    Iterable<ch.nblotti.leonidas.entry.cash.CashEntryPO> iterable = mock(Iterable.class);

    when(repository.findAll()).thenReturn(iterable);

    Iterable<ch.nblotti.leonidas.entry.cash.CashEntryPO> returnedIterable = cashEntryService.findAll();

    Assert.assertEquals(iterable, returnedIterable);
  }

  @Test
  public void findAllByAccountAndCurrencyOrderByValueDateAsc() {

    Iterable<ch.nblotti.leonidas.entry.cash.CashEntryPO> iterable = mock(Iterable.class);
    int account = 1;
    String currency = "CHF";

    when(repository.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency)).thenReturn(iterable);

    Iterable<ch.nblotti.leonidas.entry.cash.CashEntryPO> returnedIterable = cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
    Assert.assertEquals(iterable, returnedIterable);
  }

  @Test
  public void findByAccountAndOrderID() {

    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryPO = mock(ch.nblotti.leonidas.entry.cash.CashEntryPO.class);
    int account = 1;
    int orderID = 1;

    when(repository.findByAccountAndOrderID(account, orderID)).thenReturn(cashEntryPO);

    ch.nblotti.leonidas.entry.cash.CashEntryPO returnedCashEntryPO = cashEntryService.findByAccountAndOrderID(account, orderID);
    Assert.assertEquals(cashEntryPO, returnedCashEntryPO);
  }

  @Test
  public void findById() {

    Optional<ch.nblotti.leonidas.entry.cash.CashEntryPO> cashEntryPO = mock(Optional.class);
    String cashEntryID = "1";
    when(repository.findById(Long.valueOf(cashEntryID))).thenReturn(cashEntryPO);
    Optional<ch.nblotti.leonidas.entry.cash.CashEntryPO> returnedCashEntryPO = cashEntryService.findById(cashEntryID);
    Assert.assertEquals(cashEntryPO, returnedCashEntryPO);
  }

  @Test
  public void fromMarketOrderSideDbt() {
    OrderPO orderPO = mock(OrderPO.class);
    AccountPO currentAccountPO = mock(AccountPO.class);
    AssetPO assetPO = mock(AssetPO.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryTO = mock(ch.nblotti.leonidas.entry.cash.CashEntryPO.class);
    QuoteDTO fxQquote = mock(QuoteDTO.class);

    long orderID = 1;
    int accountID = 1;
    float orderQty = 1;
    int orderStatus = 1;
    String exchange = "US";
    String symbol = "FB";
    String currency = "USD";
    String performanceCurrency = "CHF";
    String adjustedClose = "2";
    float grossAmount = 5f;
    float exchangeRate = 2f;
    float netAmount = 2f;
    String fxAdjustedClose = "2";

    when(acountService.findAccountById(any())).thenReturn(currentAccountPO);
    when(assetService.getSymbol(any(), anyString())).thenReturn(assetPO);
    when(assetService.getValueDateForExchange(anyString())).thenReturn(3);
    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(fxQquote);
    when(quoteService.getQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(quoteDTO);
    int valueDateForExchange = assetService.getValueDateForExchange(assetPO.getExchange());


    when(orderPO.getAccountId()).thenReturn(accountID);
    when(orderPO.getId()).thenReturn(orderID);
    when(orderPO.getTransactTime()).thenReturn(LocalDate.now());
    when(orderPO.getOrderQtyData()).thenReturn(orderQty);
    when(orderPO.getStatus()).thenReturn(orderStatus);
    when(orderPO.getSide()).thenReturn(DEBIT_CREDIT.CRDT);
    when(orderPO.getExchange()).thenReturn(exchange);
    when(orderPO.getSymbol()).thenReturn(symbol);


    when(assetPO.getExchange()).thenReturn(exchange);
    when(assetPO.getCurrency()).thenReturn(currency);

    when(currentAccountPO.getId()).thenReturn(accountID);
    when(currentAccountPO.getPerformanceCurrency()).thenReturn(performanceCurrency);
    when(quoteDTO.getAdjustedClose()).thenReturn(adjustedClose);
    ;
    when(cashEntryTO.getGrossAmount()).thenReturn(grossAmount);
    when(cashEntryTO.getFxExchangeRate()).thenReturn(exchangeRate);
    when(cashEntryTO.getNetAmount()).thenReturn(netAmount);
    ;
    when(fxQquote.getAdjustedClose()).thenReturn(fxAdjustedClose);
    ;

    ;


    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryPO = cashEntryService.fromMarketOrder(orderPO);

    Assert.assertEquals(1, cashEntryPO.getAccount());
    Assert.assertEquals(1, cashEntryPO.getOrderID());
    Assert.assertEquals(DEBIT_CREDIT.DBIT, cashEntryPO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), cashEntryPO.getEntryDate());
    Assert.assertEquals(LocalDate.now().plusDays(3), cashEntryPO.getValueDate());
    Assert.assertEquals(currency, cashEntryPO.getCurrency());
    Assert.assertEquals(Float.valueOf(4), cashEntryPO.getEntryValueReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getNetAmount());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getGrossAmount());
    Assert.assertEquals("CHF", cashEntryPO.getAccountReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getFxExchangeRate());
    Assert.assertEquals(1, cashEntryPO.getStatus());
  }


  @Test
  public void fromMarketOrderSideCrdt() {
    OrderPO orderPO = mock(OrderPO.class);
    AccountPO currentAccountPO = mock(AccountPO.class);
    AssetPO assetPO = mock(AssetPO.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryTO = mock(ch.nblotti.leonidas.entry.cash.CashEntryPO.class);
    QuoteDTO fxQquote = mock(QuoteDTO.class);

    long orderID = 1;
    int accountID = 1;
    float orderQty = 1;
    int orderStatus = 1;
    String exchange = "US";
    String symbol = "FB";
    String currency = "USD";
    String performanceCurrency = "CHF";
    String adjustedClose = "2";
    float grossAmount = 5f;
    float exchangeRate = 2f;
    float netAmount = 2f;
    String fxAdjustedClose = "2";

    when(acountService.findAccountById(any())).thenReturn(currentAccountPO);
    when(assetService.getSymbol(any(), anyString())).thenReturn(assetPO);
    when(assetService.getValueDateForExchange(anyString())).thenReturn(3);
    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(fxQquote);
    when(quoteService.getQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(quoteDTO);
    int valueDateForExchange = assetService.getValueDateForExchange(assetPO.getExchange());


    when(orderPO.getAccountId()).thenReturn(accountID);
    when(orderPO.getId()).thenReturn(orderID);
    when(orderPO.getTransactTime()).thenReturn(LocalDate.now());
    when(orderPO.getOrderQtyData()).thenReturn(orderQty);
    when(orderPO.getStatus()).thenReturn(orderStatus);
    when(orderPO.getSide()).thenReturn(DEBIT_CREDIT.DBIT);
    when(orderPO.getExchange()).thenReturn(exchange);
    when(orderPO.getSymbol()).thenReturn(symbol);


    when(assetPO.getExchange()).thenReturn(exchange);
    when(assetPO.getCurrency()).thenReturn(currency);

    when(currentAccountPO.getId()).thenReturn(accountID);
    when(currentAccountPO.getPerformanceCurrency()).thenReturn(performanceCurrency);
    when(quoteDTO.getAdjustedClose()).thenReturn(adjustedClose);
    ;
    when(cashEntryTO.getGrossAmount()).thenReturn(grossAmount);
    when(cashEntryTO.getFxExchangeRate()).thenReturn(exchangeRate);
    when(cashEntryTO.getNetAmount()).thenReturn(netAmount);
    ;
    when(fxQquote.getAdjustedClose()).thenReturn(fxAdjustedClose);
    ;

    ;


    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryPO = cashEntryService.fromMarketOrder(orderPO);

    Assert.assertEquals(1, cashEntryPO.getAccount());
    Assert.assertEquals(1, cashEntryPO.getOrderID());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, cashEntryPO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), cashEntryPO.getEntryDate());
    Assert.assertEquals(LocalDate.now().plusDays(3), cashEntryPO.getValueDate());
    Assert.assertEquals(currency, cashEntryPO.getCurrency());
    Assert.assertEquals(Float.valueOf(4), cashEntryPO.getEntryValueReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getNetAmount());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getGrossAmount());
    Assert.assertEquals("CHF", cashEntryPO.getAccountReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getFxExchangeRate());
    Assert.assertEquals(1, cashEntryPO.getStatus());
  }

  @Test
  public void fromCashEntryOrder() {

    OrderPO orderPO = mock(OrderPO.class);
    AccountPO currentAccountPO = mock(AccountPO.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);

    when(orderPO.getAccountId()).thenReturn(1);
    when(acountService.findAccountById(orderPO.getAccountId())).thenReturn(currentAccountPO);
    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(quoteDTO);


    when(currentAccountPO.getId()).thenReturn((1));
    when(orderPO.getId()).thenReturn(1l);
    when(orderPO.getTransactTime()).thenReturn(LocalDate.now());

    when(orderPO.getStatus()).thenReturn(1);

    when(orderPO.getSide()).thenReturn(DEBIT_CREDIT.CRDT);
    when(orderPO.getTransactTime()).thenReturn(LocalDate.now());
    when(orderPO.getAmount()).thenReturn(2f);
    when(orderPO.getCashCurrency()).thenReturn("CHF");
    when(quoteDTO.getAdjustedClose()).thenReturn("2");
    when(currentAccountPO.getPerformanceCurrency()).thenReturn("CHF");


    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryPO = cashEntryService.fromCashEntryOrder(orderPO);

    Assert.assertEquals(1, cashEntryPO.getAccount());
    Assert.assertEquals(1, cashEntryPO.getOrderID());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, cashEntryPO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), cashEntryPO.getEntryDate());
    Assert.assertEquals(LocalDate.now().plusDays(3), cashEntryPO.getValueDate());
    Assert.assertEquals("CHF", cashEntryPO.getCurrency());
    Assert.assertEquals(Float.valueOf(4), cashEntryPO.getEntryValueReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getNetAmount());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getGrossAmount());
    Assert.assertEquals("CHF", cashEntryPO.getAccountReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getFxExchangeRate());
    Assert.assertEquals(1, cashEntryPO.getStatus());

  }


  @Test
  public void save() {

    ch.nblotti.leonidas.entry.cash.CashEntryPO cashEntryPO = mock(ch.nblotti.leonidas.entry.cash.CashEntryPO.class);
    Logger logger = mock(Logger.class);


    CashEntryService spyCashEntryService = spy(cashEntryService);
    doReturn(logger).when(spyCashEntryService).getLogger();
    when(logger.isLoggable(any())).thenReturn(Boolean.TRUE);
    when(repository.save(any())).thenReturn(cashEntryPO);
    when(cashEntryPO.getOrderID()).thenReturn(1l);
    when(cashEntryPO.getAccount()).thenReturn(1);

    ch.nblotti.leonidas.entry.cash.CashEntryPO returned = spyCashEntryService.save(cashEntryPO);

    verify(marketProcessService, times(1)).setCashEntryRunningForProcess(1, 1);
    verify(jmsOrderTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    verify(logger, times(1)).fine(anyString());

    Assert.assertEquals(cashEntryPO, returned);
  }

  @Test
  public void saveNoLogger() {

    CashEntryPO cashEntryPO = mock(CashEntryPO.class);
    Logger logger = mock(Logger.class);


    CashEntryService spyCashEntryService = spy(cashEntryService);
    doReturn(logger).when(spyCashEntryService).getLogger();
    when(logger.isLoggable(any())).thenReturn(Boolean.FALSE);
    when(repository.save(any())).thenReturn(cashEntryPO);
    when(cashEntryPO.getOrderID()).thenReturn(1l);
    when(cashEntryPO.getAccount()).thenReturn(1);

    CashEntryPO returned = spyCashEntryService.save(cashEntryPO);

    verify(marketProcessService, times(1)).setCashEntryRunningForProcess(1, 1);
    verify(jmsOrderTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    verify(logger, times(0)).fine(anyString());

    Assert.assertEquals(cashEntryPO, returned);
  }


  @Test
  public void getLogger() {
    Logger returned = cashEntryService.getLogger();
    Assert.assertEquals("CashEntryService", returned.getName());

  }


}
