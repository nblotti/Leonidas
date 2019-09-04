package ch.nblotti.leonidas.account;

import ch.nblotti.leonidas.order.OrderService;
import ch.nblotti.leonidas.process.MarketProcessService;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Equals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class AccountServiceTest {

  @TestConfiguration
  static class AccountServiceTestContextConfiguration {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    ;

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

  AccountPO account1 = new AccountPO();
  AccountPO account2 = new AccountPO();

  @Before
  public void setUp() {


    Mockito.when(accountRepository.findAll())
      .thenReturn(Arrays.asList(account1, account2));
  }

  @Test
  public void findAllAccount() {

    List<AccountPO> result = Lists.newArrayList(acountService.findAll());

    assertEquals(result.get(0), account1);
    assertEquals(result.get(1), account2);

  }


}
