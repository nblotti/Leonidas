package ch.nblotti.leonidas.security.position.security;

import ch.nblotti.leonidas.security.entry.security.SecurityEntry;
import ch.nblotti.leonidas.security.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.security.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class SecurityPositionReceiver {


  private static Logger LOGGER = Logger.getLogger("SecurityEntryReceiver");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;


  @JmsListener(destination = "securityentrybox", containerFactory = "factory")
  public void receiveNewEntry(Message message) {


    Optional<SecurityEntry> securityEntry = securityEntryService.findById(message.getEntity_id().toString());

    if (!securityEntry.isPresent()) {
      LOGGER.info(String.format("No securityEntry for id %s, returning", message.getEntity_id()));
      return;
    }

    switch (message.getEntityAction()) {

      case CREATE:
        LOGGER.info(String.format("Start creation of security positions for entry with id %s", message.getEntity_id()));
        long startTime = System.nanoTime();
        securityPositionService.updatePosition(securityEntry.get());
        long endTime = System.nanoTime();
        long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
        LOGGER.info(String.format("End creation of security positions for entry from order with id %s, it took me %d seconds", message.getEntity_id(),elapsedTime));

        break;
      case DELETE:
        break;

    }

  }


}
