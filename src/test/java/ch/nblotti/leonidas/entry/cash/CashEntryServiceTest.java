package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.QuoteService;
import org.hibernate.criterion.Order;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    Iterable<CashEntryPO> iterable = mock(Iterable.class);

    when(repository.findAll()).thenReturn(iterable);

    Iterable<CashEntryPO> returnedIterable = cashEntryService.findAll();

    Assert.assertEquals(iterable, returnedIterable);
  }

  @Test
  public void findAllByAccountAndCurrencyOrderByValueDateAsc() {

    Iterable<CashEntryPO> iterable = mock(Iterable.class);
    int account = 1;
    String currency = "CHF";

    when(repository.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency)).thenReturn(iterable);

    Iterable<CashEntryPO> returnedIterable = cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
    Assert.assertEquals(iterable, returnedIterable);
  }

  @Test
  public void findByAccountAndOrderID() {

    CashEntryPO cashEntryPO = mock(CashEntryPO.class);
    int account = 1;
    int orderID = 1;

    when(repository.findByAccountAndOrderID(account, orderID)).thenReturn(cashEntryPO);

    CashEntryPO returnedCashEntryPO = cashEntryService.findByAccountAndOrderID(account, orderID);
    Assert.assertEquals(cashEntryPO, returnedCashEntryPO);
  }

  @Test
  public void findById() {

    Optional<CashEntryPO> cashEntryPO = mock(Optional.class);
    String cashEntryID = "1";
    when(repository.findById(Long.valueOf(cashEntryID))).thenReturn(cashEntryPO);
    Optional<CashEntryPO> returnedCashEntryPO = cashEntryService.findById(cashEntryID);
    Assert.assertEquals(cashEntryPO, returnedCashEntryPO);
  }

  @Test
  public void fromMarketOrder() {
    OrderPO orderPO = mock(OrderPO.class);
    AccountPO currentAccountPO = mock(AccountPO.class);
    AssetPO assetPO = mock(AssetPO.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    CashEntryPO cashEntryTO = mock(CashEntryPO.class);
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


    CashEntryPO cashEntryPO = cashEntryService.fromMarketOrder(orderPO);

    Assert.assertEquals(1,cashEntryPO.getAccount());
    Assert.assertEquals(1, cashEntryPO.getOrderID());
    Assert.assertEquals(DEBIT_CREDIT.DBIT,cashEntryPO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(),cashEntryPO.getEntryDate());
    Assert.assertEquals(LocalDate.now().plusDays(3), cashEntryPO.getValueDate());
    Assert.assertEquals(currency, cashEntryPO.getCurrency());
    Assert.assertEquals(Float.valueOf(4), cashEntryPO.getEntryValueReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getNetAmount());
    Assert.assertEquals(Float.valueOf(2),cashEntryPO.getGrossAmount());
    Assert.assertEquals("CHF", cashEntryPO.getAccountReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), cashEntryPO.getFxExchangeRate());
    Assert.assertEquals(1, cashEntryPO.getStatus());
  }
}
