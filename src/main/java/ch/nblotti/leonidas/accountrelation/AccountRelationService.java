package ch.nblotti.leonidas.accountrelation;


import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AccountRelationService {


  @Autowired
  private AccountRelationRepository accountRelationRepository;

  @Autowired
  private AccountService accountService;


  public AccountRelationService() {
  }

  public Iterable<AccountRelationPO> findAll() {

    return accountRelationRepository.findAll();
  }

  public Iterable<AccountRelationPO> findAllOpenedRelationBetweenAccount(int firstAcccountID, int secondAccountID, AccountRelationPO.RELATION_TYPE relationType) {

    return accountRelationRepository.findAllByFirstAccountIdAndSecondAccountIdAndRelationTypeAndRelationStatus(firstAcccountID, secondAccountID, relationType, AccountRelationPO.RELATION_STATUS.OPEN);
  }


  public AccountRelationPO save(AccountRelationPO accountRelationPO) {

    AccountPO firstAccount = accountService.findAccountById(accountRelationPO.getFirstAccountId());
    if (firstAccount == null)
      throw new IllegalStateException("Une relation doit contenir des comptes existants");
    AccountPO secondAccount = accountService.findAccountById(accountRelationPO.getSecondAccountId());
    if (secondAccount == null)
      throw new IllegalStateException("Une relation doit contenir des comptes existants");

    Iterable<AccountRelationPO> openedRelationStatus = findAllOpenedRelationBetweenAccount(accountRelationPO.getFirstAccountId(), accountRelationPO.getSecondAccountId(), accountRelationPO.getRelationType());
    List<AccountRelationPO> accountRelationList = Lists.newArrayList(openedRelationStatus);

    if (!accountRelationList.isEmpty())
      throw new IllegalStateException("Il ne peut exister qu'une relation du même type ouverte à la fois");
    else
      return accountRelationRepository.save(accountRelationPO);
  }


}
