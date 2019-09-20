package ch.nblotti.leonidas.account;

import ch.nblotti.leonidas.entry.ACHAT_VENTE_TITRE;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.order.OrderService;
import ch.nblotti.leonidas.process.MarketProcessService;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class AccountServiceTest {

  @TestConfiguration
  static class AccountServiceTestContextConfiguration {


    @Bean
    public AccountService accountService() {

      return new AccountService();

    }
  }


  @MockBean
  MarketProcessService marketProcessService;
  @MockBean
  AccountRepository accountRepository;
  @MockBean
  DateTimeFormatter dateTimeFormatter;

  @MockBean
  OrderService orderService;


  @Autowired
  private AccountService acountService;

  AccountPO account1 = mock(AccountPO.class);
  AccountPO account2 = mock(AccountPO.class);
  LocalDate now = LocalDate.now();


  OrderPO order = mock(OrderPO.class);
  Iterable<OrderPO> orders = Arrays.asList(order);

  @Before
  public void setUp() {

    when(accountRepository.findAll())
      .thenReturn(Arrays.asList(account1, account2));

    when(account1.getId())
      .thenReturn(1);
    when(accountRepository.save(any()))
      .thenReturn(account2);

    when(accountRepository.findAccountById(1))
      .thenReturn(account1);

    when(account1.getPerformanceCurrency())
      .thenReturn("CHF");

    when(account1.getEntryDate())
      .thenReturn(now);

    when(orderService.findByAccountIdAndTransactTimeAfter(1, now))
      .thenReturn(orders);

    when(order.getcIOrdID())
      .thenReturn("1");

    when(account1.getPerformanceCurrency())
      .thenReturn("USD");

    when(order.getOrderQtyData())
      .thenReturn(1F);
    when(order.getSide())
      .thenReturn(ACHAT_VENTE_TITRE.ACHAT);
    when(order.getStatus())
      .thenReturn(1);
    when(order.getSymbol())
      .thenReturn("FB");
    when(order.getSymbol())
      .thenReturn("FB");
    when(order.getTransactTime())
      .thenReturn(now);
    when(order.getType())
      .thenReturn(ORDER_TYPE.CASH_ENTRY);
    when(order.getCashCurrency())
      .thenReturn("CHF");
    when(order.getExchange())
      .thenReturn("US");


  }

  @Test
  public void findAllAccount() {

    List<AccountPO> result = Lists.newArrayList(acountService.findAll());

    assertEquals(account1,result.get(0));
    assertEquals(account2,result.get(1));

  }

  @Test
  public void save() {

    AccountPO result = acountService.save(account1);

    assertEquals( account2,result);
  }

  @Test
  public void findAccountById() {
    AccountPO result = acountService.findAccountById(1);

    assertEquals(account1,result);
  }

  @Test(expected = IllegalStateException.class)

  public void duplicateAccountByIdWrongID() {

    AccountPO result = acountService.duplicateAccount(2, now, "CHF");
  }

  @Test()
  public void duplicateAccount() {
    AccountPO result = acountService.duplicateAccount(1, now, "CHF");

    ArgumentCaptor<AccountPO> argument = ArgumentCaptor.forClass(AccountPO.class);
    verify(accountRepository).save(argument.capture());


  }

  @Test()
  public void duplicateAccountWithoutPerformanceCurrency() {
    AccountPO result = acountService.duplicateAccount(1, now, null);

    ArgumentCaptor<AccountPO> argument = ArgumentCaptor.forClass(AccountPO.class);
    verify(accountRepository).save(argument.capture());
    assertEquals(now, argument.getValue().getEntryDate());
    assertEquals("USD", argument.getValue().getPerformanceCurrency());


  }

  @Test()
  public void duplicateOrders() {
    List<OrderPO> orders = acountService.duplicateOrders(account1, 1);
    assertEquals(1, orders.size());
    OrderPO returned = orders.get(0);

    assertEquals(1F,order.getOrderQtyData(),  0);
    assertEquals(ACHAT_VENTE_TITRE.ACHAT,order.getSide());
    assertEquals( 1,order.getStatus());
    assertEquals("FB",order.getSymbol());
    assertEquals("FB",order.getSymbol());
    assertEquals(now,order.getTransactTime());
    assertEquals(ORDER_TYPE.CASH_ENTRY,order.getType());
    assertEquals("CHF",order.getCashCurrency());
    assertEquals( "US",order.getExchange());

  }


  @Test()
  public void duplicateAccountById() {

    AccountService spyAccountService = Mockito.spy(new AccountService(orderService));
    doReturn(account2).when(spyAccountService).duplicateAccount(anyInt(), any(), anyString());
    doReturn(Lists.newArrayList(orders)).when(spyAccountService).duplicateOrders(any(), anyInt());


    AccountPO returned = spyAccountService.duplicateAccountById(1, account1);

    assertEquals(account2,returned );
  }
}
