package ch.nblotti.leonidas.position.security;

import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.security.SecurityEntry;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SecurityPositionReceiver {


  private static Logger LOGGER = Logger.getLogger("SecurityEntryReceiver");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;


  @Autowired
  MarketProcessService marketProcessService;

  @JmsListener(destination = "securityentrybox", containerFactory = "factory")
  public void receiveNewEntry(Message message) {


    SecurityEntry securityEntry = securityEntryService.findByAccountAndOrderID(message.getAccountID(), message.getOrderID());

    marketProcessService.setSecurityPositionRunningForProcess(message.getOrderID(), message.getAccountID());

    if (securityEntry == null) {
      LOGGER.info(String.format("No securityEntry for id %s, returning", message.getAccountID()));
      return;
    }

    switch (message.getEntityAction()) {

      case CREATE:
        LOGGER.log(Level.FINE, String.format("Start creation of security positions for entry with id %s", message.getAccountID()));
        long startTime = System.nanoTime();
        securityPositionService.updatePosition(securityEntry);
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
        LOGGER.log(Level.FINE, String.format("End creation of security positions for entry from order with id %s, it took me %d seconds", message.getAccountID(), elapsedTime));

        break;
      case DELETE:

        LOGGER.log(Level.FINE, String.format("Delete  security positions for entry from order with id %s", message.getOrderID()));
        break;

      default:
        LOGGER.log(Level.FINE, String.format("Unknown action type for entry from order with id %s", message.getOrderID()));
        break;

    }

  }


}
