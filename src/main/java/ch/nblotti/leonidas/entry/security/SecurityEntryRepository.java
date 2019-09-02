package ch.nblotti.leonidas.entry.security;

import org.springframework.data.repository.CrudRepository;

public interface SecurityEntryRepository extends CrudRepository<SecurityEntry, Long> {


  Iterable<SecurityEntry> findAllByAccountAndSecurityIDOrderByValueDateAsc(int account,String securityID);

  SecurityEntry findByAccountAndOrderID(int account,long orderID);
}
