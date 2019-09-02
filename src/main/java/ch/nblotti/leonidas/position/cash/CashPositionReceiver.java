package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.entry.cash.CashEntry;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
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
  public void receiveNewEntry(Message message) {


    CashEntry cashEntry = cashEntryService.findByAccountAndOrderID(message.getAccountID(), message.getOrderID());

    marketProcessService.setCashPositionRunningForProcess(message.getOrderID(), message.getAccountID());

    if (cashEntry == null) {
      LOGGER.log(Level.FINE, String.format("No cashEntry for id %s, returning", message.getOrderID()));
      return;
    }


    switch (message.getEntityAction()) {

      case CREATE:
        LOGGER.log(Level.FINE, String.format("Start creation of cash positions for entry with id %s", message.getOrderID()));
        long startTime = System.nanoTime();
        cashPositionService.updatePosition(cashEntry);
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
        LOGGER.log(Level.FINE, String.format("End creation of cash positions for entry from order with id %s, it took me %d seconds", message.getOrderID(), elapsedTime));
        break;
      case DELETE:

        LOGGER.log(Level.FINE, String.format("Delete  cash positions for entry from order with id %s", message.getOrderID()));
        break;

      default:
        LOGGER.log(Level.FINE, String.format("Unknown action type for entry from order with id %s", message.getOrderID()));
        break;

    }

  }


}
