package ch.nblotti.leonidas.order;


import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.process.order.MarketProcessor;
import ch.nblotti.leonidas.process.order.ORDER_EVENTS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class OrderController {


  private static final Logger logger = Logger.getLogger("OrderController");
  public static final int PROCESSING = 102;

  @Autowired
  OrderService orderService;

  @Autowired
  MarketProcessService marketProcessService;

  @Autowired
  MarketProcessor marketProcessor;

  @Autowired
  AccountService acountService;


  @GetMapping("/orders")
  public Iterable<OrderPO> findAll() {

    return this.orderService.findAll();

  }

  @PostMapping(value = "/orders")
  public OrderPO save(@Valid @RequestBody OrderPO order, HttpServletResponse response) {//NOSONAR
    Message<ORDER_EVENTS> message;
    switch (order.getType()) {
      case MARKET_ORDER:
        message = MessageBuilder
          .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
          .setHeader("type", ORDER_TYPE.MARKET_ORDER)
          .build();
        marketProcessor.sendEvent(message);
        break;

      case CASH_ENTRY:
        message = MessageBuilder
          .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
          .setHeader("type", ORDER_TYPE.CASH_ENTRY)
          .build();
        marketProcessor.sendEvent(message);
        break;
      default:
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Kind of order not handled");
        }
        break;


    }


    if (marketProcessService.isProcessForAccountRunning(order.getAccountId())) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    if (logger.isLoggable(Level.FINE)) {
      logger.fine(String.format("Create market order process for market order with id %s", order.getId()));
    }


    return this.orderService.save(order);

  }

  @GetMapping(value = "/orders/{id}")
  public Optional<OrderPO> findById(@PathVariable String id) {

    return orderService.findById(id);

  }


  public Iterable<OrderPO> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return orderService.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }
}
