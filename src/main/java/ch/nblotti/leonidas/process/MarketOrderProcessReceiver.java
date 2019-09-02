package ch.nblotti.leonidas.process;

import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.order.Order;
import ch.nblotti.leonidas.order.OrderController;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import java.util.Optional;
import java.util.logging.Logger;

public class MarketOrderProcessReceiver {

  private final static Logger LOGGER = Logger.getLogger("MarketOrder");

  @Autowired
  AccountService acountService;

  @Autowired
  OrderController orderController;

  @Autowired
  MarketProcessService marketProcessService;


  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(Message message) {


  }


  private Order getOrder(Message message) {
    Optional<Order> order = orderController.findById(String.valueOf(message.getOrderID()));

    if (!order.isPresent()) {
      throw new IllegalStateException(String.format("No order for id %s, returning", message.getOrderID()));
    }
    return order.get();

  }


}
