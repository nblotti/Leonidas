package ch.nblotti.leonidas.order;

import ch.nblotti.leonidas.account.Account;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class OrderService {

  public static final String ORDERBOX = "orderbox";
  @Autowired
  private OrderRepository repository;

  @Autowired
  private AccountService accountService;

  @Autowired
  private JmsTemplate jmsTemplate;


  @Autowired
  MarketProcessService marketProcessService;


  @GetMapping("/orders")
  public Iterable<Order> findAll() {

    return this.repository.findAll();

  }


  public Order save(Order orders) {

    Account account = accountService.findAccountById(orders.getAccountId());

    if (account == null)
      throw new IllegalStateException("account does not exists");

    //verifier que la valeur existe

    Order createdOrder = this.repository.save(orders);

    marketProcessService.startMarketProcessService(createdOrder, account);
    //poster un message sur le bus
    postMessage(createdOrder);
    return createdOrder;
  }


  public Optional<Order> findById(@PathVariable String id) {


    return this.repository.findById(Long.valueOf(id));

  }


  public Iterable<Order> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return this.repository.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }

  public Iterable<Order> saveAll(Iterable<Order> orders) {
    Iterable<Order> newOrders = this.repository.saveAll(orders);

    for (Order createdOrder : newOrders) {
      marketProcessService.startMarketProcessService(createdOrder);
      postMessage(createdOrder);
    }

    return newOrders;
  }

  private void postMessage(Order createdOrder) {
    switch (createdOrder.getType()) {
      case MARKET_ORDER:
        jmsTemplate.convertAndSend(ORDERBOX, new Message(createdOrder.getId(), createdOrder.getAccountId(), Message.MESSAGE_TYPE.MARKET_ORDER, Message.ENTITY_ACTION.CREATE));
        break;

      case CASH_ENTRY:
        jmsTemplate.convertAndSend(ORDERBOX, new Message(createdOrder.getId(),createdOrder.getAccountId(), Message.MESSAGE_TYPE.CASH_ENTRY, Message.ENTITY_ACTION.CREATE));
        break;

      case SECURITY_ENTRY:
        jmsTemplate.convertAndSend(ORDERBOX, new Message(createdOrder.getId(),createdOrder.getAccountId(), Message.MESSAGE_TYPE.SECURITY_ENTRY, Message.ENTITY_ACTION.CREATE));
        break;
    }
  }
}
