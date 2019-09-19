package ch.nblotti.leonidas.process;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.order.OrderService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class MarketProcessServiceTest {


  @MockBean
  private MarketProcessRepository marketProcessRepository;

  @MockBean
  private OrderService orderService;


  @MockBean
  DateTimeFormatter dateTimeFormatter;


  @TestConfiguration
  static class MarketProcessServiceTestContextConfiguration {


    @Bean
    public MarketProcessService marketProcessService() {

      return new MarketProcessService();

    }
  }

  @Autowired
  MarketProcessService marketProcessService;


  @Test
  public void isProcessForAccountRunningOneProcess() {

    int accountID = 1;
    int processRunning = 1;

    when(marketProcessRepository.findRunningProcessForAccount(accountID)).thenReturn(processRunning);

    boolean returned = marketProcessService.isProcessForAccountRunning(accountID);

    Assert.assertEquals(Boolean.TRUE,returned);

  }

  @Test
  public void isProcessForAccountRunningZeroProcess() {

    int accountID = 1;
    int processRunning = 0;

    when(marketProcessRepository.findRunningProcessForAccount(accountID)).thenReturn(processRunning);

    boolean returned = marketProcessService.isProcessForAccountRunning(accountID);

    Assert.assertEquals(Boolean.FALSE,returned);

  }


  @Test
  public void startMarketProcessService() {

    OrderPO orderPO = mock(OrderPO.class);
    int accountID = 0;

    when(orderPO.getAccountId()).thenReturn(accountID);
    when(orderPO.getId()).thenReturn(1l);
    when(orderPO.getType()).thenReturn(ORDER_TYPE.SECURITY_ENTRY);

    marketProcessService.startMarketProcessService(orderPO, accountID);

    verify(marketProcessRepository, times(1)).save(anyObject());
  }


  @Test
  public void startMarketProcessServiceWithAccountPO() {

    OrderPO orderPO = mock(OrderPO.class);
    AccountPO accountPO = mock(AccountPO.class);
    int accountID = 0;
    when(orderPO.getAccountId()).thenReturn(accountID);
    when(orderPO.getId()).thenReturn(1l);
    when(accountPO.getId()).thenReturn(1);
    when(orderPO.getType()).thenReturn(ORDER_TYPE.SECURITY_ENTRY);

    marketProcessService.startMarketProcessService(orderPO, accountPO);

    verify(marketProcessRepository, times(1)).save(anyObject());
  }

  @Test
  public void startMarketProcessServiceWithOrderPO() {

    OrderPO orderPO = mock(OrderPO.class);
    int accountID = 0;
    when(orderPO.getAccountId()).thenReturn(accountID);
    when(orderPO.getId()).thenReturn(1l);
    when(orderPO.getType()).thenReturn(ORDER_TYPE.SECURITY_ENTRY);

    marketProcessService.startMarketProcessService(orderPO);

    verify(marketProcessRepository, times(1)).save(anyObject());
  }

  @Test
  public void setCashEntryRunningForProcess() {

    MarketProcessPO marketProcessPO = mock(MarketProcessPO.class);
    int orderID = 1;
    int accountID = 1;

    when(marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID)).thenReturn(marketProcessPO);
    marketProcessService.setCashEntryRunningForProcess(orderID, accountID);
    verify(marketProcessPO, times(1)).setCashEntry(LocalDate.now());
    verify(marketProcessRepository, times(1)).save(marketProcessPO);
  }

  @Test
  public void setSecurityhEntryRunningForProcess() {

    MarketProcessPO marketProcessPO = mock(MarketProcessPO.class);
    int orderID = 1;
    int accountID = 1;

    when(marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID)).thenReturn(marketProcessPO);
    marketProcessService.setSecurityhEntryRunningForProcess(orderID, accountID);
    verify(marketProcessPO, times(1)).setSecurityEntry(LocalDate.now());
    verify(marketProcessRepository, times(1)).save(marketProcessPO);
  }

  @Test
  public void setCashPositionRunningForProcess() {


    MarketProcessPO marketProcessPO = mock(MarketProcessPO.class);
    int orderID = 1;
    int accountID = 1;

    when(marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID)).thenReturn(marketProcessPO);
    marketProcessService.setCashPositionRunningForProcess(orderID, accountID);
    verify(marketProcessPO, times(1)).setCashPosition(LocalDate.now());
    verify(marketProcessRepository, times(1)).save(marketProcessPO);


  }

  @Test
  public void setSecurityPositionRunningForProcess() {


    MarketProcessPO marketProcessPO = mock(MarketProcessPO.class);
    int orderID = 1;
    int accountID = 1;

    when(marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID)).thenReturn(marketProcessPO);
    marketProcessService.setSecurityPositionRunningForProcess(orderID, accountID);
    verify(marketProcessPO, times(1)).setSecurityPosition(LocalDate.now());
    verify(marketProcessRepository, times(1)).save(marketProcessPO);

  }

  @Test
  public void setSecurityFinishedForProcess() {
    MarketProcessPO marketProcessPO = mock(MarketProcessPO.class);
    int orderID = 1;
    int accountID = 1;

    when(marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID)).thenReturn(marketProcessPO);
    marketProcessService.setSecurityFinishedForProcess(orderID, accountID);
    verify(marketProcessPO, times(1)).setSecurityPerformance(LocalDate.now());
    verify(marketProcessRepository, times(1)).save(marketProcessPO);

  }

  @Test
  public void setCashFinishedForProcess() {
    MarketProcessPO marketProcessPO = mock(MarketProcessPO.class);
    int orderID = 1;
    int accountID = 1;

    when(marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID)).thenReturn(marketProcessPO);
    marketProcessService.setCashFinishedForProcess(orderID, accountID);
    verify(marketProcessPO, times(1)).setCashPerformance(LocalDate.now());
    verify(marketProcessRepository, times(1)).save(marketProcessPO);

  }
}


