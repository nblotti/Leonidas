package ch.nblotti.leonidas.order;


import ch.nblotti.leonidas.account.Account;
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


  private final static Logger LOGGER = Logger.getLogger("OrderController");
  public static final int PROCESSING = 102;

  @Autowired
  OrderService orderService;

  @Autowired
  MarketProcessService marketProcessService;

  @Autowired
  AccountService acountService;


  @GetMapping("/orders")
  public Iterable<Order> findAll() {

    return this.orderService.findAll();

  }

  @RequestMapping(value = "/orders", method = RequestMethod.POST)
  public Order save(@Valid @RequestBody Order orders, HttpServletResponse response) {//NOSONAR

    if (marketProcessService.isProcessForAccountRunning(orders.getAccountId())) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(String.format("Create market order process for market order with id %s", orders.getId()));
    }


    return this.orderService.save(orders);

  }

  @RequestMapping(value = "/orders/{id}", method = RequestMethod.GET)
  public Optional<Order> findById(@PathVariable String id) {

    return orderService.findById(id);

  }


  public Iterable<Order> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return orderService.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }
}
