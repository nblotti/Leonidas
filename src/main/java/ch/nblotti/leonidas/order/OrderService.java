package ch.nblotti.leonidas.order;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.process.order.MarketProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.Optional;

/*Tested but no coverage calculated, as it is using reflection*/
@Component
public class OrderService {


  @Autowired
  private OrderRepository repository;

  @Autowired
  private AccountService accountService;


  @GetMapping("/orders")

  public Iterable<OrderPO> findAll() {

    return this.repository.findAll();

  }

  @MarketProcess(entity = OrderPO.class)
  public OrderPO save(OrderPO orders) {

    AccountPO accountPO = accountService.findAccountById(orders.getAccountId());

    if (accountPO == null)
      throw new IllegalStateException("account does not exists");

    //verifier que la valeur existe

    return this.repository.save(orders);

  }


  public Optional<OrderPO> findById(@PathVariable String id) {


    return this.repository.findById(Long.valueOf(id));

  }


  public Iterable<OrderPO> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return this.repository.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }


  public boolean isOrderValid(Object order) {

    return true;
  }
}
