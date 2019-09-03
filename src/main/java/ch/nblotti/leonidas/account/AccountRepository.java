package ch.nblotti.leonidas.account;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<AccountPO, Long> {


  public AccountPO findAccountById(int id);

}
