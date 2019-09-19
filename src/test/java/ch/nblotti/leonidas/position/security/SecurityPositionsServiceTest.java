package ch.nblotti.leonidas.position.security;


import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.position.PositionPO;
import ch.nblotti.leonidas.position.PositionRepository;
import ch.nblotti.leonidas.position.cash.AggregatedCashEntryVO;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.QuoteService;
import com.google.common.collect.Lists;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class SecurityPositionsServiceTest {


  @MockBean
  private PositionRepository repository;

  @MockBean
  SecurityEntryService securityEntryService;
  @MockBean
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

  @Autowired
  SecurityPositionService securityPositionService;


  @Test
  public void aggregateSecuritiesEntriesByDay() {

    //private Iterable<AggregatedSecurityEntryVO>
  }


  @Test
  public void updatePositionNoPositionDeletion() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    SecurityEntryPO entry = mock(SecurityEntryPO.class);
    Iterable<AggregatedSecurityEntryVO> aggregatedSecurityEntryVOS = mock(Iterable.class);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    AccountPO accountPO = mock(AccountPO.class);
    when(entry.getValueDate()).thenReturn(LocalDate.now().plusDays(10));
    when(entry.getAccount()).thenReturn(1);
    when(entry.getSecurityID()).thenReturn("FB");
    when(entry.getSecurityID()).thenReturn("USD");

    when(accountService.findAccountById(entry.getAccount())).thenReturn(accountPO);
    when(securityEntryService.findAllByAccountAndSecurityIDOrderByValueDateAsc(entry.getAccount(), entry.getSecurityID())).thenReturn(securityEntries);
    doReturn(aggregatedSecurityEntryVOS).when(spySecurityPositionService).aggregateSecuritiesEntriesByDay(anyIterable());


    AccountPO currentAccountPO = accountService.findAccountById(entry.getAccount());

    doNothing().when(spySecurityPositionService).updatePositions(any(), anyIterable());

    spySecurityPositionService.updatePosition(entry);

    verify(repository, times(0)).deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(PositionPO.POS_TYPE.SECURITY, entry.getAccount(), entry.getSecurityID(), entry.getCurrency());
  }


  @Test
  public void updatePositionPositionDeletion() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    SecurityEntryPO entry = mock(SecurityEntryPO.class);
    Iterable<AggregatedSecurityEntryVO> aggregatedSecurityEntryVOS = mock(Iterable.class);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    AccountPO accountPO = mock(AccountPO.class);
    when(entry.getValueDate()).thenReturn(LocalDate.now());
    when(entry.getAccount()).thenReturn(1);
    when(entry.getSecurityID()).thenReturn("FB");
    when(entry.getSecurityID()).thenReturn("USD");

    when(accountService.findAccountById(entry.getAccount())).thenReturn(accountPO);
    when(securityEntryService.findAllByAccountAndSecurityIDOrderByValueDateAsc(entry.getAccount(), entry.getSecurityID())).thenReturn(securityEntries);
    doReturn(aggregatedSecurityEntryVOS).when(spySecurityPositionService).aggregateSecuritiesEntriesByDay(anyIterable());


    AccountPO currentAccountPO = accountService.findAccountById(entry.getAccount());

    doNothing().when(spySecurityPositionService).updatePositions(any(), anyIterable());

    spySecurityPositionService.updatePosition(entry);

    verify(repository, times(1)).deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(PositionPO.POS_TYPE.SECURITY, entry.getAccount(), entry.getSecurityID(), entry.getCurrency());
  }

  @Test
  public void saveAll() {

    List<PositionPO> positions = mock(List.class);

    when(repository.saveAll(anyIterable())).then(i -> i.getArgument(0));
    Iterable<PositionPO> returnedPositions = securityPositionService.saveAll(positions);

    Assert.assertEquals(positions, returnedPositions);

  }

  @Test
  public void createSecurityPositions() {

    AccountPO currentAccountPO = mock(AccountPO.class);
    Float quantity = 2f;
    Float cma = 2f;
    Float tma = 3f;
    Float realized = 4f;
    AggregatedSecurityEntryVO firstEntry = mock(AggregatedSecurityEntryVO.class);
    LocalDate endDate = LocalDate.now();
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    QuoteDTO fxQuoteDto = mock(QuoteDTO.class);
    Logger localLogger = mock(Logger.class);

    when(uuidHolder.getCurrentRandomUUID()).thenReturn("1");
    when(firstEntry.getAccount()).thenReturn(1);
    when(firstEntry.getSecurityID()).thenReturn("FB");
    when(currentAccountPO.getPerformanceCurrency()).thenReturn("CHF");
    when(firstEntry.getExchange()).thenReturn("US");
    when(quoteDTO.getAdjustedClose()).thenReturn("2.1");
    when(fxQuoteDto.getAdjustedClose()).thenReturn("1.5");

    when(quoteService.getQuoteForDate(anyString(), anyString(), any())).thenReturn(quoteDTO);
    when(firstEntry.getCurrency()).thenReturn("USD");
    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), any())).thenReturn(fxQuoteDto);

    when(firstEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(10));

    when(repository.saveAll(anyIterable())).then(i -> i.getArgument(0));

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);

    when(localLogger.isLoggable(Level.FINE)).thenReturn(true);
    doReturn(localLogger).when(spySecurityPositionService).getLogger();


    Iterable<PositionPO> positionsIt = spySecurityPositionService.createSecurityPositions(currentAccountPO, quantity, cma, tma, realized, firstEntry, endDate, uuidHolder);

    List<PositionPO> positions = Lists.newArrayList(positionsIt);
    Assert.assertEquals(11, positions.size());
    Assert.assertEquals("1", positions.get(0).getUniqueID());
    Assert.assertEquals(LocalDate.now().minusDays(10), positions.get(0).getPosDate());
    Assert.assertEquals(firstEntry.getAccount(), positions.get(0).getAccountId());
    Assert.assertEquals(quantity, positions.get(0).getQuantity());
    Assert.assertEquals(cma, positions.get(0).getCMA());
    Assert.assertEquals(tma, positions.get(0).getTMA());
    Assert.assertEquals(currentAccountPO.getPerformanceCurrency(), positions.get(0).getAccountPerformanceCurrency());

    Assert.assertEquals("1", positions.get(1).getUniqueID());
    Assert.assertEquals(LocalDate.now().minusDays(9), positions.get(1).getPosDate());
    Assert.assertEquals(firstEntry.getAccount(), positions.get(1).getAccountId());
    Assert.assertEquals(quantity, positions.get(1).getQuantity());
    Assert.assertEquals(cma, positions.get(1).getCMA());
    Assert.assertEquals(tma, positions.get(1).getTMA());
    Assert.assertEquals(currentAccountPO.getPerformanceCurrency(), positions.get(1).getAccountPerformanceCurrency());


  }


  @Test
  public void createSecurityPositionsZeroQuantity() {

    AccountPO currentAccountPO = mock(AccountPO.class);
    Float quantity = 0f;
    Float cma = 0f;
    Float tma = 0f;
    Float realized = 0f;
    AggregatedSecurityEntryVO firstEntry = mock(AggregatedSecurityEntryVO.class);
    LocalDate endDate = LocalDate.now();
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    QuoteDTO fxQuoteDto = mock(QuoteDTO.class);
    Logger localLogger = mock(Logger.class);

    when(uuidHolder.getCurrentRandomUUID()).thenReturn("1");
    when(firstEntry.getAccount()).thenReturn(1);
    when(firstEntry.getSecurityID()).thenReturn("FB");
    when(currentAccountPO.getPerformanceCurrency()).thenReturn("CHF");
    when(firstEntry.getExchange()).thenReturn("US");
    when(quoteDTO.getAdjustedClose()).thenReturn("2.1");
    when(fxQuoteDto.getAdjustedClose()).thenReturn("1.5");

    when(quoteService.getQuoteForDate(anyString(), anyString(), any())).thenReturn(quoteDTO);
    when(firstEntry.getCurrency()).thenReturn("USD");
    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), any())).thenReturn(fxQuoteDto);

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);


    when(firstEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(10));

    when(repository.saveAll(anyIterable())).then(i -> i.getArgument(0));

    when(localLogger.isLoggable(Level.FINE)).thenReturn(false);
    doReturn(localLogger).when(spySecurityPositionService).getLogger();

    Iterable<PositionPO> positionsIt = spySecurityPositionService.createSecurityPositions(currentAccountPO, quantity, cma, tma, realized, firstEntry, endDate, uuidHolder);

    List<PositionPO> positions = Lists.newArrayList(positionsIt);
    Assert.assertEquals(1, positions.size());
    Assert.assertEquals("1", positions.get(0).getUniqueID());
    Assert.assertEquals(LocalDate.now().minusDays(10), positions.get(0).getPosDate());
    Assert.assertEquals(firstEntry.getAccount(), positions.get(0).getAccountId());
    Assert.assertEquals(quantity, positions.get(0).getQuantity());
    Assert.assertEquals(cma, positions.get(0).getCMA());
    Assert.assertEquals(tma, positions.get(0).getTMA());
    Assert.assertEquals(currentAccountPO.getPerformanceCurrency(), positions.get(0).getAccountPerformanceCurrency());


  }

  @Test
  public void getLogger() {


    Logger logger = securityPositionService.getLogger();

    Assert.assertEquals(SecurityPositionService.logger, logger);
  }

}

