package ch.nblotti.leonidas.process;


import ch.nblotti.leonidas.account.Account;
import ch.nblotti.leonidas.account.AccountRepository;
import ch.nblotti.leonidas.order.Order;
import ch.nblotti.leonidas.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class MarketProcessService {


  @Autowired
  private MarketProcessRepository marketProcessRepository;

  @Autowired
  private OrderService orderService;


  @Autowired
  DateTimeFormatter dateTimeFormatter;


  public boolean isProcessForAccountRunning( int accountID) {

    int runningProcess = marketProcessRepository.findRunningProcessForAccount( accountID);

    if (runningProcess != 0)
      return true;

    return false;
  }

  public void startMarketProcessService(Order order, Account account) {

    MarketProcess marketProcess = new MarketProcess();
    marketProcess.setAccountID(account.getId());
    marketProcess.setOrderID(order.getId());
    marketProcess.setOrderType(order.getType());
    marketProcessRepository.save(marketProcess);
  }

  public void setCashEntryRunningForProcess(long orderID, int accountID) {
    MarketProcess marketProcess = marketProcessRepository.readByOrderIDAndAccountID(orderID,accountID);

    marketProcess.setCashEntry(LocalDate.now());

    marketProcessRepository.save(marketProcess);

  }

  public void setSecurityhEntryRunningForProcess(long orderID, int accountID) {
    MarketProcess marketProcess = marketProcessRepository.readByOrderIDAndAccountID(orderID,accountID);

    marketProcess.setSecurityEntry(LocalDate.now());
    marketProcessRepository.save(marketProcess);
  }


  public void setCashPositionRunningForProcess(long orderID, int accountID) {
    MarketProcess marketProcess = marketProcessRepository.readByOrderIDAndAccountID(orderID,accountID);

    marketProcess.setCashPosition(LocalDate.now());
    marketProcessRepository.save(marketProcess);
  }


  public void setSecurityPositionRunningForProcess(long orderID, int accountID) {
    MarketProcess marketProcess = marketProcessRepository.readByOrderIDAndAccountID(orderID,accountID);

    marketProcess.setSecurityPosition(LocalDate.now());
    marketProcessRepository.save(marketProcess);
  }

  public void setSecurityFinishedForProcess(long orderID, int accountID) {

    MarketProcess marketProcess = marketProcessRepository.readByOrderIDAndAccountID(orderID,accountID);

    marketProcess.setSecurityPerformance(LocalDate.now());
    marketProcessRepository.save(marketProcess);
  }

  public void setCashFinishedForProcess(long orderID, int accountID) {

    MarketProcess marketProcess = marketProcessRepository.readByOrderIDAndAccountID(orderID,accountID);

    marketProcess.setCashPerformance(LocalDate.now());
    marketProcessRepository.save(marketProcess);
  }
}
