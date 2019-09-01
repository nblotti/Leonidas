package ch.nblotti.leonidas.security.position.cash;

import ch.nblotti.leonidas.security.entry.cash.CashEntry;
import ch.nblotti.leonidas.security.entry.cash.CashEntryService;
import ch.nblotti.leonidas.security.technical.Message;
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


  @JmsListener(destination = "cashentrybox", containerFactory = "factory")
  public void receiveNewEntry(Message message) {


    Optional<CashEntry> cashEntry = cashEntryService.findById(message.getEntity_id().toString());

    if (!cashEntry.isPresent()) {
      LOGGER.log(Level.FINE,String.format("No cashEntry for id %s, returning", message.getEntity_id()));
      return;
    }

    switch (message.getEntityAction()) {

      case CREATE:
        LOGGER.log(Level.FINE, String.format("Start creation of cash positions for entry with id %s", message.getEntity_id()));
        long startTime = System.nanoTime();
        cashPositionService.updatePosition(cashEntry.get());
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
        LOGGER.log(Level.FINE,String.format("End creation of cash positions for entry from order with id %s, it took me %d seconds", message.getEntity_id(),elapsedTime));






        break;
      case DELETE:
        break;

    }

  }


}
