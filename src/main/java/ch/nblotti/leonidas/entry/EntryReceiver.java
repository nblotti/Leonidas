package ch.nblotti.leonidas.entry;

import ch.nblotti.leonidas.quote.asset.QuoteController;
import ch.nblotti.leonidas.order.Order;
import ch.nblotti.leonidas.order.OrderController;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class EntryReceiver<T> {


  private static Logger LOGGER = Logger.getLogger("EntryReceiver");


  @Autowired
  QuoteController quoteController;

  @Autowired
  OrderController orderController;


  private Order getOrder(Message message) {
    Optional<Order> order = orderController.findById(String.valueOf(message.getOrderID()));

    if (!order.isPresent()) {
      throw new IllegalStateException(String.format("No order for id %s, returning", message.getOrderID()));
    }
    return order.get();

  }


  protected void receiveNewOrder(Message message) {


    Order order = getOrder(message);

    switch (message.getEntityAction()) {

      case CREATE:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create entry from order with id %s", message.getOrderID()));
        }
        save(fromOrder(order));
        break;
      case DELETE:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.log(Level.FINE, String.format("Delete entry from order with id %s", message.getOrderID()));
        }
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.log(Level.FINE, String.format("Unsupported action type for order with id %s", message.getOrderID()));
        }
        break;

    }

  }


  protected abstract T fromOrder(Order order);

  protected abstract T save(T entry);


}
