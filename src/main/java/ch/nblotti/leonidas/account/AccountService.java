package ch.nblotti.leonidas.account;


import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class AccountService {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private OrderService orderService;


  @Autowired
  DateTimeFormatter dateTimeFormatter;


  public Iterable<AccountPO> findAll() {

    return accountRepository.findAll();
  }

  public AccountPO save(AccountPO accountPO) {

    return accountRepository.save(accountPO);
  }

  public AccountPO findAccountById(Integer id) {

    return accountRepository.findAccountById(id);
  }

  public AccountService() {
  }

  /*for tests*/
  protected AccountService(OrderService orderService) {
    this();
    this.orderService = orderService;
  }

  public AccountPO duplicateAccountById(Integer oldAccountId, AccountPO accountPO) {


    AccountPO newAccountPO = duplicateAccount(oldAccountId, accountPO.getEntryDate(), accountPO.getPerformanceCurrency());
    List<OrderPO> orders = duplicateOrders(newAccountPO, oldAccountId);

    for (OrderPO order : orders)
      orderService.save(order);

    return newAccountPO;
  }

  AccountPO duplicateAccount(Integer oldAccountId, LocalDate date, String perfcurrency) {

    AccountPO acc = accountRepository.findAccountById(oldAccountId);
    if (acc == null)
      throw new IllegalStateException("Reference Account not found");
    AccountPO newAccountPO = new AccountPO();
    newAccountPO.setEntryDate(date);
    newAccountPO.setPerformanceCurrency(perfcurrency == null ? acc.getPerformanceCurrency() : perfcurrency);
    return this.save(newAccountPO);


  }


  List<OrderPO> duplicateOrders(AccountPO newAccountPO, Integer oldAccountId) {

    List<OrderPO> newOrderPOS = new ArrayList<>();

    Iterable<OrderPO> orders = orderService.findByAccountIdAndTransactTimeAfter(oldAccountId, newAccountPO.getEntryDate());
    for (OrderPO currentOrderPO : orders) {
      OrderPO newOrderPO = new OrderPO();
      newOrderPO.setAccountId(newAccountPO.getId());
      newOrderPO.setcIOrdID(currentOrderPO.getcIOrdID());
      newOrderPO.setOrderQtyData(currentOrderPO.getOrderQtyData());
      newOrderPO.setSide(currentOrderPO.getSide());
      newOrderPO.setStatus(currentOrderPO.getStatus());
      newOrderPO.setSymbol(currentOrderPO.getSymbol());
      newOrderPO.setTransactTime(currentOrderPO.getTransactTime());
      newOrderPO.setType(currentOrderPO.getType());
      newOrderPO.setCashCurrency(currentOrderPO.getCashCurrency());
      newOrderPO.setExchange(currentOrderPO.getExchange());
      newOrderPO.setSide(currentOrderPO.getSide());
      newOrderPO.setAmount(currentOrderPO.getAmount());

      newOrderPOS.add(newOrderPO);
    }
    return newOrderPOS;
  }


}
