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
public class CashPerformanceReceiver {


  private static Logger logger = Logger.getLogger("CashPerformanceReceiver");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;

  @Autowired
  MarketProcessService marketProcessService;

  @JmsListener(destination = "cashpositionbox", containerFactory = "factory")
  public void receiveNewEntry(MessageVO messageVO) {


    marketProcessService.setCashFinishedForProcess(messageVO.getOrderID(), messageVO.getAccountID());

    if (logger.isLoggable(Level.FINE)) {
      logger.fine(String.format("Recalcul de la performance cash pour le compte %s", messageVO.getAccountID()));
    }


  }


}
