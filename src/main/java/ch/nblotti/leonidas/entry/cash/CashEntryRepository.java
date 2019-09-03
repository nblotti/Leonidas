package ch.nblotti.leonidas.entry.cash;

import org.springframework.data.repository.CrudRepository;

public interface CashEntryRepository extends CrudRepository<CashEntryPO, Long> {


  public Iterable<CashEntryPO> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency);

  public CashEntryPO findByAccountAndOrderID(int account, long orderID);

}
