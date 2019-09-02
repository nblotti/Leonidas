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
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      return null;
    }

    LOGGER.log(Level.FINE, String.format("Create market order process for market order with id %s", orders.getId()));

    Account account = getAccount(orders);




    return this.orderService.save(orders);

  }

  private Account getAccount(Order order) {

    Account account = acountService.findAccountById(order.getAccountId());

    if (account == null) {
      throw new IllegalStateException(String.format("No account %s for order %s, returning", order.getAccountId(), order.getId()));
    }
    return account;


  }

  @RequestMapping(value = "/orders/{id}", method = RequestMethod.GET)
  public Optional<Order> findById(@PathVariable String id) {

    return orderService.findById(id);

  }


  public Iterable<Order> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return orderService.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }
}
