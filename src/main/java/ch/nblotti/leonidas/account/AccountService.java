package ch.nblotti.leonidas.account;


import ch.nblotti.leonidas.order.Order;
import ch.nblotti.leonidas.order.OrderService;
import ch.nblotti.leonidas.process.MarketProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;

@Component
public class AccountService {

  @Autowired
  MarketProcessService marketProcessService;
  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private OrderService orderService;


  @Autowired
  DateTimeFormatter dateTimeFormatter;


  public Iterable<Account> findAll() {

    return accountRepository.findAll();
  }

  public Account save(Account account) {

    return accountRepository.save(account);
  }

  public Account findAccountById(Integer id) {

    return accountRepository.findAccountById(id);
  }

  public Account duplicateAccount(Integer oldAccountId, Account account) {


    Account newAccount = duplicate(oldAccountId, account.getEntryDate(), account.getPerformanceCurrency());
    duplicateOrder(newAccount, oldAccountId);

    return newAccount;
  }

  private Account duplicate(Integer oldAccountId, LocalDate date, String perfcurrency) {

    Account acc = accountRepository.findAccountById(oldAccountId);
    if (acc == null)
      throw new IllegalStateException("Reference Account not found");
    Account newAccount = new Account();
    newAccount.setEntryDate(date);
    newAccount.setPerformanceCurrency(perfcurrency == null ? acc.getPerformanceCurrency() : perfcurrency);
    return this.accountRepository.save(newAccount);


  }


  private void duplicateOrder(Account newAccount, Integer oldAccountId) {

    List<Order> newOrders = new ArrayList<>();

    Iterable<Order> orders = orderService.findByAccountIdAndTransactTimeAfter(oldAccountId, newAccount.getEntryDate());
    for (Order currentOrder : orders) {
      Order newOrder = new Order();
      newOrder.setAccountId(newAccount.getId());
      newOrder.setcIOrdID(currentOrder.getcIOrdID());
      newOrder.setOrderQtyData(currentOrder.getOrderQtyData());
      newOrder.setSide(currentOrder.getSide());
      newOrder.setStatus(currentOrder.getStatus());
      newOrder.setSymbol(currentOrder.getSymbol());
      newOrder.setTransactTime(currentOrder.getTransactTime());
      newOrder.setType(currentOrder.getType());
      newOrder.setCashCurrency(currentOrder.getCashCurrency());
      newOrder.setExchange(currentOrder.getExchange());
      newOrder.setSide(currentOrder.getSide());
      newOrder.setAmount(currentOrder.getAmount());

      newOrders.add(newOrder);
    }
    orderService.saveAll(newOrders);
  }


}
