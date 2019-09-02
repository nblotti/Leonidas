package ch.nblotti.leonidas.security.order;


import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

@RestController
public class OrderController {


  @Autowired
  OrderService orderService;


  @GetMapping("/orders")
  public Iterable<Order> findAll() {

    return this.orderService.findAll();

  }

  @RequestMapping(value = "/orders", method = RequestMethod.POST)
  public Order save(@Valid @RequestBody Order orders) {//NOSONAR

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
