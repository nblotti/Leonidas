package ch.nblotti.leonidas.entry.security;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.entry.ACHAT_VENTE;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class SecurityEntryServiceTest {


  @MockBean
  private SecurityEntryRepository repository;

  @MockBean
  private JmsTemplate jmsOrderTemplate;

  @MockBean
  AccountService accountService;

  @MockBean
  AssetService assetService;

  @MockBean
  QuoteService quoteService;


  @MockBean
  FXQuoteService fxQuoteService;

  @MockBean
  MarketProcessService marketProcessService;


  @TestConfiguration
  static class SecurityEntryServiceTestContextConfiguration {


    @Bean
    public SecurityEntryService securityEntryService() {

      return new SecurityEntryService();

    }
  }


  @Autowired
  SecurityEntryService securityEntryService;


  @Test
  public void findAll() {

    Iterable<SecurityEntryPO> entries = mock(Iterable.class);
    when(repository.findAll()).thenReturn(entries);

    Iterable<SecurityEntryPO> returned = securityEntryService.findAll();

    Assert.assertEquals(entries, returned);
  }

  @Test
  public void findById() {
    Optional<SecurityEntryPO> entry = mock(Optional.class);
    when(repository.findById(1l)).thenReturn(entry);


    Optional<SecurityEntryPO> returned = securityEntryService.findById("1");

    Assert.assertEquals(entry, returned);

  }

  @Test
  public void findAllByAccountAndSecurityIDOrderByValueDateAsc() {
    Iterable<SecurityEntryPO> entries = mock(Iterable.class);
    when(repository.findAllByAccountAndSecurityIDOrderByValueDateAsc(1, "FB")).thenReturn(entries);


    Iterable<SecurityEntryPO> returned = securityEntryService.findAllByAccountAndSecurityIDOrderByValueDateAsc(1, "FB");

    Assert.assertEquals(entries, returned);

  }

  @Test
  public void findByAccountAndOrderID() {
    SecurityEntryPO entry = mock(SecurityEntryPO.class);
    when(repository.findByAccountAndOrderID(1, 2)).thenReturn(entry);

    SecurityEntryPO returned = securityEntryService.findByAccountAndOrderID(1, 2);

    Assert.assertEquals(entry, returned);

  }


  @Test
  public void fromOrder() {
    OrderPO orderPO = mock(OrderPO.class);
    AccountPO accountPO = mock(AccountPO.class);
    AssetPO assetPO = mock(AssetPO.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    QuoteDTO fxQuoteDTO = mock(QuoteDTO.class);

    when(accountService.findAccountById(any())).thenReturn(accountPO);
    when(assetService.getSymbol(any(), any())).thenReturn(assetPO);
    when(quoteService.getQuoteForDate(any(), any(), any())).thenReturn(quoteDTO);

    when(orderPO.getTransactTime()).thenReturn(LocalDate.now());
    when(fxQuoteService.getFXQuoteForDate(any(), any(), any())).thenReturn(fxQuoteDTO);


    when(orderPO.getExchange()).thenReturn("US");
    when(assetService.getValueDateForExchange(assetPO.getExchange())).thenReturn(3);


    when(orderPO.getAccountId()).thenReturn(1);
    when(orderPO.getId()).thenReturn(1l);
    when(orderPO.getTransactTime()).thenReturn(LocalDate.now());
    when(orderPO.getSide()).thenReturn(ACHAT_VENTE.ACHAT);
    when(orderPO.getStatus()).thenReturn(1);
    when(quoteDTO.getAdjustedClose()).thenReturn("2");
    when(fxQuoteDTO.getAdjustedClose()).thenReturn("2");
    when(orderPO.getOrderQtyData()).thenReturn(1F);
    when(accountPO.getPerformanceCurrency()).thenReturn("CHF");
    when(orderPO.getOrderQtyData()).thenReturn(1F);
    when(assetPO.getCode()).thenReturn("1");
    when(assetPO.getCurrency()).thenReturn("CHF");


    SecurityEntryPO returned = securityEntryService.fromOrder(orderPO);

    Assert.assertEquals(1, returned.getAccount());
    Assert.assertEquals(Float.valueOf(1), returned.getQuantity());
    Assert.assertEquals("US", returned.getExchange());
    Assert.assertEquals("1", returned.getSecurityID());
    Assert.assertEquals(1, returned.getOrderID());
    Assert.assertEquals(ACHAT_VENTE.ACHAT, returned.getAchatVenteCode());
    Assert.assertEquals(LocalDate.now(), returned.getEntryDate());
    Assert.assertEquals(LocalDate.now().plusDays(3), returned.getValueDate());
    Assert.assertEquals("CHF", returned.getCurrency());
    Assert.assertEquals(Float.valueOf(4), returned.getEntryValueReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), returned.getNetAmount());
    Assert.assertEquals(Float.valueOf(2), returned.getGrossAmount());
    Assert.assertEquals("CHF", returned.getAccountReportingCurrency());
    Assert.assertEquals(Float.valueOf(2), returned.getFxExchangeRate());
    Assert.assertEquals(1, returned.getStatus());


  }

  @Test
  public void save() {

    SecurityEntryPO securityEntryPO = mock(SecurityEntryPO.class);
    Logger logger = mock(Logger.class);


    SecurityEntryService spySecurityEntryService = spy(securityEntryService);
    doReturn(logger).when(spySecurityEntryService).getLogger();
    when(logger.isLoggable(any())).thenReturn(Boolean.TRUE);
    when(repository.save(any())).thenReturn(securityEntryPO);
    when(securityEntryPO.getOrderID()).thenReturn(1l);
    when(securityEntryPO.getAccount()).thenReturn(1);

    SecurityEntryPO returned = spySecurityEntryService.save(securityEntryPO);

    verify(marketProcessService, times(1)).setSecurityhEntryRunningForProcess(1, 1);
    verify(jmsOrderTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    verify(logger, times(1)).fine(anyString());

    Assert.assertEquals(securityEntryPO, returned);
  }

  @Test
  public void saveNoLogger() {

    SecurityEntryPO securityEntryPO = mock(SecurityEntryPO.class);
    Logger logger = mock(Logger.class);


    SecurityEntryService spySecurityEntryService = spy(securityEntryService);
    doReturn(logger).when(spySecurityEntryService).getLogger();
    when(logger.isLoggable(any())).thenReturn(Boolean.FALSE);
    when(repository.save(any())).thenReturn(securityEntryPO);
    when(securityEntryPO.getOrderID()).thenReturn(1l);
    when(securityEntryPO.getAccount()).thenReturn(1);

    SecurityEntryPO returned = spySecurityEntryService.save(securityEntryPO);

    verify(marketProcessService, times(1)).setSecurityhEntryRunningForProcess(1, 1);
    verify(jmsOrderTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    verify(logger, times(0)).fine(anyString());

    Assert.assertEquals(securityEntryPO, returned);
  }


  @Test
  public void getLogger() {
    Logger returned = securityEntryService.getLogger();
    Assert.assertEquals("SecurityEntryService", returned.getName());

  }


}
