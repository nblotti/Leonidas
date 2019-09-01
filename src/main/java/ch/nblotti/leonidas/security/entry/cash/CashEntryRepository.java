package ch.nblotti.leonidas.security.entry.cash;

import org.springframework.data.repository.CrudRepository;

public interface CashEntryRepository extends CrudRepository<CashEntry, Long> {


  public Iterable<CashEntry> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency);

}
