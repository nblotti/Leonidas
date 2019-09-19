package ch.nblotti.leonidas.order;


import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.process.MarketProcessService;
import org.springframework.beans.factory.annotation.Autowired;
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
  AccountService acountService;


  @GetMapping("/orders")
  public Iterable<OrderPO> findAll() {

    return this.orderService.findAll();

  }

  @PostMapping(value = "/orders")
  public OrderPO save(@Valid @RequestBody OrderPO orders, HttpServletResponse response) {//NOSONAR

    if (marketProcessService.isProcessForAccountRunning(orders.getAccountId())) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    if (logger.isLoggable(Level.FINE)) {
      logger.fine(String.format("Create market order process for market order with id %s", orders.getId()));
    }


    return this.orderService.save(orders);

  }

  @GetMapping(value = "/orders/{id}")
  public Optional<OrderPO> findById(@PathVariable String id) {

    return orderService.findById(id);

  }


  public Iterable<OrderPO> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return orderService.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }
}
