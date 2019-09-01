package ch.nblotti.leonidas.security.account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

public interface AccountRepository extends CrudRepository<Account, Long> {


  public Account findAccountById(int id);

}
