package ch.nblotti.leonidas.position.security;

import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SecurityPositionReceiver {


  private static Logger logger = Logger.getLogger("SecurityEntryReceiver");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;


  @Autowired
  MarketProcessService marketProcessService;

  @JmsListener(destination = "securityentrybox", containerFactory = "factory")
  public void receiveNewEntry(MessageVO messageVO) {


    SecurityEntryPO securityEntry = securityEntryService.findByAccountAndOrderID(messageVO.getAccountID(), messageVO.getOrderID());

    marketProcessService.setSecurityPositionRunningForProcess(messageVO.getOrderID(), messageVO.getAccountID());

    if (securityEntry == null) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine(String.format("No securityEntry for id %s, returning", messageVO.getAccountID()));
      }
      return;
    }

    switch (messageVO.getEntityAction()) {

      case CREATE:
        if (logger.isLoggable(Level.FINE)) {
          logger.fine(String.format("Start creation of security positions for entry with id %s", messageVO.getAccountID()));
        }
        long startTime = System.nanoTime();
        securityPositionService.updatePosition(securityEntry);
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
        if (logger.isLoggable(Level.FINE)) {
          logger.fine(String.format("End creation of security positions for entry from order with id %s, it took me %d seconds", messageVO.getAccountID(), elapsedTime));
        }
        break;
      case DELETE:

        if (logger.isLoggable(Level.FINE)) {
          logger.fine(String.format("Delete  security positions for entry from order with id %s", messageVO.getOrderID()));
        }
        break;

      default:
        if (logger.isLoggable(Level.FINE)) {
          logger.fine(String.format("Unknown action type for entry from order with id %s", messageVO.getOrderID()));
        }
        break;

    }

  }


}
