package ch.nblotti.leonidas.accountrelation;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class AccountRelationServiceTest {

  @MockBean
  private AccountRelationRepository accountRelationRepository;

  @MockBean
  private AccountService accountService;

  @TestConfiguration
  static class AccountRelationServiceTestContextConfiguration {


    @Bean
    public AccountRelationService accountRelationService() {

      return new AccountRelationService();

    }
  }

  @Autowired
  AccountRelationService accountRelationService;


  @Test
  public void findAll() {
    Iterable<AccountRelationPO> accountRelationPOS = mock(Iterable.class);
    when(accountRelationRepository.findAll()).thenReturn(accountRelationPOS);

    Iterable<AccountRelationPO> returnedaccountRelationPOS = accountRelationService.findAll();

    Assert.assertEquals(accountRelationPOS, returnedaccountRelationPOS);
  }

  @Test
  public void findAllOpenedRelationBetweenAccount() {


    int firstAcccountID = 1;
    int secondAccountID = 2;
    AccountRelationPO.RELATION_TYPE relationType = AccountRelationPO.RELATION_TYPE.BENCHMARK;
    Iterable<AccountRelationPO> accountRelation = mock(Iterable.class);

    when(accountRelationRepository.findAllByFirstAccountIdAndSecondAccountIdAndRelationTypeAndRelationStatus(firstAcccountID, secondAccountID, relationType, AccountRelationPO.RELATION_STATUS.OPEN)).thenReturn(accountRelation);

    Iterable<AccountRelationPO> returnedaccountRelationPOS = accountRelationService.findAllOpenedRelationBetweenAccount(firstAcccountID, secondAccountID, relationType);

    Assert.assertEquals(accountRelation, returnedaccountRelationPOS);
  }


  @Test(expected = IllegalStateException.class)
  public void saveFirstAccountNotFound() {

    AccountRelationPO accountRelationPO = mock(AccountRelationPO.class);
    when(accountRelationPO.getFirstAccountId()).thenReturn(1);
    when(accountService.findAccountById(accountRelationPO.getFirstAccountId())).thenReturn(null);
    accountRelationService.save(accountRelationPO);
  }

  @Test(expected = IllegalStateException.class)
  public void saveSecondAccountNotFound() {

    AccountRelationPO accountRelationPO = mock(AccountRelationPO.class);
    AccountPO accountPO = mock(AccountPO.class);
    when(accountRelationPO.getFirstAccountId()).thenReturn(1);
    when(accountService.findAccountById(accountRelationPO.getFirstAccountId())).thenReturn(accountPO);
    when(accountRelationPO.getSecondAccountId()).thenReturn(2);
    when(accountService.findAccountById(accountRelationPO.getSecondAccountId())).thenReturn(null);
    accountRelationService.save(accountRelationPO);
  }

  @Test
  public void saveNoOpenedRelationExisting() {

    AccountRelationService spyAccountRelationService = spy(accountRelationService);
    Iterable<AccountRelationPO> openedRelationStatus = mock(Iterable.class);
    Iterator<AccountRelationPO> accountRelationPOIterator = mock(Iterator.class);
    AccountRelationPO accountRelationPO = mock(AccountRelationPO.class);
    AccountPO accountPO1 = mock(AccountPO.class);
    AccountPO accountPO2 = mock(AccountPO.class);

    when(accountRelationPO.getFirstAccountId()).thenReturn(1);
    when(accountRelationPO.getRelationType()).thenReturn(AccountRelationPO.RELATION_TYPE.BENCHMARK);
    when(openedRelationStatus.iterator()).thenReturn(accountRelationPOIterator);
    when(accountRelationPOIterator.hasNext()).thenReturn(false);
    when(accountService.findAccountById(accountRelationPO.getFirstAccountId())).thenReturn(accountPO1);
    when(accountRelationPO.getSecondAccountId()).thenReturn(2);
    when(accountService.findAccountById(accountRelationPO.getSecondAccountId())).thenReturn(accountPO2);


    doReturn(openedRelationStatus).when(spyAccountRelationService).findAllOpenedRelationBetweenAccount(anyInt(), anyInt(), any());

    spyAccountRelationService.save(accountRelationPO);

    verify(accountRelationRepository, times(1)).save(accountRelationPO);
  }

  @Test(expected = IllegalStateException.class)
  public void saveOpenedRelationExisting() {

    AccountRelationService spyAccountRelationService = spy(accountRelationService);
    Iterable<AccountRelationPO> openedRelationStatus = mock(Iterable.class);
    Iterator<AccountRelationPO> accountRelationPOIterator = mock(Iterator.class);
    AccountRelationPO accountRelationPO = mock(AccountRelationPO.class);
    AccountRelationPO existingRelationPO = mock(AccountRelationPO.class);
    AccountPO accountPO1 = mock(AccountPO.class);
    AccountPO accountPO2 = mock(AccountPO.class);

    when(accountRelationPO.getFirstAccountId()).thenReturn(1);
    when(accountRelationPO.getRelationType()).thenReturn(AccountRelationPO.RELATION_TYPE.BENCHMARK);
    when(openedRelationStatus.iterator()).thenReturn(accountRelationPOIterator);
    when(accountRelationPOIterator.hasNext()).thenReturn(true, false);
    when(accountRelationPOIterator.next()).thenReturn(existingRelationPO);
    when(accountService.findAccountById(accountRelationPO.getFirstAccountId())).thenReturn(accountPO1);
    when(accountRelationPO.getSecondAccountId()).thenReturn(2);
    when(accountService.findAccountById(accountRelationPO.getSecondAccountId())).thenReturn(accountPO2);


    doReturn(openedRelationStatus).when(spyAccountRelationService).findAllOpenedRelationBetweenAccount(anyInt(), anyInt(), any());

    spyAccountRelationService.save(accountRelationPO);

    verify(accountRelationRepository, times(1)).save(accountRelationPO);
  }
}
