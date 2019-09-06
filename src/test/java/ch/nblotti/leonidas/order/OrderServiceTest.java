package ch.nblotti.leonidas.order;


import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.asset.QuoteService;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class OrderServiceTest {


  @MockBean
  private OrderRepository repository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private JmsTemplate jmsTemplate;


  @MockBean
  MarketProcessService marketProcessService;

  @TestConfiguration
  static class AccountServiceTestContextConfiguration {


    @Bean
    public OrderService orderService() {

      return new OrderService();

    }
  }

  @Autowired
  OrderService orderService;

  @Test
  public void findAll() {


    OrderPO orderPO = mock(OrderPO.class);
    List<OrderPO> orders = Arrays.asList(orderPO);

    when(orderPO.getId()).thenReturn(1l);
    when(repository.findAll()).thenReturn(orders);

    Iterable<OrderPO> returned = orderService.findAll();

    List<OrderPO> returnedList = Lists.newArrayList(returned);
    Assert.assertEquals(1, returnedList.size());
    Assert.assertEquals(1l, (long) returnedList.get(0).getId());

  }


  @Test(expected = IllegalStateException.class)
  public void saveWrongAccountID() {


    OrderPO orderPO = mock(OrderPO.class);
    AccountPO accountPO = mock(AccountPO.class);
    when(orderPO.getAccountId()).thenReturn(1);
    when(accountService.findAccountById(1)).thenReturn(null);

    OrderPO returned = orderService.save(orderPO);
  }

  @Test
  public void save() {


    OrderPO orderPO1 = mock(OrderPO.class);
    AccountPO accountPO = mock(AccountPO.class);
    when(orderPO1.getAccountId()).thenReturn(1);
    when(accountService.findAccountById(1)).thenReturn(accountPO);
    when(repository.save(orderPO1)).thenReturn(orderPO1);
    when(orderPO1.getType()).thenReturn(ORDER_TYPE.MARKET_ORDER);

    OrderPO returned = orderService.save(orderPO1);

    Assert.assertEquals(orderPO1.getId(), returned.getId());
  }


}


