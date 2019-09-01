package ch.nblotti.leonidas.security.performance;

import ch.nblotti.leonidas.security.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.security.position.security.SecurityPositionService;
import ch.nblotti.leonidas.security.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class SecurityPerformanceReceiver {


  private static Logger LOGGER = Logger.getLogger("SecurityPerformanceReceiver");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;


  @JmsListener(destination = "securitypositionbox", containerFactory = "factory")
  public void receiveNewEntry(Message message) {


    String account  = message.getEntity_id().toString();

    LOGGER.info(String.format("Recalcul de la performance titre pour le compte %s", message.getEntity_id()));



  }


}
