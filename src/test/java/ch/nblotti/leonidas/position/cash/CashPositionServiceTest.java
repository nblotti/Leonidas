package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.position.PositionPO;
import ch.nblotti.leonidas.position.PositionRepository;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.QuoteService;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.internal.matchers.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class CashPositionServiceTest {


  @MockBean
  private PositionRepository repository;

  @MockBean
  CashEntryService cashEntryService;


  @MockBean
  DateTimeFormatter dateTimeFormatter;

  @MockBean
  AccountService accountService;

  @MockBean
  JmsTemplate jmsOrderTemplate;

  @MockBean
  QuoteService quoteService;

  @MockBean
  FXQuoteService fxQuoteService;


  @TestConfiguration
  static class CashPositionServiceTestContextConfiguration {


    @Bean
    public CashPositionService cashPositionService() {

      return new CashPositionService();

    }
  }


  @Autowired
  CashPositionService cashPositionService;

  @Test
  public void saveAll() {

    List<PositionPO> positionPOS = mock(List.class);

    when(repository.saveAll(positionPOS)).thenReturn(positionPOS);
    Iterable<PositionPO> returned = cashPositionService.saveAll(positionPOS);

    Assert.assertEquals(positionPOS, returned);
    verify(repository, times(1)).saveAll(any());

  }

  @Test
  public void aggregateCashEntriesSameDateSameDebitCreditSignIsCredit() {
    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);
    Iterator<CashEntryPO> iterator = mock(Iterator.class);
    CashEntryPO cashEntryPO1 = mock(CashEntryPO.class);
    CashEntryPO cashEntryPO2 = mock(CashEntryPO.class);

    when(cashEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO1.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO2.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO1.getNetAmount()).thenReturn(100f);
    when(cashEntryPO2.getNetAmount()).thenReturn(100f);

    when(cashEntries.iterator()).thenReturn(iterator);
//this is to mock list with one element, adjust accordingly
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(cashEntryPO1, cashEntryPO2);


    Iterable<AggregatedCashEntryVO> aggregatedCashEntryVOS = cashPositionService.aggregateCashEntries(cashEntries);
    List<AggregatedCashEntryVO> entryVOList = Lists.newArrayList(aggregatedCashEntryVOS);
    Assert.assertEquals(1, entryVOList.size());
    AggregatedCashEntryVO aggregatedCashEntryVO = entryVOList.get(0);
    Assert.assertEquals(Float.valueOf(200), aggregatedCashEntryVO.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, aggregatedCashEntryVO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), aggregatedCashEntryVO.getValueDate());

  }

  @Test
  public void aggregateCashEntriesSameDateSameDebitCreditSignIsDebit() {
    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);
    Iterator<CashEntryPO> iterator = mock(Iterator.class);
    CashEntryPO cashEntryPO1 = mock(CashEntryPO.class);
    CashEntryPO cashEntryPO2 = mock(CashEntryPO.class);

    when(cashEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(cashEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(cashEntryPO1.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO2.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO1.getNetAmount()).thenReturn(100f);
    when(cashEntryPO2.getNetAmount()).thenReturn(100f);

    when(cashEntries.iterator()).thenReturn(iterator);
//this is to mock list with one element, adjust accordingly
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(cashEntryPO1, cashEntryPO2);


    Iterable<AggregatedCashEntryVO> aggregatedCashEntryVOS = cashPositionService.aggregateCashEntries(cashEntries);
    List<AggregatedCashEntryVO> entryVOList = Lists.newArrayList(aggregatedCashEntryVOS);
    Assert.assertEquals(1, entryVOList.size());
    AggregatedCashEntryVO aggregatedCashEntryVO = entryVOList.get(0);
    Assert.assertEquals(Float.valueOf(200), aggregatedCashEntryVO.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.DBIT, aggregatedCashEntryVO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), aggregatedCashEntryVO.getValueDate());

  }


  @Test
  public void aggregateCashEntriesSameDateDifferenteDebitCreditChangeInSignCredit() {
    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);
    Iterator<CashEntryPO> iterator = mock(Iterator.class);
    CashEntryPO cashEntryPO1 = mock(CashEntryPO.class);
    CashEntryPO cashEntryPO2 = mock(CashEntryPO.class);

    when(cashEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(cashEntryPO1.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO2.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO1.getNetAmount()).thenReturn(100f);
    when(cashEntryPO2.getNetAmount()).thenReturn(200f);

    when(cashEntries.iterator()).thenReturn(iterator);
//this is to mock list with one element, adjust accordingly
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(cashEntryPO1, cashEntryPO2);


    Iterable<AggregatedCashEntryVO> aggregatedCashEntryVOS = cashPositionService.aggregateCashEntries(cashEntries);
    List<AggregatedCashEntryVO> entryVOList = Lists.newArrayList(aggregatedCashEntryVOS);
    Assert.assertEquals(1, entryVOList.size());
    AggregatedCashEntryVO aggregatedCashEntryVO = entryVOList.get(0);
    Assert.assertEquals(Float.valueOf(-100), aggregatedCashEntryVO.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.DBIT, aggregatedCashEntryVO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), aggregatedCashEntryVO.getValueDate());

  }

  @Test
  public void aggregateCashEntriesSameDateDifferenteDebitCreditChangeInSignDebit() {
    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);
    Iterator<CashEntryPO> iterator = mock(Iterator.class);
    CashEntryPO cashEntryPO1 = mock(CashEntryPO.class);
    CashEntryPO cashEntryPO2 = mock(CashEntryPO.class);

    when(cashEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(cashEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO1.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO2.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO1.getNetAmount()).thenReturn(100f);
    when(cashEntryPO2.getNetAmount()).thenReturn(200f);

    when(cashEntries.iterator()).thenReturn(iterator);
//this is to mock list with one element, adjust accordingly
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(cashEntryPO1, cashEntryPO2);


    Iterable<AggregatedCashEntryVO> aggregatedCashEntryVOS = cashPositionService.aggregateCashEntries(cashEntries);
    List<AggregatedCashEntryVO> entryVOList = Lists.newArrayList(aggregatedCashEntryVOS);
    Assert.assertEquals(1, entryVOList.size());
    AggregatedCashEntryVO aggregatedCashEntryVO = entryVOList.get(0);
    Assert.assertEquals(Float.valueOf(-100), aggregatedCashEntryVO.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, aggregatedCashEntryVO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), aggregatedCashEntryVO.getValueDate());

  }


  @Test
  public void aggregateCashEntriesSameDateDifferenteDebitCreditNoChangeInSign() {
    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);
    Iterator<CashEntryPO> iterator = mock(Iterator.class);
    CashEntryPO cashEntryPO1 = mock(CashEntryPO.class);
    CashEntryPO cashEntryPO2 = mock(CashEntryPO.class);

    when(cashEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(cashEntryPO1.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO2.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO1.getNetAmount()).thenReturn(100f);
    when(cashEntryPO2.getNetAmount()).thenReturn(50f);

    when(cashEntries.iterator()).thenReturn(iterator);
//this is to mock list with one element, adjust accordingly
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(cashEntryPO1, cashEntryPO2);


    Iterable<AggregatedCashEntryVO> aggregatedCashEntryVOS = cashPositionService.aggregateCashEntries(cashEntries);
    List<AggregatedCashEntryVO> entryVOList = Lists.newArrayList(aggregatedCashEntryVOS);
    Assert.assertEquals(1, entryVOList.size());
    AggregatedCashEntryVO aggregatedCashEntryVO = entryVOList.get(0);
    Assert.assertEquals(Float.valueOf(50), aggregatedCashEntryVO.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, aggregatedCashEntryVO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), aggregatedCashEntryVO.getValueDate());

  }

  @Test
  public void aggregateCashEntriesDifferentDate() {
    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);
    Iterator<CashEntryPO> iterator = mock(Iterator.class);
    CashEntryPO cashEntryPO1 = mock(CashEntryPO.class);
    CashEntryPO cashEntryPO2 = mock(CashEntryPO.class);

    when(cashEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(cashEntryPO1.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(cashEntryPO2.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO1.getNetAmount()).thenReturn(100f);
    when(cashEntryPO2.getNetAmount()).thenReturn(100f);

    when(cashEntries.iterator()).thenReturn(iterator);
//this is to mock list with one element, adjust accordingly
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(cashEntryPO1, cashEntryPO2);


    Iterable<AggregatedCashEntryVO> aggregatedCashEntryVOS = cashPositionService.aggregateCashEntries(cashEntries);
    List<AggregatedCashEntryVO> entryVOList = Lists.newArrayList(aggregatedCashEntryVOS);
    Assert.assertEquals(2, entryVOList.size());
    AggregatedCashEntryVO aggregatedCashEntryVO = entryVOList.get(0);
    Assert.assertEquals(Float.valueOf(100), aggregatedCashEntryVO.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, aggregatedCashEntryVO.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now().minusDays(1), aggregatedCashEntryVO.getValueDate());


    AggregatedCashEntryVO aggregatedCashEntryV1 = entryVOList.get(1);
    Assert.assertEquals(Float.valueOf(100), aggregatedCashEntryV1.getNetAmount());
    Assert.assertEquals(DEBIT_CREDIT.CRDT, aggregatedCashEntryV1.getDebitCreditCode());
    Assert.assertEquals(LocalDate.now(), aggregatedCashEntryV1.getValueDate());

  }

  @Test
  public void positionFromEntry() {
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    AggregatedCashEntryVO nextEntry = mock(AggregatedCashEntryVO.class);

    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.ZERO);
    Iterable<PositionPO> positionPOS = cashPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, nextEntry);
    List<PositionPO> positionList = Lists.newArrayList(positionPOS);


    Assert.assertEquals(0, positionList.size());
  }

  @Test
  public void positionFromEntryPositionsEmptyArrayCredit() {

    ArgumentCaptor<Float> amount = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    CashPositionService spyCashPositionService = spy(cashPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    AggregatedCashEntryVO nextEntry = mock(AggregatedCashEntryVO.class);
    Iterator<PositionPO> iterator = mock(Iterator.class);

    when(positions.iterator()).thenReturn(iterator);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now());
    when(currentEntry.getNetAmount()).thenReturn(100f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);

    when(nextEntry.getValueDate()).thenReturn(LocalDate.now().plusDays(3));

    doReturn(positions).when(spyCashPositionService).createPositions(anyObject(), amount.capture(), tma.capture(), anyObject(), anyObject());
    Iterable<PositionPO> positionPOS = spyCashPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, nextEntry);

    verify(spyCashPositionService, times(1)).createPositions(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
    Assert.assertEquals(100f, amount.getValue().floatValue(), 0);
    Assert.assertEquals(2f, tma.getValue().floatValue(), 0);
  }

  @Test
  public void positionFromEntryPositionsEmptyArrayDebit() {

    ArgumentCaptor<Float> amount = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    CashPositionService spyCashPositionService = spy(cashPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    Iterator<PositionPO> iterator = mock(Iterator.class);


    when(positions.iterator()).thenReturn(iterator);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().plusDays(1));
    when(currentEntry.getNetAmount()).thenReturn(100f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);


    doReturn(positions).when(spyCashPositionService).createPositions(anyObject(), amount.capture(), tma.capture(), anyObject(), anyObject());
    Iterable<PositionPO> positionPOS = spyCashPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, null);

    verify(spyCashPositionService, times(1)).createPositions(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
    Assert.assertEquals(-100f, amount.getValue().floatValue(), 0);
    Assert.assertEquals(2f, tma.getValue().floatValue(), 0);
  }

  @Test
  public void positionFromEntryPositionsNotNullDebit() {

    ArgumentCaptor<Float> amount = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    CashPositionService spyCashPositionService = spy(cashPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    AggregatedCashEntryVO nextEntry = mock(AggregatedCashEntryVO.class);
    Iterator<PositionPO> iterator = mock(Iterator.class);
    PositionPO position1 = mock(PositionPO.class);
    PositionPO position2 = mock(PositionPO.class);


    when(positions.iterator()).thenReturn(iterator);
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(position1, position2);


    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().plusDays(1));
    when(currentEntry.getNetAmount()).thenReturn(100f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);

    when(position2.getTMA()).thenReturn(3.5f);
    when(position2.getPosValue()).thenReturn(2f);

    when(nextEntry.getValueDate()).thenReturn(LocalDate.now().plusDays(3));

    doReturn(positions).when(spyCashPositionService).createPositions(anyObject(), amount.capture(), tma.capture(), anyObject(), anyObject());
    Iterable<PositionPO> positionPOS = spyCashPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, null);

    verify(spyCashPositionService, times(1)).createPositions(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
    Assert.assertEquals(102f, amount.getValue().floatValue(), 0);
    Assert.assertEquals(203.5f, tma.getValue().floatValue(), 0);
  }

  @Test
  public void positionFromEntryPositionsNotNullCredit() {

    ArgumentCaptor<Float> amount = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    CashPositionService spyCashPositionService = spy(cashPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    AggregatedCashEntryVO nextEntry = mock(AggregatedCashEntryVO.class);
    Iterator<PositionPO> iterator = mock(Iterator.class);
    PositionPO position1 = mock(PositionPO.class);
    PositionPO position2 = mock(PositionPO.class);


    when(positions.iterator()).thenReturn(iterator);
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(position1, position2);


    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().plusDays(1));
    when(currentEntry.getNetAmount()).thenReturn(50f);

    when(position2.getPosValue()).thenReturn(200f);

    when(nextEntry.getValueDate()).thenReturn(LocalDate.now().plusDays(3));

    doReturn(positions).when(spyCashPositionService).createPositions(anyObject(), amount.capture(), tma.capture(), anyObject(), anyObject());
    Iterable<PositionPO> positionPOS = spyCashPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, null);

    verify(spyCashPositionService, times(1)).createPositions(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
    Assert.assertEquals(150f, amount.getValue().floatValue(), 0);
    Assert.assertEquals(0f, tma.getValue().floatValue(), 0);
  }

  @Test
  public void createPosition() {

    Iterable<PositionPO> positions = mock(Iterable.class);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Float netAmount = 0f;
    Float tma = 0f;
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);

    when(currentAccountPO.getPerformanceCurrency()).thenReturn("CHF");
    when(currentEntry.getCurrency()).thenReturn("USD");
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(30));
    when(quoteDTO.getAdjustedClose()).thenReturn("2");

    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(quoteDTO);

    when(repository.saveAll(anyIterable())).thenAnswer(i -> i.getArguments()[0]);

    Iterable<PositionPO> returnedpositions = cashPositionService.createPositions(currentAccountPO, netAmount, tma, currentEntry, LocalDate.now());

    verify(fxQuoteService, times(31)).getFXQuoteForDate(anyString(), anyString(), anyObject());

    Assert.assertEquals(Lists.newArrayList(returnedpositions).size(), 31);
  }

  @Test
  public void createPositionWithLogger() {

    Iterable<PositionPO> positions = mock(Iterable.class);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Float netAmount = 0f;
    Float tma = 0f;
    AggregatedCashEntryVO currentEntry = mock(AggregatedCashEntryVO.class);
    CashPositionService spyCashPositionService = spy(cashPositionService);
    QuoteDTO quoteDTO = mock(QuoteDTO.class);
    Logger logger = mock(Logger.class);
    when(logger.isLoggable(Level.FINE)).thenReturn(true);

    when(currentAccountPO.getPerformanceCurrency()).thenReturn("CHF");
    when(currentEntry.getCurrency()).thenReturn("USD");
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(30));
    when(quoteDTO.getAdjustedClose()).thenReturn("2");
    doReturn(logger).when(spyCashPositionService).getLogger();

    when(fxQuoteService.getFXQuoteForDate(anyString(), anyString(), anyObject())).thenReturn(quoteDTO);

    when(repository.saveAll(anyIterable())).thenAnswer(i -> i.getArguments()[0]);

    Iterable<PositionPO> returnedpositions = spyCashPositionService.createPositions(currentAccountPO, netAmount, tma, currentEntry, LocalDate.now());

    verify(fxQuoteService, times(31)).getFXQuoteForDate(anyString(), anyString(), anyObject());

    Assert.assertEquals(Lists.newArrayList(returnedpositions).size(), 31);
  }

  @Test
  public void updatePosition() {
    CashPositionService spyCashPositionService = spy(cashPositionService);

    CashEntryPO cashEntryPO = mock(CashEntryPO.class);
    when(cashEntryPO.getValueDate()).thenReturn(LocalDate.now());
    when(cashEntryPO.getAccount()).thenReturn(1);
    when(cashEntryPO.getCurrency()).thenReturn("CHF");

    Iterable<CashEntryPO> cashEntries = mock(Iterable.class);

    Iterable<AggregatedCashEntryVO> aggegatedCashEntries = mock(Iterable.class);

    AccountPO currentAccountPO = mock(AccountPO.class);

    when(cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(cashEntryPO.getAccount(), cashEntryPO.getCurrency())).thenReturn(cashEntries);
    doReturn(aggegatedCashEntries).when(spyCashPositionService).aggregateCashEntries(cashEntries);
    when(accountService.findAccountById(any())).thenReturn(currentAccountPO);

    doNothing().when(spyCashPositionService).updatePositions(currentAccountPO, aggegatedCashEntries);


    spyCashPositionService.updatePositions(cashEntryPO);
    verify(repository, times(1)).deleteByPosTypeAndAccountIdAndCurrency(PositionPO.POS_TYPE.CASH, cashEntryPO.getAccount(), cashEntryPO.getCurrency());
    verify(cashEntryService, times(1)).findAllByAccountAndCurrencyOrderByValueDateAsc(cashEntryPO.getAccount(), cashEntryPO.getCurrency());
    verify(spyCashPositionService, times(1)).aggregateCashEntries(cashEntries);

    verify(accountService, times(1)).findAccountById(cashEntryPO.getAccount());
    verify(spyCashPositionService, times(1)).updatePositions(currentAccountPO, aggegatedCashEntries);
  }

  @Test
  public void updatePositions() {

    Iterable positions = mock(Iterable.class);
    CashPositionService spyCashPositionService = spy(cashPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<AggregatedCashEntryVO> cashEntries = mock(Iterable.class);
    Iterator<AggregatedCashEntryVO> cashEntriesIterator = mock(Iterator.class);
    AggregatedCashEntryVO aggregatedCashEntryVO1 = mock(AggregatedCashEntryVO.class);
    AggregatedCashEntryVO aggregatedCashEntryVO2 = mock(AggregatedCashEntryVO.class);

    when(cashEntries.iterator()).thenReturn(cashEntriesIterator);
    when(cashEntriesIterator.hasNext()).thenReturn(true, true, false);
    when(cashEntriesIterator.next()).thenReturn(aggregatedCashEntryVO1, aggregatedCashEntryVO2);

     doReturn(positions).when(spyCashPositionService).positionFromEntry(anyObject(), anyCollection(), anyObject(), anyObject());
    doReturn(positions).when(spyCashPositionService).positionFromEntry(currentAccountPO, positions, aggregatedCashEntryVO2, null);
    spyCashPositionService.updatePositions(currentAccountPO, cashEntries);

    verify(spyCashPositionService,times(1)).positionFromEntry(anyObject(), anyCollection(), anyObject(), anyObject());
    verify(spyCashPositionService,times(1)).positionFromEntry(currentAccountPO, positions, aggregatedCashEntryVO2, null);

  }
}
