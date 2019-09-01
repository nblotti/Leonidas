package ch.nblotti.leonidas.security.entry;

import ch.nblotti.leonidas.security.quote.asset.QuoteController;
import ch.nblotti.leonidas.security.order.Order;
import ch.nblotti.leonidas.security.order.OrderController;
import ch.nblotti.leonidas.security.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.logging.Logger;

public abstract class EntryReceiver<T> {


  private static Logger LOGGER = Logger.getLogger("EntryReceiver");


  @Autowired
  QuoteController quoteController;

  @Autowired
  OrderController orderController;


  private Order getOrder(Message message) {
    Optional<Order> order = orderController.findById(message.getEntity_id().toString());

    if (!order.isPresent()) {
      throw new IllegalStateException(String.format("No order for id %s, returning", message.getEntity_id()));
    }
    return order.get();

  }


  protected void receiveNewOrder(Message message) {

    T createdEntry;

    Order order = getOrder(message);

    switch (message.getEntityAction()) {

      case CREATE:
        LOGGER.info(String.format("Create entry from order with id %s", message.getEntity_id()));
        createdEntry = save(fromOrder(order));
        break;
      case DELETE:
        break;

    }

  }


  protected abstract T fromOrder(Order order);

  protected abstract T save(T entry);


}
