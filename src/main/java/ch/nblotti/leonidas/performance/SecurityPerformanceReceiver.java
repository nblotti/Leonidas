package ch.nblotti.leonidas.performance;

import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.position.security.SecurityPositionService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SecurityPerformanceReceiver {


  private static Logger LOGGER = Logger.getLogger("SecurityPerformanceReceiver");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;

  @Autowired
  MarketProcessService marketProcessService;


  @JmsListener(destination = "securitypositionbox", containerFactory = "factory")
  public void receiveNewEntry(MessageVO messageVO) {


    marketProcessService.setSecurityFinishedForProcess(messageVO.getOrderID(), messageVO.getAccountID());

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(String.format("Recalcul de la performance titre pour le compte %s", messageVO.getAccountID()));
    }

  }


}
