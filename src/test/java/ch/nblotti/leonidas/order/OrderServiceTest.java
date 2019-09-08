package ch.nblotti.leonidas.order;


import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
@PrepareForTest({ORDER_TYPE.class, OrderService.class})
public class OrderServiceTest {

  @Mock
  ORDER_TYPE order_type;

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

  @Before
  public void init() {
    ORDER_TYPE[] values = ORDER_TYPE.values();
    ORDER_TYPE[] valuesAndAdditional = new ORDER_TYPE[values.length + 1];
    System.arraycopy(values, 0, valuesAndAdditional, 0, values.length);

    PowerMockito.mockStatic(ORDER_TYPE.class);

    Whitebox.setInternalState(order_type, "name", "ADDITIONAL_DAY");
    Whitebox.setInternalState(order_type, "ordinal", values.length);
    valuesAndAdditional[values.length] = order_type;


    /*when(order_type.name()).thenReturn(
      "ADDITIONAL_DAY");*/
    when(ORDER_TYPE.values()).thenReturn(
      valuesAndAdditional);
    when(order_type.ordinal()).thenReturn(values.length);


  }

  @Test(expected = IllegalArgumentException.class)
  public void postDefaultMessage() {


    OrderPO orderPO1 = mock(OrderPO.class);
    MessageVO returned;

    when(orderPO1.getType()).thenReturn(order_type);

    returned = orderService.postMessage(orderPO1);
  }

  @Test
  public void postMessage() {


    OrderPO orderPO1 = mock(OrderPO.class);
    MessageVO returned;
    OrderPO orderPO2 = mock(OrderPO.class);
    OrderPO orderPO3 = mock(OrderPO.class);


    when(orderPO1.getType()).thenReturn(ORDER_TYPE.MARKET_ORDER);
    when(orderPO2.getType()).thenReturn(ORDER_TYPE.CASH_ENTRY);
    when(orderPO3.getType()).thenReturn(ORDER_TYPE.SECURITY_ENTRY);


    returned = orderService.postMessage(orderPO1);
    verify(jmsTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    Assert.assertEquals(MessageVO.MESSAGE_TYPE.MARKET_ORDER, returned.getMessageType());
    reset(jmsTemplate);

    returned = orderService.postMessage(orderPO2);
    verify(jmsTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    Assert.assertEquals(MessageVO.MESSAGE_TYPE.CASH_ENTRY, returned.getMessageType());
    reset(jmsTemplate);

    returned = orderService.postMessage(orderPO3);
    verify(jmsTemplate, times(1)).convertAndSend(anyString(), any(MessageVO.class));
    Assert.assertEquals(MessageVO.MESSAGE_TYPE.SECURITY_ENTRY, returned.getMessageType());
    reset(jmsTemplate);


  }


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

  @Autowired
  OrderService orderService;


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

  @Test
  public void findById() {
    Optional<OrderPO> orderPO1 = mock(Optional.class);
    when(repository.findById(1l)).thenReturn(orderPO1);
    Optional<OrderPO> returned = orderService.findById("1");

    verify(repository, times(1)).findById(1L);
  }

  @Test
  public void findByAccountIdAndTransactTimeAfter() {
    Integer accountId = 1;
    LocalDate transactTime = LocalDate.now();
    Iterable<OrderPO> orders = mock(Iterable.class);
    when(repository.findByAccountIdAndTransactTimeAfter(accountId, transactTime)).thenReturn(orders);


    Iterable<OrderPO> returned = orderService.findByAccountIdAndTransactTimeAfter(accountId, transactTime);
    verify(repository, times(1)).findByAccountIdAndTransactTimeAfter(accountId, transactTime);
  }

  @Test


  public void saveAll() {


    OrderPO orderPO1 = mock(OrderPO.class);
    OrderPO orderPO2 = mock(OrderPO.class);
    Iterable<OrderPO> orders = mock(Iterable.class);
    Iterator<OrderPO> orderPOIterator = mock(Iterator.class);

    when(orderPOIterator.hasNext()).thenReturn(true, true, false);
    when(orderPOIterator.next()).thenReturn(orderPO1).thenReturn(orderPO2);
    when(orderPO1.getType()).thenReturn(ORDER_TYPE.MARKET_ORDER);
    when(orderPO2.getType()).thenReturn(ORDER_TYPE.MARKET_ORDER);

    when(orders.iterator()).thenReturn(orderPOIterator);
    when(repository.saveAll(orders)).thenReturn(orders);

    orderService.saveAll(orders);
    verify(marketProcessService, times(2)).startMarketProcessService(any());

    verify(jmsTemplate, times(2)).convertAndSend(anyString(), any(MessageVO.class));

  }


}



