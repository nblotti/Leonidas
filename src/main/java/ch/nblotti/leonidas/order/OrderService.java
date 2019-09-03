package ch.nblotti.leonidas.order;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
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
  public Iterable<OrderPO> findAll() {

    return this.repository.findAll();

  }


  public OrderPO save(OrderPO orders) {

    AccountPO accountPO = accountService.findAccountById(orders.getAccountId());

    if (accountPO == null)
      throw new IllegalStateException("account does not exists");

    //verifier que la valeur existe

    OrderPO createdOrderPO = this.repository.save(orders);

    marketProcessService.startMarketProcessService(createdOrderPO, accountPO);
    //poster un message sur le bus
    postMessage(createdOrderPO);
    return createdOrderPO;
  }


  public Optional<OrderPO> findById(@PathVariable String id) {


    return this.repository.findById(Long.valueOf(id));

  }


  public Iterable<OrderPO> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime) {
    return this.repository.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }

  public Iterable<OrderPO> saveAll(Iterable<OrderPO> orders) {
    Iterable<OrderPO> newOrders = this.repository.saveAll(orders);

    for (OrderPO createdOrderPO : newOrders) {
      marketProcessService.startMarketProcessService(createdOrderPO);
      postMessage(createdOrderPO);
    }

    return newOrders;
  }

  private void postMessage(OrderPO createdOrderPO) {
    switch (createdOrderPO.getType()) {
      case MARKET_ORDER:
        jmsTemplate.convertAndSend(ORDERBOX, new MessageVO(createdOrderPO.getId(), createdOrderPO.getAccountId(), MessageVO.MESSAGE_TYPE.MARKET_ORDER, MessageVO.ENTITY_ACTION.CREATE));
        break;

      case CASH_ENTRY:
        jmsTemplate.convertAndSend(ORDERBOX, new MessageVO(createdOrderPO.getId(), createdOrderPO.getAccountId(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE));
        break;

      case SECURITY_ENTRY:
        jmsTemplate.convertAndSend(ORDERBOX, new MessageVO(createdOrderPO.getId(), createdOrderPO.getAccountId(), MessageVO.MESSAGE_TYPE.SECURITY_ENTRY, MessageVO.ENTITY_ACTION.CREATE));
        break;
    }
  }
}