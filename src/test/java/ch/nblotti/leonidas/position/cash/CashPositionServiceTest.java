package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.position.PositionPO;
import ch.nblotti.leonidas.position.PositionRepository;
import ch.nblotti.leonidas.quote.FXQuoteService;
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
import java.util.Iterator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
}
