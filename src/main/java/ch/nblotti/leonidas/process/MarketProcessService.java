package ch.nblotti.leonidas.process;


import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class MarketProcessService {


  @Autowired
  private MarketProcessRepository marketProcessRepository;

  @Autowired
  private OrderService orderService;


  @Autowired
  DateTimeFormatter dateTimeFormatter;


  public boolean isProcessForAccountRunning(int accountID) {

    int runningProcess = marketProcessRepository.findRunningProcessForAccount(accountID);

    return (runningProcess != 0);
  }

  public void startMarketProcessService(OrderPO orderPO, int accountID) {

    MarketProcessPO marketProcessPO = new MarketProcessPO();
    marketProcessPO.setAccountID(accountID);
    marketProcessPO.setOrderID(orderPO.getId());
    marketProcessPO.setOrderType(orderPO.getType());
    marketProcessRepository.save(marketProcessPO);
  }

  public void startMarketProcessService(OrderPO orderPO, AccountPO accountPO) {

    startMarketProcessService(orderPO, accountPO.getId());

  }

  public void startMarketProcessService(OrderPO orderPO) {

    startMarketProcessService(orderPO, orderPO.getAccountId());

  }

  public void setCashEntryRunningForProcess(long orderID, int accountID) {
    MarketProcessPO marketProcessPO = marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID);

    marketProcessPO.setCashEntry(LocalDate.now());

    marketProcessRepository.save(marketProcessPO);

  }

  public void setSecurityhEntryRunningForProcess(long orderID, int accountID) {
    MarketProcessPO marketProcessPO = marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID);

    marketProcessPO.setSecurityEntry(LocalDate.now());
    marketProcessRepository.save(marketProcessPO);
  }


  public void setCashPositionRunningForProcess(long orderID, int accountID) {
    MarketProcessPO marketProcessPO = marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID);

    marketProcessPO.setCashPosition(LocalDate.now());
    marketProcessRepository.save(marketProcessPO);
  }


  public void setSecurityPositionRunningForProcess(long orderID, int accountID) {
    MarketProcessPO marketProcessPO = marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID);

    marketProcessPO.setSecurityPosition(LocalDate.now());
    marketProcessRepository.save(marketProcessPO);
  }

  public void setSecurityFinishedForProcess(long orderID, int accountID) {

    MarketProcessPO marketProcessPO = marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID);

    marketProcessPO.setSecurityPerformance(LocalDate.now());
    marketProcessRepository.save(marketProcessPO);
  }

  public void setCashFinishedForProcess(long orderID, int accountID) {

    MarketProcessPO marketProcessPO = marketProcessRepository.readByOrderIDAndAccountID(orderID, accountID);

    marketProcessPO.setCashPerformance(LocalDate.now());
    marketProcessRepository.save(marketProcessPO);
  }
}
