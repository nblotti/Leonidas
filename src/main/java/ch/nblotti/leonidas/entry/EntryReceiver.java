package ch.nblotti.leonidas.entry;

import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.quote.QuoteController;
import ch.nblotti.leonidas.order.OrderController;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class EntryReceiver<T> {


  private static final Logger logger = Logger.getLogger("EntryReceiver");


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
        if (logger.isLoggable(Level.FINE)) {
          logger.fine(String.format("Create entry from order with id %s", messageVO.getOrderID()));
        }
        save(fromOrder(orderPO));
        break;
      case DELETE:
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, String.format("Delete entry from order with id %s", messageVO.getOrderID()));
        }
        break;
      default:
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, String.format("Unsupported action type for order with id %s", messageVO.getOrderID()));
        }
        break;

    }

  }


  protected abstract T fromOrder(OrderPO orderPO);

  protected abstract T save(T entry);


}
