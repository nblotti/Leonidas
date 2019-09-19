package ch.nblotti.leonidas.position.security;


import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
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
  public void positionFromEntryPositionCredit() {
    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);

    Iterable<PositionPO> positions = mock(Iterable.class);
    Iterator<PositionPO> positionsItr = mock(Iterator.class);
    when(positions.iterator()).thenReturn(positionsItr);
    PositionPO positionPO1 = mock(PositionPO.class);
    PositionPO positionPO2= mock(PositionPO.class);
    when(positionsItr.hasNext()).thenReturn(true,true,false);
    when(positionsItr.next()).thenReturn(positionPO1,positionPO2);

    when(positionPO1.getRealized()).thenReturn(2f);
    when(positionPO2.getRealized()).thenReturn(4f);

    when(positionPO1.getQuantity()).thenReturn(2f);
    when(positionPO2.getQuantity()).thenReturn(4f);

    when(positionPO1.getCMA()).thenReturn(2f);
    when(positionPO2.getCMA()).thenReturn(4f);

    when(positionPO1.getTMA()).thenReturn(2f);
    when(positionPO2.getTMA()).thenReturn(4f);

    AggregatedSecurityEntryVO currentEntry = mock(AggregatedSecurityEntryVO.class);
    AggregatedSecurityEntryVO nextEntry = mock(AggregatedSecurityEntryVO.class);
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);

    ArgumentCaptor<LocalDate> endDate= ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<Float> quantity = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> cma = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(5));

    when(nextEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(5));
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getNetPosValue()).thenReturn(2f);
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);

    doReturn(null).when(spySecurityPositionService).createSecurityPositions(any(), quantity.capture(), cma.capture(), tma.capture(), any(), any(), endDate.capture(), any());


    Iterable<PositionPO> returnedPositionsIt = spySecurityPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, null, uuidHolder);

    Assert.assertNull( returnedPositionsIt);
    Assert.assertEquals(6f,quantity.getValue(),0);
    Assert.assertEquals(3f,cma.getValue(),0);
    Assert.assertEquals(7f,tma.getValue(),0);
    Assert.assertEquals(LocalDate.now(),endDate.getValue());
  }


  @Test
  public void positionFromEntryPositionDebit() {
    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);

    Iterable<PositionPO> positions = mock(Iterable.class);
    Iterator<PositionPO> positionsItr = mock(Iterator.class);
    when(positions.iterator()).thenReturn(positionsItr);
    PositionPO positionPO1 = mock(PositionPO.class);
    PositionPO positionPO2= mock(PositionPO.class);
    when(positionsItr.hasNext()).thenReturn(true,true,false);
    when(positionsItr.next()).thenReturn(positionPO1,positionPO2);

    when(positionPO1.getRealized()).thenReturn(2f);
    when(positionPO2.getRealized()).thenReturn(4f);

    when(positionPO1.getQuantity()).thenReturn(2f);
    when(positionPO2.getQuantity()).thenReturn(4f);

    when(positionPO1.getCMA()).thenReturn(2f);
    when(positionPO2.getCMA()).thenReturn(4f);

    when(positionPO1.getTMA()).thenReturn(2f);
    when(positionPO2.getTMA()).thenReturn(4f);

    AggregatedSecurityEntryVO currentEntry = mock(AggregatedSecurityEntryVO.class);
    AggregatedSecurityEntryVO nextEntry = mock(AggregatedSecurityEntryVO.class);
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);

    ArgumentCaptor<LocalDate> endDate= ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<Float> quantity = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> cma = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(5));

    when(nextEntry.getValueDate()).thenReturn(LocalDate.now());
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getNetPosValue()).thenReturn(2f);
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);

    doReturn(null).when(spySecurityPositionService).createSecurityPositions(any(), quantity.capture(), cma.capture(), tma.capture(), any(), any(), endDate.capture(), any());


    Iterable<PositionPO> returnedPositionsIt = spySecurityPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, nextEntry, uuidHolder);

    Assert.assertNull( returnedPositionsIt);
    Assert.assertEquals(2f,quantity.getValue(),0);
    Assert.assertEquals(4f,cma.getValue(),0);
    Assert.assertEquals(4f,tma.getValue(),0);
    Assert.assertEquals(LocalDate.now().minusDays(1),endDate.getValue());
  }




  @Test
  public void positionFromEntryPositionEmptyLocalDateAfter() {
    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedSecurityEntryVO currentEntry = mock(AggregatedSecurityEntryVO.class);
    AggregatedSecurityEntryVO nextEntry = mock(AggregatedSecurityEntryVO.class);
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);
    Iterator<PositionPO> positionsItr = mock(Iterator.class);
    when(positionsItr.hasNext()).thenReturn(false);

    ArgumentCaptor<LocalDate> endDate= ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<Float> quantity = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> cma = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now().minusDays(5));

    when(positions.iterator()).thenReturn(positionsItr);
    when(nextEntry.getValueDate()).thenReturn(LocalDate.now());
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getNetPosValue()).thenReturn(2f);
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);

    doReturn(null).when(spySecurityPositionService).createSecurityPositions(any(), quantity.capture(), cma.capture(), tma.capture(), any(), any(), endDate.capture(), any());


    Iterable<PositionPO> returnedPositionsIt = spySecurityPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, null, uuidHolder);

    Assert.assertNull( returnedPositionsIt);
    Assert.assertEquals(2f,quantity.getValue(),0);
    Assert.assertEquals(1f,cma.getValue(),0);
    Assert.assertEquals(2f,tma.getValue(),0);
    Assert.assertEquals(LocalDate.now(),endDate.getValue());
  }

  @Test
  public void positionFromEntryPositionNull() {
    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedSecurityEntryVO currentEntry = mock(AggregatedSecurityEntryVO.class);
    AggregatedSecurityEntryVO nextEntry = mock(AggregatedSecurityEntryVO.class);
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);


    ArgumentCaptor<LocalDate> endDate= ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<Float> quantity = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> cma = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> tma = ArgumentCaptor.forClass(Float.class);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(currentEntry.getValueDate()).thenReturn(LocalDate.now());

    when(nextEntry.getValueDate()).thenReturn(LocalDate.now());
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getNetPosValue()).thenReturn(2f);
    when(currentEntry.getQuantity()).thenReturn(2f);
    when(currentEntry.getFxchangeRate()).thenReturn(2f);

    doReturn(null).when(spySecurityPositionService).createSecurityPositions(any(), quantity.capture(), cma.capture(), tma.capture(), any(), any(), endDate.capture(), any());


    Iterable<PositionPO> returnedPositionsIt = spySecurityPositionService.positionFromEntry(currentAccountPO, null, currentEntry, nextEntry, uuidHolder);

    Assert.assertNull( returnedPositionsIt);
    Assert.assertEquals(2f,quantity.getValue(),0);
    Assert.assertEquals(1f,cma.getValue(),0);
    Assert.assertEquals(2f,tma.getValue(),0);
    Assert.assertEquals(LocalDate.now().minusDays(1),endDate.getValue());
  }


  @Test
  public void positionFromEntryDebitCreditZero() {
    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<PositionPO> positions = mock(Iterable.class);
    AggregatedSecurityEntryVO currentEntry = mock(AggregatedSecurityEntryVO.class);
    AggregatedSecurityEntryVO nextEntry = mock(AggregatedSecurityEntryVO.class);
    SecurityPositionService.UUIDHolder uuidHolder = mock(SecurityPositionService.UUIDHolder.class);

    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.ZERO);


    Iterable<PositionPO> returnedPositionsIt = spySecurityPositionService.positionFromEntry(currentAccountPO, positions, currentEntry, nextEntry, uuidHolder);

    List<PositionPO> returnedPositions = Lists.newArrayList(returnedPositionsIt);
    Assert.assertEquals(0, returnedPositions.size());
  }

  @Test
  public void updatePositionsTwoEntry() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<AggregatedSecurityEntryVO> securityEntries = mock(Iterable.class);
    Iterator<AggregatedSecurityEntryVO> securityEntryVOIterator = mock(Iterator.class);
    Iterable<PositionPO> positions = mock(Iterable.class);

    AggregatedSecurityEntryVO aggregatedSecurityEntryVO1 = mock(AggregatedSecurityEntryVO.class);
    AggregatedSecurityEntryVO aggregatedSecurityEntryVO2 = mock(AggregatedSecurityEntryVO.class);

    ArgumentCaptor<SecurityPositionService.UUIDHolder> argumentCaptor = ArgumentCaptor.forClass(SecurityPositionService.UUIDHolder.class);

    doReturn(positions).when(spySecurityPositionService).positionFromEntry(any(), any(), any(), nullable(AggregatedSecurityEntryVO.class), argumentCaptor.capture());

    when(securityEntries.iterator()).thenReturn(securityEntryVOIterator);
    when(securityEntryVOIterator.hasNext()).thenReturn(true, true, false);
    when(securityEntryVOIterator.next()).thenReturn(aggregatedSecurityEntryVO1, aggregatedSecurityEntryVO2);
    spySecurityPositionService.updatePositions(currentAccountPO, securityEntries);

    SecurityPositionService.UUIDHolder holder = argumentCaptor.getValue();

    verify(spySecurityPositionService, times(2)).positionFromEntry(any(), any(), any(), any(), any());

    String id = holder.getCurrentRandomUUID();
    Assert.assertEquals(id, holder.getCurrentRandomUUID());

    String newId = holder.getNewRandomUUID();
    Assert.assertEquals(newId, holder.getCurrentRandomUUID());
  }

  @Test
  public void updatePositionsOneEntry() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    AccountPO currentAccountPO = mock(AccountPO.class);
    Iterable<AggregatedSecurityEntryVO> securityEntries = mock(Iterable.class);
    Iterator<AggregatedSecurityEntryVO> securityEntryVOIterator = mock(Iterator.class);
    Iterable<PositionPO> positions = mock(Iterable.class);

    AggregatedSecurityEntryVO aggregatedSecurityEntryVO1 = mock(AggregatedSecurityEntryVO.class);

    doReturn(positions).when(spySecurityPositionService).positionFromEntry(any(), any(), any(), nullable(AggregatedSecurityEntryVO.class), any());

    when(securityEntries.iterator()).thenReturn(securityEntryVOIterator);
    when(securityEntryVOIterator.hasNext()).thenReturn(true, false);
    when(securityEntryVOIterator.next()).thenReturn(aggregatedSecurityEntryVO1);
    spySecurityPositionService.updatePositions(currentAccountPO, securityEntries);

    verify(spySecurityPositionService, times(1)).positionFromEntry(any(), any(), any(), nullable(AggregatedSecurityEntryVO.class), any());
  }


  @Test
  public void updateEntryAtZeroCredit() {
    SecurityEntryPO currentEntry = mock(SecurityEntryPO.class);
    AggregatedSecurityEntryVO existingEntry = mock(AggregatedSecurityEntryVO.class);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(currentEntry.getQuantity()).thenReturn(10f);
    when(currentEntry.getNetAmount()).thenReturn(10f);
    when(currentEntry.getGrossAmount()).thenReturn(10f);

    securityPositionService.updateEntryAtZero(currentEntry, existingEntry);

    verify(existingEntry, times(1)).setQuantity(-10f);
    verify(existingEntry, times(1)).setNetPosValue(-10f);
    verify(existingEntry, times(1)).setGrossPosValue(-10f);

  }

  @Test
  public void updateEntryAtZero() {
    SecurityEntryPO currentEntry = mock(SecurityEntryPO.class);
    AggregatedSecurityEntryVO existingEntry = mock(AggregatedSecurityEntryVO.class);
    when(currentEntry.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(currentEntry.getQuantity()).thenReturn(10f);
    when(currentEntry.getNetAmount()).thenReturn(10f);
    when(currentEntry.getGrossAmount()).thenReturn(10f);

    securityPositionService.updateEntryAtZero(currentEntry, existingEntry);

    verify(existingEntry, times(1)).setQuantity(10f);
    verify(existingEntry, times(1)).setNetPosValue(10f);
    verify(existingEntry, times(1)).setGrossPosValue(10f);

  }


  @Test
  public void updateEntryWithSameSign() {
    SecurityEntryPO currentEntry = mock(SecurityEntryPO.class);
    AggregatedSecurityEntryVO existingEntry = mock(AggregatedSecurityEntryVO.class);

    when(currentEntry.getQuantity()).thenReturn(10f);
    when(existingEntry.getQuantity()).thenReturn(20f);
    when(currentEntry.getNetAmount()).thenReturn(10f);
    when(existingEntry.getNetPosValue()).thenReturn(20f);
    when(currentEntry.getGrossAmount()).thenReturn(10f);
    when(existingEntry.getGrossPosValue()).thenReturn(20f);

    securityPositionService.updateEntryWithSameSign(currentEntry, existingEntry);

    verify(existingEntry, times(1)).setQuantity(30f);
    verify(existingEntry, times(1)).setNetPosValue(30f);
    verify(existingEntry, times(1)).setGrossPosValue(30f);

  }

  @Test
  public void updateEntryWithDifferentSignChangeInSign() {
    SecurityEntryPO currentEntry = mock(SecurityEntryPO.class);
    AggregatedSecurityEntryVO existingEntry = mock(AggregatedSecurityEntryVO.class);

    when(currentEntry.getQuantity()).thenReturn(20f);
    when(existingEntry.getQuantity()).thenReturn(10f);
    when(currentEntry.getNetAmount()).thenReturn(20f);
    when(existingEntry.getNetPosValue()).thenReturn(10f);
    when(currentEntry.getGrossAmount()).thenReturn(20f);
    when(existingEntry.getGrossPosValue()).thenReturn(10f);

    securityPositionService.updateEntryWithDifferentSign(currentEntry, existingEntry);

    verify(existingEntry, times(1)).setDebitCreditCode(DEBIT_CREDIT.CRDT);
    verify(existingEntry, times(1)).setQuantity(-10f);
    verify(existingEntry, times(1)).setNetPosValue(-10f);
    verify(existingEntry, times(1)).setGrossPosValue(-10f);

  }

  @Test
  public void updateEntryWithDifferentSign() {
    SecurityEntryPO currentEntry = mock(SecurityEntryPO.class);
    AggregatedSecurityEntryVO existingEntry = mock(AggregatedSecurityEntryVO.class);

    when(currentEntry.getQuantity()).thenReturn(10f);
    when(existingEntry.getQuantity()).thenReturn(20f);
    when(currentEntry.getNetAmount()).thenReturn(10f);
    when(existingEntry.getNetPosValue()).thenReturn(20f);
    when(currentEntry.getGrossAmount()).thenReturn(10f);
    when(existingEntry.getGrossPosValue()).thenReturn(20f);

    securityPositionService.updateEntryWithDifferentSign(currentEntry, existingEntry);

    verify(existingEntry, times(1)).setDebitCreditCode(DEBIT_CREDIT.DBIT);
    verify(existingEntry, times(1)).setQuantity(10f);
    verify(existingEntry, times(1)).setNetPosValue(10f);
    verify(existingEntry, times(1)).setGrossPosValue(10f);

  }


  @Test
  public void aggregateSecuritiesEntriesSortByDay() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    Iterator<SecurityEntryPO> securityEntryPOIterator = mock(Iterator.class);
    SecurityEntryPO securityEntryPO1 = mock(SecurityEntryPO.class);
    SecurityEntryPO securityEntryPO2 = mock(SecurityEntryPO.class);

    when(securityEntryPO1.getValueDate()).thenReturn(LocalDate.now());
    when(securityEntryPO2.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);
    when(securityEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);

    when(securityEntryPO1.getQuantity()).thenReturn(10f);
    when(securityEntryPO2.getQuantity()).thenReturn(10f);

    when(securityEntryPO2.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntries.iterator()).thenReturn(securityEntryPOIterator);
    when(securityEntryPOIterator.hasNext()).thenReturn(true, true, false);
    when(securityEntryPOIterator.next()).thenReturn(securityEntryPO1, securityEntryPO2);
    doNothing().when(spySecurityPositionService).updateEntryAtZero(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithSameSign(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithDifferentSign(any(), any());

    Iterable<AggregatedSecurityEntryVO> returnedAggregatedSecurityEntryVos = spySecurityPositionService.aggregateSecuritiesEntriesByDay(securityEntries);

    List<AggregatedSecurityEntryVO> aggregatedSecurityEntryVOSLst = Lists.newArrayList(returnedAggregatedSecurityEntryVos);

    Assert.assertEquals(2, aggregatedSecurityEntryVOSLst.size());
    verify(spySecurityPositionService, times(0)).updateEntryWithDifferentSign(any(), any());
    verify(spySecurityPositionService, times(0)).updateEntryAtZero(any(), any());
    verify(spySecurityPositionService, times(0)).updateEntryWithSameSign(any(), any());

    Assert.assertEquals(LocalDate.now().minusDays(1), aggregatedSecurityEntryVOSLst.get(0).getValueDate());
    Assert.assertEquals(LocalDate.now(), aggregatedSecurityEntryVOSLst.get(1).getValueDate());

  }


  @Test
  public void aggregateSecuritiesEntriesByDayDifferentSignQuantityEqualsZero() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    Iterator<SecurityEntryPO> securityEntryPOIterator = mock(Iterator.class);
    SecurityEntryPO securityEntryPO1 = mock(SecurityEntryPO.class);
    SecurityEntryPO securityEntryPO2 = mock(SecurityEntryPO.class);

    when(securityEntryPO1.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(securityEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);

    when(securityEntryPO1.getQuantity()).thenReturn(10f);
    when(securityEntryPO2.getQuantity()).thenReturn(10f);

    when(securityEntryPO2.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntries.iterator()).thenReturn(securityEntryPOIterator);
    when(securityEntryPOIterator.hasNext()).thenReturn(true, true, false);
    when(securityEntryPOIterator.next()).thenReturn(securityEntryPO1, securityEntryPO2);
    doNothing().when(spySecurityPositionService).updateEntryAtZero(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithSameSign(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithDifferentSign(any(), any());

    Iterable<AggregatedSecurityEntryVO> returnedAggregatedSecurityEntryVos = spySecurityPositionService.aggregateSecuritiesEntriesByDay(securityEntries);

    List<AggregatedSecurityEntryVO> aggregatedSecurityEntryVOSLst = Lists.newArrayList(returnedAggregatedSecurityEntryVos);

    Assert.assertEquals(0, aggregatedSecurityEntryVOSLst.size());
    verify(spySecurityPositionService, times(0)).updateEntryWithDifferentSign(any(), any());
    verify(spySecurityPositionService, times(0)).updateEntryAtZero(any(), any());
    verify(spySecurityPositionService, times(0)).updateEntryWithSameSign(any(), any());

  }

  @Test
  public void aggregateSecuritiesEntriesByDayDifferentSignQuantityNotEqualsZero() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    Iterator<SecurityEntryPO> securityEntryPOIterator = mock(Iterator.class);
    SecurityEntryPO securityEntryPO1 = mock(SecurityEntryPO.class);
    SecurityEntryPO securityEntryPO2 = mock(SecurityEntryPO.class);

    when(securityEntryPO1.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(securityEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.DBIT);

    when(securityEntryPO1.getQuantity()).thenReturn(10f);
    when(securityEntryPO2.getQuantity()).thenReturn(5f);

    when(securityEntryPO2.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntries.iterator()).thenReturn(securityEntryPOIterator);
    when(securityEntryPOIterator.hasNext()).thenReturn(true, true, false);
    when(securityEntryPOIterator.next()).thenReturn(securityEntryPO1, securityEntryPO2);
    doNothing().when(spySecurityPositionService).updateEntryAtZero(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithSameSign(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithDifferentSign(any(), any());

    Iterable<AggregatedSecurityEntryVO> returnedAggregatedSecurityEntryVos = spySecurityPositionService.aggregateSecuritiesEntriesByDay(securityEntries);

    List<AggregatedSecurityEntryVO> aggregatedSecurityEntryVOSLst = Lists.newArrayList(returnedAggregatedSecurityEntryVos);

    Assert.assertEquals(1, aggregatedSecurityEntryVOSLst.size());
    verify(spySecurityPositionService, times(1)).updateEntryWithDifferentSign(any(), any());

  }

  @Test
  public void aggregateSecuritiesEntriesByDaySameSign() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    Iterator<SecurityEntryPO> securityEntryPOIterator = mock(Iterator.class);
    SecurityEntryPO securityEntryPO1 = mock(SecurityEntryPO.class);
    SecurityEntryPO securityEntryPO2 = mock(SecurityEntryPO.class);

    when(securityEntryPO1.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);
    when(securityEntryPO2.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.CRDT);

    when(securityEntryPO2.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntries.iterator()).thenReturn(securityEntryPOIterator);
    when(securityEntryPOIterator.hasNext()).thenReturn(true, true, false);
    when(securityEntryPOIterator.next()).thenReturn(securityEntryPO1, securityEntryPO2);
    doNothing().when(spySecurityPositionService).updateEntryAtZero(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithSameSign(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithDifferentSign(any(), any());

    Iterable<AggregatedSecurityEntryVO> returnedAggregatedSecurityEntryVos = spySecurityPositionService.aggregateSecuritiesEntriesByDay(securityEntries);

    List<AggregatedSecurityEntryVO> aggregatedSecurityEntryVOSLst = Lists.newArrayList(returnedAggregatedSecurityEntryVos);

    Assert.assertEquals(1, aggregatedSecurityEntryVOSLst.size());
    verify(spySecurityPositionService, times(1)).updateEntryWithSameSign(any(), any());

  }


  @Test
  public void aggregateSecuritiesEntriesByDayDebitCreditZero() {

    SecurityPositionService spySecurityPositionService = spy(securityPositionService);
    Iterable<SecurityEntryPO> securityEntries = mock(Iterable.class);
    Iterator<SecurityEntryPO> securityEntryPOIterator = mock(Iterator.class);
    SecurityEntryPO securityEntryPO1 = mock(SecurityEntryPO.class);
    SecurityEntryPO securityEntryPO2 = mock(SecurityEntryPO.class);

    when(securityEntryPO1.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntryPO1.getDebitCreditCode()).thenReturn(DEBIT_CREDIT.ZERO);

    when(securityEntryPO2.getValueDate()).thenReturn(LocalDate.now().minusDays(1));
    when(securityEntries.iterator()).thenReturn(securityEntryPOIterator);
    when(securityEntryPOIterator.hasNext()).thenReturn(true, true, false);
    when(securityEntryPOIterator.next()).thenReturn(securityEntryPO1, securityEntryPO2);
    doNothing().when(spySecurityPositionService).updateEntryAtZero(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithSameSign(any(), any());

    doNothing().when(spySecurityPositionService).updateEntryWithDifferentSign(any(), any());

    Iterable<AggregatedSecurityEntryVO> returnedAggregatedSecurityEntryVos = spySecurityPositionService.aggregateSecuritiesEntriesByDay(securityEntries);

    verify(spySecurityPositionService, times(1)).updateEntryAtZero(any(), any());

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

