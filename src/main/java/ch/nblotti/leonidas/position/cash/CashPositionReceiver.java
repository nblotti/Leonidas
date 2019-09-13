package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class CashPositionReceiver {


  private static Logger LOGGER = Logger.getLogger("CashEntryReceiver");


  @Autowired
  CashEntryService cashEntryService;

  @Autowired
  CashPositionService cashPositionService;

  @Autowired
  MarketProcessService marketProcessService;

  @JmsListener(destination = "cashentrybox", containerFactory = "factory")
  public void receiveNewEntry(MessageVO messageVO) {


    CashEntryPO cashEntryTO = cashEntryService.findByAccountAndOrderID(messageVO.getAccountID(), messageVO.getOrderID());

    marketProcessService.setCashPositionRunningForProcess(messageVO.getOrderID(), messageVO.getAccountID());

    if (cashEntryTO == null) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine(String.format("No cashEntry for id %s, returning", messageVO.getOrderID()));
      }
      return;
    }


    switch (messageVO.getEntityAction()) {

      case CREATE:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Start creation of cash positions for entry with id %s", messageVO.getOrderID()));
        }
        long startTime = System.nanoTime();
        cashPositionService.updatePositions(cashEntryTO);
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("End creation of cash positions for entry from order with id %s, it took me %d seconds", messageVO.getOrderID(), elapsedTime));
        }
        break;
      case DELETE:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Delete  cash positions for entry from order with id %s", messageVO.getOrderID()));
        }
        break;

      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Unknown action type for entry from order with id %s", messageVO.getOrderID()));
        }
        break;

    }

  }


}
