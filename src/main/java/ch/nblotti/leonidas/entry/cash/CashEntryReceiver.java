package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.entry.EntryReceiver;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CashEntryReceiver extends EntryReceiver<CashEntryPO> {

  private static final Logger LOGGER = Logger.getLogger("CashEntryReceiver");

  @Autowired
  CashEntryService cashEntryService;


  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(MessageVO messageVO) {

    switch (messageVO.getMessageType()) {
      case MARKET_ORDER:

        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create cash entry from market order with id %s", messageVO.getOrderID()));
        }
        this.receiveNewOrder(messageVO);
        break;
      case CASH_ENTRY:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create cash entry from cash order with id %s", messageVO.getOrderID()));
        }
        this.receiveNewOrder(messageVO);
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Action unknown for order with id %s", messageVO.getOrderID()));
        }

        break;
    }


  }

  protected CashEntryPO fromOrder(OrderPO orderPO) {

    CashEntryPO cashEntryTO = null;


    switch (orderPO.getType()) {
      case MARKET_ORDER:
        cashEntryTO = cashEntryService.fromMarketOrder(orderPO);
        break;
      case CASH_ENTRY:

        cashEntryTO = cashEntryService.fromCashEntryOrder(orderPO);
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Type unknown for order with id %s", orderPO.getId()));
        }
        break;
    }


    return cashEntryTO;
  }

  @Override
  protected CashEntryPO save(CashEntryPO entry) {

    return cashEntryService.save(entry);
  }


}
