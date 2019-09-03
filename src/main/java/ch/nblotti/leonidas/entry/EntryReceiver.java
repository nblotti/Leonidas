package ch.nblotti.leonidas.entry;

import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.quote.asset.QuoteController;
import ch.nblotti.leonidas.order.OrderController;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class EntryReceiver<T> {


  private static final Logger LOGGER = Logger.getLogger("EntryReceiver");


  @Autowired
  QuoteController quoteController;

  @Autowired
  OrderController orderController;


  private OrderPO getOrder(MessageVO messageVO) {
    Optional<OrderPO> order = orderController.findById(String.valueOf(messageVO.getOrderID()));

    if (!order.isPresent()) {
      throw new IllegalStateException(String.format("No order for id %s, returning", messageVO.getOrderID()));
    }
    return order.get();

  }


  protected void receiveNewOrder(MessageVO messageVO) {


    OrderPO orderPO = getOrder(messageVO);

    switch (messageVO.getEntityAction()) {

      case CREATE:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create entry from order with id %s", messageVO.getOrderID()));
        }
        save(fromOrder(orderPO));
        break;
      case DELETE:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.log(Level.FINE, String.format("Delete entry from order with id %s", messageVO.getOrderID()));
        }
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.log(Level.FINE, String.format("Unsupported action type for order with id %s", messageVO.getOrderID()));
        }
        break;

    }

  }


  protected abstract T fromOrder(OrderPO orderPO);

  protected abstract T save(T entry);


}
