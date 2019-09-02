package ch.nblotti.leonidas.security.account;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {


  public Account findAccountById(int id);

}
