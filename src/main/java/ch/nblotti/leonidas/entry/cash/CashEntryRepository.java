package ch.nblotti.leonidas.entry.cash;

import org.springframework.data.repository.CrudRepository;

public interface CashEntryRepository extends CrudRepository<CashEntry, Long> {


  public Iterable<CashEntry> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency);

  public CashEntry findByAccountAndOrderID(int account, long orderID);

}
